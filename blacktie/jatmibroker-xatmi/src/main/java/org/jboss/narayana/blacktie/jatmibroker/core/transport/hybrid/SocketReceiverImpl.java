/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.ResponseMonitor;
import org.jboss.narayana.blacktie.jatmibroker.core.server.SocketServer;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.EventListener;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.JtsTransactionImple;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Receiver;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

/**
 * @author zhfeng
 *
 */
public class SocketReceiverImpl implements Receiver, Runnable {
    private static final Logger log = LogManager.getLogger(SocketReceiverImpl.class);
    private int cd;
    private int timeout = 0;
    private SocketServer server;
    private Socket socket;
    private Thread thread;
    private List<Message> data;
    private ResponseMonitor responseMonitor;
    private EventListener eventListener;
    private boolean closed;
    private String replyto;

    public SocketReceiverImpl(SocketServer server, Properties properties, int cd,
            ResponseMonitor responseMonitor, EventListener eventListener) throws ConnectionException {
        log.debug("create socket receiver with server");
        this.server = server;
        this.responseMonitor = responseMonitor;
        this.eventListener = eventListener;
        this.cd = cd;
        this.replyto = new StringBuffer().append(server.getAddr()).append(":").append(server.getPort()).append(":").append(cd).toString();

        timeout = Integer.parseInt(properties.getProperty("ReceiveTimeout")) * 1000
                + Integer.parseInt(properties.getProperty("TimeToLive")) * 1000;
        log.debug("Timeout set as: " + timeout);

        server.register(cd, responseMonitor, eventListener);
    }

    public SocketReceiverImpl(Socket socket, String replyto, Properties properties) {
        log.debug("create socket receiver with socket");
        this.socket = socket;
        this.replyto = replyto;
        this.data = new ArrayList<Message>();
        this.server = null;
        this.responseMonitor = null;
        this.eventListener = null;
        this.cd = Integer.parseInt(replyto.split(":")[2]);

        timeout = Integer.parseInt(properties.getProperty("ReceiveTimeout")) * 1000
                + Integer.parseInt(properties.getProperty("TimeToLive")) * 1000;
        log.debug("Timeout set as: " + timeout);
        this.thread = new Thread(this);
        this.thread.setDaemon(true);
        this.thread.start();
    }

    public int getCd() {
        return cd;
    }

    public Message receive(long flags) throws ConnectionException {    
        if (closed) {
            throw new ConnectionException(Connection.TPEPROTO, "Receiver already closed");
        }
        Message message = null;
        if ((flags & Connection.TPNOBLOCK) != Connection.TPNOBLOCK) {
            if(server != null) {
                message = server.receiveMessage(cd, determineTimeout(flags));
            } else if(socket != null) {
                synchronized (this) {
                    if ((flags & Connection.TPNOBLOCK) != Connection.TPNOBLOCK) {
                        if(data.isEmpty()) {
                            try {
                                wait(determineTimeout(flags));
                            } catch (InterruptedException e) {
                            }
                        }                  
                    }
                    if(!data.isEmpty()) {
                        message = data.remove(0);
                    }
                }
            }
        } else {
            log.debug("Not waiting for the response, hope its there!");
        }
        if(message == null && (flags & Connection.TPNOBLOCK) == Connection.TPNOBLOCK) {
            throw new ConnectionException(Connection.TPEBLOCK, "Did not receive a message");
        } else if (message == null) {
            if (JtsTransactionImple.hasTransaction()) {
                try {
                    log.debug("Marking rollbackOnly");
                    TransactionImpl.current().rollback_only();
                } catch (TransactionException e) {
                    throw new ConnectionException(Connection.TPESYSTEM, "Could not mark transaction for rollback only");
                }
            }
            throw new ConnectionException(Connection.TPETIME, "Did not receive a message");
        } else {
            log.debug("Message was available");
            if (message.rval == EventListener.DISCON_CODE) {
                if (TransactionImpl.current() != null) {
                    try {
                        log.debug("Marking rollbackOnly as disconnection");
                        TransactionImpl.current().rollback_only();
                    } catch (TransactionException e) {
                        throw new ConnectionException(Connection.TPESYSTEM,
                                "Could not mark transaction for rollback only");
                    }
                }
            } else if (message.rcode == Connection.TPESVCERR) {
                if (TransactionImpl.current() != null) {
                    try {
                        log.debug("Marking rollbackOnly as svc err");
                        TransactionImpl.current().rollback_only();
                    } catch (TransactionException e) {
                        throw new ConnectionException(Connection.TPESYSTEM,
                                "Could not mark transaction for rollback only");
                    }
                }
            } else if (message.rval == Connection.TPFAIL) {
                if (TransactionImpl.current() != null) {
                    try {
                        TransactionImpl.current().rollback_only();
                    } catch (TransactionException e) {
                        throw new ConnectionException(Connection.TPESYSTEM,
                                "Could not mark transaction for rollback only");
                    }
                }
            }
        }
        if (responseMonitor != null) {
            responseMonitor.responseReceived(cd, true);
        }
        return message;
    }

    public Object getReplyTo() throws ConnectionException {
        return replyto;
    }

    public void close() throws ConnectionException {
        if(server != null && cd != -1) {
            server.unregister(cd);
        }
        if(thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.warn("receiver close join socket thread failed with " + e);
            }
        }

        if(socket != null) {
            try {
                socket.shutdownInput();
            } catch (SocketException e) {
            } catch (Exception e) {
                log.warn("receiver shutdownInput failed with " + e);
            }
        }
        closed = true;
    }

    public int determineTimeout(long flags) throws ConnectionException {
        if ((flags & Connection.TPNOTIME) == Connection.TPNOTIME) {
            return 0;
        } else {
            return timeout;
        }
    }

    public void run() {
        try {
            DataInputStream ins = new DataInputStream(socket.getInputStream());

            int size;
            while((size = ins.readInt()) != -1) {
                log.debug("size is " + size);
                Message message = new Message();

                byte[] buf = new byte[size];
                int readn = 0;
                int remain = size;
                while(remain > 0) {
                    int n;
                    n = ins.read(buf, readn, remain);

                    if(n != -1) {
                        remain -= n;
                        readn += n;
                    } else {
                        log.error("expect " + size + " but read " + readn);
                        break;
                    }
                }

                if(remain == 0) {
                    //log.debug(buf);
                    String[] s = new String(buf).split("\n");

                    log.debug("sid is " + s[0]);

                    log.debug("cd is " + s[1]);
                    message.cd = Integer.parseInt(s[1]);

                    log.debug("rcode is " + s[2]);
                    message.rcode = Integer.parseInt(s[2]);

                    log.debug("len is " + s[3]);
                    message.len = Integer.parseInt(s[3]);

                    log.debug("flags is " + s[4]);
                    message.flags = Integer.parseInt(s[4]);

                    log.debug("rval is " + s[5]);
                    message.rval = Short.parseShort(s[5]);

                    log.debug("replyto is " + s[6]);
                    message.replyTo = s[6].equals("(null)") ? null : s[6];

                    log.debug("type is " + s[7]);
                    message.type = s[7].equals("(null)") ? null : s[7];

                    log.debug("subtype is " + s[8]);
                    message.subtype = s[8].equals("(null)") ? null : s[8];

                    message.data = new byte[message.len];
                    System.arraycopy(buf, size - message.len, message.data, 0, message.len);
                    log.debug("data is " + new String(message.data));
                    synchronized(this) {
                        if (eventListener != null) {
                            log.debug("Event listener will be called back");
                            if (message.rval == EventListener.DISCON_CODE) {
                                eventListener.setLastEvent(Connection.TPEV_DISCONIMM, message.rcode);
                            } else if (message.rcode == Connection.TPESVCERR) {
                                eventListener.setLastEvent(Connection.TPEV_SVCERR, message.rcode);
                            } else if (message.rval == Connection.TPFAIL) {
                                eventListener.setLastEvent(Connection.TPEV_SVCFAIL, message.rcode);
                            }
                        }
                        data.add(message);
                        if (responseMonitor != null) {
                            responseMonitor.responseReceived(this.cd, false);
                        }
                        notify();
                    }
                }
            }
        } catch (EOFException e) {
            log.info("receiver " + socket + " close");
            closed = true;
        } catch (SocketException e) {
        } catch (IOException e) {
            log.error("receiver " + socket + " run failed with " + e);
        }    
    }

    public Object getEndpoint() throws ConnectionException {
        if(socket != null) {
            return socket;
        } else if(server != null && cd != -1){
            return server.getClientSocket(cd);
        } else return null;
    }
}
