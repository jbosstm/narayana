/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.ResponseMonitor;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.EventListener;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.JtsTransactionImple;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.OrbManagement;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Receiver;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;

import AtmiBroker.EndpointQueuePOA;

public class CorbaReceiverImpl extends EndpointQueuePOA implements Receiver {
    private static final Logger log = LogManager.getLogger(CorbaReceiverImpl.class);
    private POA m_default_poa;
    private String callbackIOR;
    private List<Message> returnData = new ArrayList<Message>();
    private byte[] activate_object;
    private int timeout = 0;
    private EventListener eventListener;

    private int pad = 0;
    private ResponseMonitor responseMonitor;
    private int cd;
    private boolean closed;

    CorbaReceiverImpl(OrbManagement orbManagement, Properties properties, int cd, ResponseMonitor responseMonitor)
            throws ConnectionException {
        this(null, orbManagement, properties, cd, responseMonitor);
    }

    CorbaReceiverImpl(EventListener eventListener, OrbManagement orbManagement, Properties properties)
            throws ConnectionException {
        this(eventListener, orbManagement, properties, -1, null);
    }

    private CorbaReceiverImpl(EventListener eventListener, OrbManagement orbManagement, Properties properties, int cd,
            ResponseMonitor responseMonitor) throws ConnectionException {
        log.debug("ClientCallbackImpl constructor");
        ORB orb = orbManagement.getOrb();
        POA poa = orbManagement.getRootPoa();
        this.eventListener = eventListener;
        this.cd = cd;
        this.responseMonitor = responseMonitor;

        try {
            try {
                Policy[] policies = new Policy[0];
                m_default_poa = poa.create_POA("TODO", poa.the_POAManager(), policies);
            } catch (AdapterAlreadyExists e) {
                m_default_poa = poa.find_POA("TODO", true);
            }
            log.debug("CorbaReceiverImpl createCallbackObject");
            activate_object = m_default_poa.activate_object(this);
            log.debug("activated this " + this);

            org.omg.CORBA.Object tmp_ref = m_default_poa.servant_to_reference(this);
            log.debug("created reference " + tmp_ref);
            AtmiBroker.EndpointQueue clientCallback = AtmiBroker.EndpointQueueHelper.narrow(tmp_ref);
            log.debug("narrowed reference " + clientCallback);
            callbackIOR = orb.object_to_string(clientCallback);
            log.debug("Created:" + callbackIOR);
        } catch (Throwable t) {
            throw new ConnectionException(Connection.TPESYSTEM, "Cannot create the receiver", t);
        }
        timeout = Integer.parseInt(properties.getProperty("ReceiveTimeout")) * 1000
                + Integer.parseInt(properties.getProperty("TimeToLive")) * 1000;
        log.debug("Timeout set as: " + timeout);
    }

    public POA _default_POA() {
        log.debug("ClientCallbackImpl _default_POA");
        return m_default_poa;
    }

    public synchronized void send(String replyto_ior, short rval, int rcode, byte[] idata, int ilen, int cd, int flags,
            String type, String subtype) {
        log.debug("Received: " + callbackIOR);
        Message message = new Message();
        message.cd = cd;
        message.replyTo = replyto_ior;
        message.flags = flags;
        message.control = null;// TODO
        message.rval = rval;
        message.rcode = rcode;
        message.type = type;
        message.subtype = subtype;
        message.len = ilen - pad;
        if (message.len == 0 && message.type == "") {
            message.data = null;
        } else {
            message.data = new byte[message.len];
            System.arraycopy(idata, 0, message.data, 0, message.len);
        }

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

        returnData.add(message);
        if (responseMonitor != null) {
            responseMonitor.responseReceived(this.cd, false);
        }
        log.trace("notifying");
        notify();
        log.trace("notifed");
    }

    public java.lang.Object getReplyTo() {
        return callbackIOR;
    }

    public Message receive(long flags) throws ConnectionException {
        log.debug("Receiving: " + callbackIOR);
        synchronized (this) {
            if ((flags & Connection.TPNOBLOCK) != Connection.TPNOBLOCK) {
                if (returnData.isEmpty()) {
                    try {
                        log.debug("Waiting: " + callbackIOR);
                        wait(determineTimeout(flags));
                        log.debug("Waited: " + callbackIOR);
                    } catch (InterruptedException e) {
                        log.error("Caught exception", e);
                    }
                }
            } else {
                log.debug("Not waiting for the response, hope its there!");
            }
            if (returnData.isEmpty() && (flags & Connection.TPNOBLOCK) == Connection.TPNOBLOCK) {
                throw new ConnectionException(Connection.TPEBLOCK, "Did not receive a message");
            } else if (returnData.isEmpty()) {
                log.debug("Empty return data: " + callbackIOR);
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
                Message message = returnData.remove(0);
                if (message != null) {
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
                } else {
                    log.debug("message was null");
                }
                if (responseMonitor != null) {
                    responseMonitor.responseReceived(cd, true);
                }
                return message;
            }
        }
    }

    public void disconnect() {
        log.debug("disconnect");
        try {
            log.debug("deactivating");
            m_default_poa.deactivate_object(activate_object);
            log.debug("deactivated");
        } catch (Throwable t) {
            log.error("Could not unbind service factory", t);
        }
        log.trace("synchronizing");
        synchronized (this) {
            log.trace("notifying");
            notify();
            log.trace("notified");
        }
    }

    public void close() throws ConnectionException {
        log.debug("close: " + callbackIOR);
        if (closed) {
            throw new ConnectionException(Connection.TPEPROTO, "Sender already closed");
        }
        disconnect();
        closed = true;
    }

    public int determineTimeout(long flags) throws ConnectionException {
        if ((flags & Connection.TPNOTIME) == Connection.TPNOTIME) {
            return 0;
        } else {
            return timeout;
        }
    }

    public int getCd() {
        return cd;
    }

    public Object getEndpoint() throws ConnectionException {
        return null;
    }
}
