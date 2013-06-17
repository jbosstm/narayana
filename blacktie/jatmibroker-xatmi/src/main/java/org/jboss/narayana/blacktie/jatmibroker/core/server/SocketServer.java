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

package org.jboss.narayana.blacktie.jatmibroker.core.server;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.ResponseMonitor;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.EventListener;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;

/**
 * Socket Server for client call back
 * @author zhfeng
 *
 */
public class SocketServer implements Runnable {
    private static final Logger log = LogManager.getLogger(SocketServer.class);
    private int port;
    private String addr;
    private ServerSocket serverSocket;
    private boolean shutdown;
    private List<Thread> threads;
    private List<Client> clients;
    private List<ClientContext> contexts;

    private static SocketServer instance;
    private static int reference;
    private Thread serverThread;

    public static synchronized SocketServer getInstance(Properties properties) throws IOException {
        if (instance == null) {
            int port = Integer.parseInt(properties.getProperty("blacktie.java.socketserver.port"));
            String addr = properties.getProperty("blacktie.java.socketserver.host", "localhost");
            instance = new SocketServer(port, addr);
            reference = 0;
        }

        reference ++;
        return instance;
    }

    public static synchronized void discardInstance() {
        reference --;
        if(reference == 0) {
            instance.shutdown();
            instance = null;
        }
    }

    private SocketServer(int port, String addr) throws IOException {
        this.shutdown = false;
        this.port = port;
        this.addr = addr;
        serverSocket = new ServerSocket(port);
        clients = new ArrayList<Client>();
        threads = new ArrayList<Thread>();
        contexts = new ArrayList<ClientContext>();

        serverThread = new Thread(this);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public int getPort() {
        return port;
    }

    public String getAddr() {
        return addr;
    }

    public Socket getClientSocket(int sid) {
        ClientContext context = getContext(sid);
        if(context != null) return context.getSocket();
        else return null;
    }

    public synchronized ClientContext register(int sid, ResponseMonitor responseMonitor, EventListener eventListener) {
        for(int i = 0; i < contexts.size(); i++) {
            if(contexts.get(i).getSid() == sid) {
                return contexts.get(i);
            }
        }

        ClientContext context = new ClientContext();
        context.setSid(sid);
        context.setResponseMonitor(responseMonitor);
        context.setEventListener(eventListener);
        contexts.add(context);
        log.debug("register sid " + sid);
        return context;
    }

    public synchronized void unregister(int sid) {
        for(int i = 0; i < contexts.size(); i++) {
            if(contexts.get(i).getSid() == sid) {
                contexts.remove(i);
                log.debug("unregister sid " + sid);
            }
        }
    }

    public Message receiveMessage(int sid, long timeout) {
        ClientContext context = this.getContext(sid);
        Message msg = null;
        if(context != null) {
            synchronized(context) {
                msg = context.getMessage(timeout);
            }
        } else {
            log.warn("Could not receive message for sid " + sid);
        }
        return msg;
    }

    public ClientContext getContext(int sid) {
        for(int i = 0; i < contexts.size(); i++) {
            if(contexts.get(i).getSid() == sid) {
                return contexts.get(i);
            }
        }
        return null;
    }

    private void shutdown() {
        log.debug("shutdowning server");
        try {
            shutdown = true;
            for(int i = 0; i < threads.size(); i++) {
                try {
                    clients.get(i).close();
                    threads.get(i).join();
                } catch (InterruptedException e) {
                    log.error("join client " + i + " failed with " + e);
                }
            }
            if(serverSocket != null) {
                serverSocket.close();
            }
            serverThread.join();
        } catch (IOException e) {
            log.error("close server socket failed with " + e);
        } catch (InterruptedException e) {
            log.error("Could not join serverThread with " + e);
        }
        log.debug("shutdown server");
    }

    protected void finalize() {
        this.shutdown();
    }

    /* running to accept client connection and handle the message.
     * 
     */
    public void run() {
        while(!shutdown) {
            try {
                Socket clientSocket  = serverSocket.accept();
                log.debug("connection from " + clientSocket);
                Client client = new Client(this, clientSocket);
                clients.add(client);
                Thread thread = new Thread(client);
                threads.add(thread);                
                thread.start();
            } catch(SocketException e) {
            } catch(IOException e) {
                log.error("run server failed with " + e);
            }
        }
    }  
}

class Client implements Runnable {
    private static final Logger log = LogManager.getLogger(Client.class);
    private Socket socket;
    private int sid;
    private boolean isClose;
    private SocketServer server;

    public Client(SocketServer server, Socket socket) {
        this.sid = -1;
        this.socket = socket;
        this.isClose = false;
        this.server = server;
    }

    public boolean isClose() {
        return isClose;
    }

    public int getSid() {
        return sid;
    }

    public void close() {
        if(!isClose && socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
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
                    sid = Integer.parseInt(s[0]);

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

                    ClientContext context = server.getContext(sid);
                    if(context != null) {
                        synchronized(context) {
                            EventListener eventListener = context.getEventListener();
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
                            context.getData().add(message);
                            log.debug("add message to context " + context.getSid());
                            context.setSocket(socket);
                            ResponseMonitor responseMonitor = context.getResponseMonitor();
                            if(responseMonitor != null) {
                                responseMonitor.responseReceived(sid, false);
                            }
                            log.debug("notifying");
                            context.notify();
                            log.debug("notified");
                        }
                    }
                }
            }
            socket.shutdownInput();   
            isClose = true;
        } catch (EOFException e) {
            log.debug("client " + socket + " close");
            isClose = true;
        } catch (SocketException e) {
            isClose = true;
        } catch (IOException e) {
            log.error("client " + socket + " run failed with " + e);
        }       
    }
}

class ClientContext {
    private static final Logger log = LogManager.getLogger(ClientContext.class);
    private int sid;
    private List<Message> data;
    private Socket socket;
    private ResponseMonitor responseMonitor;
    private EventListener eventListener;

    public ClientContext() {
        this.data = new ArrayList<Message>();
    }
    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getSid() {
        return sid;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public List<Message> getData() {
        return data;
    }

    public void setResponseMonitor(ResponseMonitor responseMonitor) {
        this.responseMonitor = responseMonitor;        
    }

    public ResponseMonitor getResponseMonitor() {
        return responseMonitor;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public Message getMessage(long timeout) {
        log.debug("receive message for context " + sid);
        if(data.isEmpty()) {
            synchronized(this) {
                try {
                    log.debug("waiting for " + timeout);
                    wait(timeout);
                    log.debug("waited");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(data.isEmpty()) {
            return null;
        } else {
            return data.remove(0);
        }
    }
}
