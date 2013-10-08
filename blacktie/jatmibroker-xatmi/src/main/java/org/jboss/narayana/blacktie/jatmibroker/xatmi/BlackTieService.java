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
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import javax.naming.NamingException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.annotation.PostConstruct;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.JtsTransactionImple;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.BufferImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.ConnectionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.SessionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.TPSVCINFO_Impl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.X_OCTET_Impl;
import org.jboss.narayana.rest.bridge.inbound.InboundBridge;
import org.jboss.narayana.rest.bridge.inbound.InboundBridgeManager;

/**
 * MDB services implementations extend this class as it provides the core service template method. For non MDB services on the
 * Service interface need be implemented.
 */
public abstract class BlackTieService implements Service {
    /**
     * The logger to use.
     */
    private static final Logger log = LogManager.getLogger(BlackTieService.class);

    private ConnectionFactory connectionFactory;

    private String name;

    protected BlackTieService(String name) throws ConfigurationException {
        //connectionFactory = ConnectionFactory.getConnectionFactory();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @PostConstruct
    public void init() {
        log.info("init PostConstruct");
        try {
            connectionFactory = ConnectionFactory.getConnectionFactory();
        } catch (ConfigurationException e) {
            log.warn("init failed with " + e);
        }
    }


    /**
     * Entry points should pass control to this method as soon as reasonably possible.
     * 
     * @param serviceName The name of the service
     * @param message The message to process
     * @throws ConnectionException
     * @throws ConnectionException In case communication fails
     * @throws ConfigurationException
     * @throws NamingException
     * @throws SystemException
     * @throws IllegalStateException
     * @throws InvalidTransactionException
     * @throws TransactionException 
     */
    protected void processMessage(String serviceName, Message message) throws ConnectionException, ConfigurationException,
    NamingException, InvalidTransactionException, IllegalStateException, SystemException, TransactionException {
        log.trace("Service invoked");
        if(connectionFactory == null) {
            connectionFactory = ConnectionFactory.getConnectionFactory();
        }
        ConnectionImpl connection = (ConnectionImpl) connectionFactory.getConnection();
        try {
            boolean hasTx = false;
            boolean hasTPNOREPLY = (message.flags & Connection.TPNOREPLY) == Connection.TPNOREPLY;
            boolean responseSendable = !hasTPNOREPLY;
            // To respond with
            short rval = Connection.TPFAIL;
            int rcode = Connection.TPESVCERR;
            byte[] data = null;
            int len = 0;
            int flags = 0;
            String type = null;
            String subtype = null;
            SessionImpl serviceSession = ((ConnectionImpl)connection).createServiceSession(serviceName, message.cd, message.replyTo);
            InboundBridge inboundBridge = null;
            try {
                boolean hasTPCONV = (message.flags & Connection.TPCONV) == Connection.TPCONV;
                Boolean conversational = (Boolean) connectionFactory.getProperties().get("blacktie." + serviceName + ".conversational");
                log.trace(serviceName);
                boolean isConversational = conversational == true;
                if (hasTPCONV && isConversational) {
                    X_OCTET odata = new X_OCTET_Impl(null);
                    byte[] ack = new byte[4];
                    byte[] bytes = "ACK".getBytes();
                    System.arraycopy(bytes, 0, ack, 0, 3);
                    odata.setByteArray(ack);
                    long result = serviceSession.tpsend(odata, 0);
                    if (result == -1) {
                        log.error("Could not send ack");
                        serviceSession.close();
                        return;
                    } else {
                        log.debug("Sent ack");
                        serviceSession.setCreatedState(message.flags);
                    }
                } else if (!hasTPCONV && !isConversational) {
                    log.debug("Session was not a TPCONV");
                } else {
                    log.error("Session was invoked in an improper manner");
                    X_OCTET odata = new X_OCTET_Impl(null);
                    byte[] ack = new byte[4];
                    byte[] bytes = "ERR".getBytes();
                    System.arraycopy(bytes, 0, ack, 0, 3);
                    odata.setByteArray(bytes);
                    long result = serviceSession.tpsend(odata, 0);
                    if (result == -1) {
                        log.error("Could not send err");
                    } else {
                        log.error("Error reported");
                    }
                    serviceSession.close();
                    return;
                }
                log.debug("Created the session");
                // THIS IS THE FIRST CALL
                BufferImpl buffer = null;
                if (message.type != null && !message.type.equals("")) {
                    buffer = (BufferImpl) connection.tpalloc(message.type, message.subtype);
                    buffer.deserialize(message.data);
                }
                TPSVCINFO tpsvcinfo = new TPSVCINFO_Impl(message.serviceName, buffer, message.flags, (hasTPCONV ? serviceSession
                        : null), connection, message.len);
                log.debug("Prepared the data for passing to the service");

                hasTx = (message.control != null && message.control.length() != 0);

                log.debug("hasTx=" + hasTx + " control: " + message.control);

                if (hasTx) {
                    // make sure any foreign tx is resumed before calling
                    // the
                    // service routine
                    if(message.control.startsWith("IOR")) {
                        log.debug("resume OTS transaction");
                        JtsTransactionImple.resume(message.control);
                    } else if(message.control.startsWith("http")) {
                        log.debug("start inbound bridge");
                        inboundBridge = InboundBridgeManager.getInstance().createInboundBridge(message.control);
                        inboundBridge.start();
                    } else {
                        log.error(message.control + " is not OTS or RTS when resume the transaction");
                    }
                }

                log.debug("Invoking the XATMI service");
                Response response = null;
                try {
                    response = tpservice(tpsvcinfo);
                    log.debug("Service invoked");
                    if (!hasTPNOREPLY && response == null) {
                        log.error("Error, expected response but none returned");
                    }
                } catch (Throwable t) {
                    log.error("Service error detected", t);
                }

                if (!hasTPNOREPLY && serviceSession.getSender() != null) {
                    log.trace("Sending response");
                    if (response != null) {
                        rval = response.getRval();
                        rcode = response.getRcode();
                        if (rval != Connection.TPSUCCESS && rval != Connection.TPFAIL) {
                            rval = Connection.TPFAIL;
                        }
                    }
                    if (connection.hasOpenSessions()) {
                        rcode = Connection.TPESVCERR;
                        rval = Connection.TPFAIL;
                    }
                    if (rval == Connection.TPFAIL) {
                        if (TransactionImpl.current() != null) {
                            try {
                                TransactionImpl.current().rollback_only();
                            } catch (TransactionException e) {
                                throw new ConnectionException(Connection.TPESYSTEM,
                                        "Could not mark transaction for rollback only");
                            }
                        }
                    }

                    if (response != null) {
                        BufferImpl toSend = (BufferImpl) response.getBuffer();
                        if (toSend != null) {
                            len = toSend.getLen();
                            data = toSend.serialize();
                            type = toSend.getType();
                            subtype = toSend.getSubtype();
                        }
                        flags = response.getFlags();
                    }
                    log.debug("Will return desired message");
                } else if (!hasTPNOREPLY && serviceSession.getSender() == null) {
                    log.error("No sender avaible but message to be sent");
                    responseSendable = false;
                } else {
                    log.debug("No need to send a response");
                }
            } finally {
                if (hasTx) {
                // and suspend it again
                    if(message.control.startsWith("IOR")) {
                        log.debug("suspend OTS transaction");
                        JtsTransactionImple.suspend();
                    } else if(message.control.startsWith("http")) {
                        log.debug("inbound bridge stop");
                        if(inboundBridge != null) {
                            inboundBridge.stop();
                        }
                    } else {
                        log.error(message.control + " is not OTS or RTS when suspend the transaction");
                    }
                }

                if (responseSendable) {
                    // Even though we can provide the cd we don't as
                    // atmibroker-xatmi doesn't because tpreturn doesn't
                    serviceSession.getSender().send("", rval, rcode, data, len, 0, flags, 0, type, subtype);
                }
            }
        } finally {
            connection.close();
        }
    }
}
