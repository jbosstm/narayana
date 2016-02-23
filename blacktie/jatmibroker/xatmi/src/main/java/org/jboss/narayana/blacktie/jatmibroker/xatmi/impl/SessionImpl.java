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
package org.jboss.narayana.blacktie.jatmibroker.xatmi.impl;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.EventListener;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Receiver;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Sender;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Transport;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionException;
import org.jboss.narayana.blacktie.jatmibroker.core.tx.TransactionImpl;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ResponseException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Session;

/**
 * A session reference may either be obtained from the tpconnect
 * <code>Connection</code> invocation for a client or retrieved from the
 * TPSVCINFO structure for a service (assuming the service was invoked within
 * the scope of a tpconnect).
 * 
 * It is used to send and retrieve data: 
 * ConnectionImpl#tpconnect(String, BufferImpl, int, int)
 * TPSVCINFO_Impl#getSession()
 */
public class SessionImpl implements Session {
    /**
     * A logger to use.
     */
    private static final Logger log = LogManager.getLogger(SessionImpl.class);

    /**
     * The transport to manage data on
     */
    private Transport transport;

    /**
     * The descriptor
     */
    private int cd;

    /**
     * The sessions sender
     */
    private Sender sender;

    /**
     * The sessions receiver
     */
    private Receiver receiver;

    /**
     * The event listener allows us to hear events on tpsend
     */
    private EventListener eventListener;

    /**
     * The last event received by the session, this is either discon, SUCC,
     * FAIL, ERR
     */
    private long lastEvent = -1;

    /**
     * The last rcode
     */
    private int lastRCode = 0;

    /**
     * Is the session in read mode.
     */
    private boolean canSend = true;

    /**
     * Is the session in write mode, so to speak.
     */
    private boolean canRecv = true;

    /**
     * The connection to use.
     */
    private ConnectionImpl connection;

    private boolean closed;

    /**
     * Create a new session during tpconnect.
     * 
     * @param connection
     *            The connection that created the session
     * @param transport
     *            The transport to create actors on.
     * @param cd
     *            The cd of the session
     * 
     * @throws ConnectionException
     *             In case the receiver cannot be created.
     * @see ConnectionImpl#tpconnect(String, BufferImpl, int, int)
     */
    SessionImpl(ConnectionImpl connection, String serviceName,
            Transport transport, int cd) throws ConnectionException {
        log.debug("Creating a new client session: " + cd);
        this.connection = connection;
        this.transport = transport;
        this.cd = cd;
        this.eventListener = new EventListenerImpl(this);
        this.receiver = transport.createReceiver(cd, null, eventListener);
        this.sender = transport.getSender(serviceName, true);

        this.canSend = false;
        this.canRecv = true;
    }

    /**
     * Create a service side session for a pre-established client connection.
     * 
     * @param connection
     *            The connection to use.
     * @param transport
     *            The transport to use.
     * @param cd
     *            The connection descriptor to use.
     * @param replyTo
     *            The client to reply to.
     * @throws ConnectionException
     *             In case the receiver or sender cannot be established.
     * @see ConnectionImpl#createServiceSession(String, int, Object)
     */
    SessionImpl(ConnectionImpl connection, Transport transport, int cd,
            Object replyTo) throws ConnectionException {
        log.debug("Connecting a client session for the service: " + cd);
        this.connection = connection;
        this.transport = transport;
        this.cd = cd;
        this.eventListener = new EventListenerImpl(this);

        if (replyTo != null && !replyTo.equals("")) {
            this.sender = transport.createSender(replyTo);
        } else {
            log.trace("NO REPLY TO REQUIRED");
        }
        this.receiver = transport.createReceiver(this.sender);

        this.canRecv = false;
        this.canSend = true;
    }

    /**
     * Client side initialization during tpconnect.
     * 
     * @param flags
     *            The flags
     */
    void setCreatorState(long flags) {
        // Sort out session state
        if ((flags & ConnectionImpl.TPSENDONLY) == ConnectionImpl.TPSENDONLY) {
            canSend = true;
            canRecv = false;
        } else if ((flags & ConnectionImpl.TPRECVONLY) == ConnectionImpl.TPRECVONLY) {
            canSend = false;
            canRecv = true;
        }
    }

    /**
     * Set the state of the session using the flags. This is so we can respond
     * from the service easily with the <code>ACK</code> that the client expects
     * in initialization of the connection.
     * 
     * @param flags
     */
    public void setCreatedState(long flags) {
        // Sort out session state
        if ((flags & ConnectionImpl.TPSENDONLY) == ConnectionImpl.TPSENDONLY) {
            canSend = false;
            canRecv = true;
        } else if ((flags & ConnectionImpl.TPRECVONLY) == ConnectionImpl.TPRECVONLY) {
            canSend = true;
            canRecv = false;
        }
    }

    /**
     * Close the session
     * 
     * @throws ConnectionException
     */
    public void close() throws ConnectionException {
        log.debug("Closing session: " + cd);
        if (closed) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session already closed");
        }
        if (sender != null) {
            log.debug("Sender closing");
            sender.close();
            sender = null;
        }
        if (receiver != null) {
            log.debug("Receiver closing");
            receiver.close();
            receiver = null;
        }
        connection.removeSession(this);
        closed = true;
        log.debug("Closed session: " + cd);
    }

    /**
     * Send a buffer to a remote server in a conversation
     * 
     * @param toSend
     *            The outbound data
     * @param flags
     *            The flags to use
     * @throws ConnectionException
     *             If the message cannot be sent.
     */
    public int tpsend(Buffer toSend, int flags) throws ConnectionException {
        log.debug("tpsend invoked: " + cd);
        if (closed) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session already closed");
        }
        int toReturn = -1;

        int toCheck = flags
                & ~(ConnectionImpl.TPRECVONLY | ConnectionImpl.TPNOBLOCK
                        | ConnectionImpl.TPNOTIME | ConnectionImpl.TPSIGRSTRT);
        if (toCheck != 0) {
            log.trace("invalid flags remain: " + toCheck);
            throw new ConnectionException(ConnectionImpl.TPEINVAL,
                    "Invalid flags remain: " + toCheck);
        }

        if (this.lastEvent > -1) {
            throw new ResponseException(ConnectionImpl.TPEEVENT,
                    "Event existed on descriptor: " + lastEvent, lastEvent,
                    lastRCode, null);
        } else if (!canSend) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session can't send");
        }
        // Can only send in certain circumstances
        if (sender != null) {
            log.debug("Sender not null, sending");
            String type = null;
            String subtype = null;
            byte[] data = null;
            int len = 0;
            if (toSend != null) {
                data = ((BufferImpl) toSend).serialize();
                type = toSend.getType();
                subtype = toSend.getSubtype();
                len = toSend.getLen();
            }

            sender.send(receiver.getReplyTo(), (short) 0, 0, data, len, cd,
                    flags, 0, type, subtype);

            // Sort out session state
            if ((flags & ConnectionImpl.TPRECVONLY) == ConnectionImpl.TPRECVONLY) {
                canSend = false;
                canRecv = true;
            }

            toReturn = 0;
        } else {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session in receive mode");
        }
        return toReturn;
    }

    /**
     * Received the next response in a conversation
     * 
     * @param flags
     *            The flags to use
     * @return The next response
     * @throws ConnectionException
     *             If the message cannot be received or the flags are incorrect
     * @throws ConfigurationException
     */
    public Buffer tprecv(int flags) throws ConnectionException,
    ConfigurationException {
        log.debug("Receiving: " + cd);
        if (closed) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session already closed");
        }

        int toCheck = flags
                & ~(ConnectionImpl.TPNOCHANGE | ConnectionImpl.TPNOBLOCK
                        | ConnectionImpl.TPNOTIME | ConnectionImpl.TPSIGRSTRT);
        if (toCheck != 0) {
            log.trace("invalid flags remain: " + toCheck);
            throw new ConnectionException(ConnectionImpl.TPEINVAL,
                    "Invalid flags remain: " + toCheck);
        }

        if (!canRecv) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session can't receive");
        }
        Message m = receiver.receive(flags);
        // Prepare the outbound channel
        if (m.replyTo == null
                || (sender != null && !m.replyTo.equals(sender.getSendTo()))) {
            log.trace("Send to location has altered");
            sender.close();
            sender = null;
        }
        if (sender == null && m.replyTo != null && !m.replyTo.equals("")) {
            log.trace("Will require a new sender");
            if(((String)m.replyTo).contains("IOR:")) {
                sender = transport.createSender(m.replyTo);
            } else {
                sender = transport.createSender(receiver);
            }
        } else {
            log.debug("Not setting the sender");
        }

        BufferImpl received = null;
        if (m.type != null && !m.type.equals("")) {
            received = (BufferImpl) connection
                    .tpalloc(m.type, m.subtype);
            received.deserialize(m.data);
        }
        log.debug("Prepared and ready to launch");

        // Sort out session state
        if ((m.flags & ConnectionImpl.TPRECVONLY) == ConnectionImpl.TPRECVONLY) {
            canSend = true;
            canRecv = false;
        }

        // Check the condition of the response
        if ((m.flags & ConnectionImpl.TPRECVONLY) == ConnectionImpl.TPRECVONLY) {
            throw new ResponseException(ConnectionImpl.TPEEVENT,
                    "Reporting send only event", ConnectionImpl.TPEV_SENDONLY,
                    m.rcode, received);
        } else if (m.rval == EventListener.DISCON_CODE) {
            close();
            throw new ResponseException(ConnectionImpl.TPEEVENT,
                    "Received a disconnect event",
                    ConnectionImpl.TPEV_DISCONIMM, m.rcode, received);
        } else if (m.rval == ConnectionImpl.TPSUCCESS
                || m.rval == ConnectionImpl.TPFAIL) {
            log.debug("Completed session is being closed: " + cd);
            close();
            if (m.rval == ConnectionImpl.TPSUCCESS) {
                throw new ResponseException(ConnectionImpl.TPEEVENT,
                        "Service completed successfully event",
                        ConnectionImpl.TPEV_SVCSUCC, 0, received);
            } else if (m.rcode == ConnectionImpl.TPESVCERR) {
                throw new ResponseException(ConnectionImpl.TPEEVENT,
                        "Service received an error",
                        ConnectionImpl.TPEV_SVCERR, m.rcode, received);
            } else {
                throw new ResponseException(ConnectionImpl.TPEEVENT,
                        "Service received a fail", ConnectionImpl.TPEV_SVCFAIL,
                        m.rcode, received);
            }
        }
        return received;
    }

    /**
     * Close the conversation with the remote service. This will close the
     * session.
     */
    public void tpdiscon() throws ConnectionException {
        log.debug("tpdiscon: " + cd);
        if (closed) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session already closed");
        }
        if (sender == null) {
            throw new ConnectionException(ConnectionImpl.TPEPROTO,
                    "Session had no endpoint to respond to for tpdiscon");
        }
        if (TransactionImpl.current() != null) {
            try {
                TransactionImpl.current().rollback_only();
            } catch (TransactionException e) {
                throw new ConnectionException(ConnectionImpl.TPESYSTEM,
                        "Could not mark transaction for rollback only");
            }
        }
        try {
            sender.send("", EventListener.DISCON_CODE, 0, null, 0, cd, 0, 0,
                    null, null);
        } catch (org.omg.CORBA.OBJECT_NOT_EXIST one) {
            log.warn("The disconnect called failed to notify the remote end");
            log.debug("The disconnect called failed to notify the remote end",
                    one);
        }
        close();
    }

    /**
     * Return the connection descriptor
     * 
     * @return The connection descriptor id.
     */
    int getCd() {
        return cd;
    }

    /**
     * Get the receiver on this session.
     * 
     * @return The receiver
     */
    Receiver getReceiver() {
        return receiver;
    }

    /**
     * Get the sessions sender.
     * 
     * @return The sender
     */
    public Sender getSender() {
        return sender;
    }

    /**
     * Set the last event seen on this session.
     * 
     * @param lastEvent
     *            The last event
     * @param rcode
     *            The last rcode
     */
    private void setLastEvent(long lastEvent, int rcode) {
        log.debug("Set lastEvent: " + lastEvent + "lastRCode: " + lastRCode
                + " cd: " + cd);
        this.lastEvent = lastEvent;
        this.lastRCode = rcode;
    }

    /**
     * A listener for events.
     */
    private class EventListenerImpl implements EventListener {

        /**
         * The session to return events to.
         */
        private SessionImpl session;

        /**
         * Create a new listener with a session to set events on.
         * 
         * @param session
         *            The session.
         */
        public EventListenerImpl(SessionImpl session) {
            this.session = session;
        }

        /**
         * Pass the last event through to the session.
         */
        public void setLastEvent(long lastEvent, int rcode) {
            session.setLastEvent(lastEvent, rcode);
        }
    }

    Transport getTransport() {
        return transport;
    }
}
