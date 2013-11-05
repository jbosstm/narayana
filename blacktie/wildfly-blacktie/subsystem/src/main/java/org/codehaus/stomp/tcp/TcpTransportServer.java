/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.stomp.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.stomp.jms.StompConnect;

/**
 * @version $Revision: 52 $
 */
public class TcpTransportServer implements Runnable {
    private static final Log log = LogFactory.getLog(TcpTransportServer.class);
    private StompConnect stompHandlerFactory;
    private boolean daemon = true;
    private Thread runner;
    private URI connectURI;
    private URI bindLocation;
    private Map<SocketChannel, TcpTransport> connections = new HashMap<SocketChannel, TcpTransport>();
    private AtomicBoolean stopped = new AtomicBoolean(false);

    private ServerSocketChannel serverChannel;

    private Selector selector;

    private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

    public TcpTransportServer(StompConnect stompHandlerFactory, URI location)
            throws IOException, URISyntaxException {
        this.stompHandlerFactory = stompHandlerFactory;
        this.connectURI = location;
        this.bindLocation = location;

    }

    /**
     * @return pretty print of this
     */
    public String toString() {
        return bindLocation.toString();
    }

    // A list of ChangeRequest instances
    private List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<SocketChannel, List<ByteBuffer>>();

    public void send(SocketChannel socket, byte[] data) {
        synchronized (this.changeRequests) {
            // Indicate we want the interest ops set changed
            this.changeRequests.add(new ChangeRequest(socket,
                    ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.pendingData) {
                List<ByteBuffer> queue = this.pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList<ByteBuffer>();
                    this.pendingData.put(socket, queue);
                }
                queue.add(ByteBuffer.wrap(data));
            }
        }

        // Finally, wake up our selecting thread so it can make the required
        // changes
        this.selector.wakeup();
    }

    public void run() {
        while (!stopped.get()) {
            try {
                // Process any pending changes
                synchronized (this.changeRequests) {
                    Iterator<ChangeRequest> changes = this.changeRequests
                            .iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = changes.next();
                        switch (change.type) {
                        case ChangeRequest.CHANGEOPS:
                            SelectionKey key = change.socket
                                    .keyFor(this.selector);
                            key.interestOps(change.ops);
                        }
                    }
                    this.changeRequests.clear();
                }

                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator<SelectionKey> selectedKeys = this.selector
                        .selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        // For an accept to be pending the channel must be a
                        // server socket channel.
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key
                                .channel();

                        // Accept the connection and make it non-blocking
                        SocketChannel socketChannel = serverSocketChannel
                                .accept();
                        Socket socket = socketChannel.socket();
                        if (stopped.get()) {
                            socket.close();
                        } else {
                            socketChannel.configureBlocking(false);

                            // Register the new SocketChannel with our Selector,
                            // indicating
                            // we'd like to be notified when there's data
                            // waiting to
                            // be read
                            socketChannel.register(this.selector,
                                    SelectionKey.OP_READ);

                            TcpTransport transport = new TcpTransport(this,
                                    socketChannel);
                            stompHandlerFactory
                                    .assignProtocolConverter(transport);
                            connections.put(socketChannel, transport);
                        }
                    } else if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key
                                .channel();

                        // Clear out our read buffer so it's ready for new data
                        this.readBuffer.clear();

                        // Attempt to read off the channel
                        int numRead;
                        try {
                            numRead = socketChannel.read(this.readBuffer);
                        } catch (IOException e) {
                            // The remote forcibly closed the connection, cancel
                            // the selection key and close the channel.
                            key.cancel();
                            socketChannel.close();
                            return;
                        }
                        TcpTransport tcpTransport = connections
                                .get(socketChannel);

                        if (numRead == -1) {
                            tcpTransport.stop();
                            // Remote entity shut the socket down cleanly. Do
                            // the
                            // same from our end and cancel the channel.
                            key.channel().close();
                            key.cancel();
                        } else {
                            tcpTransport.readMessage(this.readBuffer.array(), numRead);
                        }
                    } else if (key.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) key
                                .channel();

                        synchronized (this.pendingData) {
                            List<ByteBuffer> queue = this.pendingData
                                    .get(socketChannel);

                            // Write until there's not more data ...
                            while (!queue.isEmpty()) {
                                ByteBuffer buf = (ByteBuffer) queue.get(0);
                                socketChannel.write(buf);
                                if (buf.remaining() > 0) {
                                    log.warn("buffer overflow");
                                    // ... or the socket's buffer fills up
                                    break;
                                }
                                queue.remove(0);
                            }

                            if (queue.isEmpty()) {
                                // We wrote away all data, so we're no longer
                                // interested
                                // in writing on this socket. Switch back to
                                // waiting for
                                // data.
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (!stopped.get()) {
                    log.error("Received accept error: " + e, e);
                    try {
                        stop();
                    } catch (Exception e1) {
                        log.error("Failed to shut down: " + e, e);
                    }
                }
            }
        }
    }

    public void start() throws IOException {
        URI bind = bindLocation;

        String host = bind.getHost();
        host = (host == null || host.length() == 0) ? "localhost" : host;

        // Create a new selector
        selector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(host, bind.getPort());
        serverChannel.socket().bind(isa);

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        log.info("Listening for connections at: " + connectURI);
        runner = new Thread(this, "StompConnect Server Thread: " + toString());
        runner.setDaemon(daemon);
        runner.start();
    }

    public void stop() throws InterruptedException, IOException, JMSException,
            URISyntaxException {
        stopped.set(true);

        if (selector != null) {
            selector.close();
        }

        // lets stop accepting new connections first
        if (serverChannel != null) {
            serverChannel.close();
        }

        // now lets close all the connections
        try {
            for (TcpTransport connection : connections.values()) {
                connection.stop();
            }
        } finally {
            connections.clear();
        }

        // lets join the server thread in case its blocked a little while
        if (runner != null) {
            log.debug("Attempting to join with runner");
            runner.join();
            log.debug("Joined with runner");
            runner = null;
        }
    }

    private class ChangeRequest {
        public static final int CHANGEOPS = 2;

        public SocketChannel socket;
        public int type;
        public int ops;

        public ChangeRequest(SocketChannel socket, int type, int ops) {
            this.socket = socket;
            this.type = type;
            this.ops = ops;
        }
    }
}
