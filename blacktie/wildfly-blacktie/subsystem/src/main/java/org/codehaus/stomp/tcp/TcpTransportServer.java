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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;
import javax.net.ServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.stomp.jms.StompConnect;

/**
 * @version $Revision: 52 $
 */
public class TcpTransportServer implements Runnable {
    private static final Log log = LogFactory.getLog(TcpTransportServer.class);
    private StompConnect stompHandlerFactory;
    private ServerSocket serverSocket;
    private int backlog = 5000;
    private ServerSocketFactory serverSocketFactory;
    private boolean daemon = true;
    private Thread runner;
    private URI connectURI;
    private URI bindLocation;
    private List<TcpTransport> connections = new CopyOnWriteArrayList<TcpTransport>();
    private AtomicBoolean stopped = new AtomicBoolean(false);

    public TcpTransportServer(StompConnect stompHandlerFactory, URI location, ServerSocketFactory serverSocketFactory)
            throws IOException, URISyntaxException {
        this.stompHandlerFactory = stompHandlerFactory;
        this.connectURI = location;
        this.bindLocation = location;
        this.serverSocketFactory = serverSocketFactory;
    }

    /**
     * @return pretty print of this
     */
    public String toString() {
        return bindLocation.toString();
    }

    /**
     * pull Sockets from the ServerSocket
     */
    public void run() {
        while (!stopped.get()) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                if (socket != null) {
                    if (stopped.get()) {
                        socket.close();
                    } else {
                        TcpTransport transport = new TcpTransport(socket);
                        stompHandlerFactory.assignProtocolConverter(transport);
                        transport.start();
                        connections.add(transport);
                    }
                }
            } catch (SocketTimeoutException ste) {
                // expect this to happen
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
        InetAddress addr = InetAddress.getByName(host);

        try {
            this.serverSocket = serverSocketFactory.createServerSocket(bind.getPort(), backlog, addr);
            this.serverSocket.setSoTimeout(2000);
        } catch (IOException e) {
            throw new IOException("Failed to bind to server socket: " + bind + " due to: " + e, e);
        }
        try {
            connectURI = new URI(bind.getScheme(), bind.getUserInfo(), bind.getHost(), serverSocket.getLocalPort(),
                    bind.getPath(), bind.getQuery(), bind.getFragment());
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage(), e);
        }

        log.info("Listening for connections at: " + connectURI);
        runner = new Thread(this, "StompConnect Server Thread: " + toString());
        runner.setDaemon(daemon);
        runner.start();
    }

    public void stop() throws InterruptedException, IOException, JMSException, URISyntaxException {
        stopped.set(true);

        // lets stop accepting new connections first
        if (serverSocket != null) {
            serverSocket.close();
        }

        // now lets close all the connections
        try {
            for (TcpTransport connection : connections) {
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
}
