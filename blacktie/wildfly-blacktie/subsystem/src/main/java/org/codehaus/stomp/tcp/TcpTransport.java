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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.JMSException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.stomp.ProtocolException;
import org.codehaus.stomp.Stomp;
import org.codehaus.stomp.StompFrame;
import org.codehaus.stomp.StompMarshaller;
import org.codehaus.stomp.jms.ProtocolConverter;

/**
 * @version $Revision: 65 $
 */
public class TcpTransport {
    private static final Log log = LogFactory.getLog(TcpTransport.class);
    private StompMarshaller marshaller = new StompMarshaller();
    private ProtocolConverter inputHandler;
    private AtomicBoolean stopped = new AtomicBoolean(false);
    private TcpTransportServer tcpTransportServer;
    private SocketChannel socket;

    /**
     * Initialize from a server Socket
     */
    public TcpTransport(TcpTransportServer tcpTransportServer,
            SocketChannel socket) throws IOException {
        this.socket = socket;
        this.tcpTransportServer = tcpTransportServer;
    }

    /**
     * @throws IOException
     */
    public void onStompFrame(StompFrame command) throws IOException {

        if (stopped.get()) {
            throw new ProtocolException("The transport is not running.");
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(byteArrayOutputStream);
        marshaller.marshal(command, dataOut);
        dataOut.flush();
        tcpTransportServer.send(socket, byteArrayOutputStream.toByteArray());
    }

    volatile int currentMax = 0;
    volatile byte[] currentBArray = new byte[] {};

    /**
     * @param numRead
     */
    public void readMessage(byte[] readBuffer, int numRead) {
        log.trace("StompConnect TCP consumer thread starting");

        byte[] data = new byte[currentMax + numRead];
        System.arraycopy(currentBArray, 0, data, 0, currentMax);
        System.arraycopy(readBuffer, 0, data, currentMax, numRead);
        currentMax = data.length;
        currentBArray = data;

        if (data.length < 3 || data[data.length - 3] != 0
                || data[data.length - 2] != 10 || data[data.length - 1] != 10) {
            log.debug("did not read a full frame");
            return;
        }

        DataInputStream dataIn = new DataInputStream(new ByteArrayInputStream(
                currentBArray));
        currentMax = 0;
        currentBArray = new byte[] {};

        try {
            StompFrame frame = marshaller.unmarshal(dataIn);
            log.debug("Sending stomp frame");
            try {
                inputHandler.onStompFrame(frame);
            } catch (IOException e) {
                if (frame.getAction().equals(Stomp.Responses.ERROR)) {
                    log.warn("Could not send frame to client: "
                            + new String(frame.getContent()));
                }
                throw e;
            }
        } catch (Throwable e) {
            // no need to log EOF exceptions
            if (e instanceof EOFException) {
                // Happens when the remote side disconnects
                log.debug("Caught an EOFException: " + e.getMessage(), e);
            } else {
                log.fatal("Caught an exception: " + e.getMessage(), e);
            }
            try {
                stop();
            } catch (Exception e2) {
                log.warn("Caught while closing: " + e2 + ". Now Closed", e2);
            }
        }
    }

    public void setProtocolConverter(ProtocolConverter protocolConverter) {
        this.inputHandler = protocolConverter;
    }

    public void stop() throws InterruptedException, IOException, JMSException,
            URISyntaxException {
        if (stopped.compareAndSet(false, true)) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Stopping transport " + this);
                }
                if (inputHandler != null) {
                    inputHandler.close();
                }

                socket.close();
            } finally {
                stopped.set(true);
            }
        }
    }
}
