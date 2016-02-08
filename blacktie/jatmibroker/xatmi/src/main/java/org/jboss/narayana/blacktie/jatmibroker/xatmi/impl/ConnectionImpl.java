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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.codec.CodecFactory;
import org.jboss.narayana.blacktie.jatmibroker.core.ResponseMonitor;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Codec;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Receiver;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Transport;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.TransportFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ResponseException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Session;

/**
 * This is the connection to remote BlackTie services. It must be created using
 * the ConnectionFactory#getConnection()
 */
public class ConnectionImpl implements Connection {

	/**
	 * The logger to use.
	 */
	private static final Logger log = LogManager
			.getLogger(ConnectionImpl.class);

	/**
	 * The next id to use for session connection descriptors.
	 */
	private static int nextId;

	/**
	 * The transports created for each service.
	 */
	private Map<String, Transport> transports = new HashMap<String, Transport>();

	/**
	 * Any local temporary queues created in this connection
	 */
	private Map<java.lang.Integer, Receiver> temporaryQueues = new HashMap<java.lang.Integer, Receiver>();

	/**
	 * The properties that this connection was created with.
	 */
	Properties properties;

	/**
	 * The open sessions from this connection/
	 */
	private Map<Integer, SessionImpl> sessions = new HashMap<Integer, SessionImpl>();

	/**
	 * The list of sessionids that have received a message.
	 */
	private List<Integer> tpGetAnySessions = new ArrayList<Integer>();

	/**
	 * The monitor of responses.
	 */
	private ResponseMonitor responseMonitor;

	/**
	 * The connection factory that created this connection.
	 */
	private ConnectionFactory connectionFactory;

	/**
	 * The service sessions for this connection.
	 */
	private SessionImpl serviceSession;

	private TransportFactory transportFactory;

	/**
	 * The connection
	 * 
	 * @param connectionFactory
	 *            The connection factory that created this connection.
	 * @param properties
	 *            The properties that this connection was created with.
	 * @throws ConfigurationException
	 */
	public ConnectionImpl(ConnectionFactory connectionFactory,
			Properties properties) throws ConfigurationException {
		log.debug("Creating connection: " + this);
		this.connectionFactory = connectionFactory;
		this.properties = properties;
		responseMonitor = new ResponseMonitorImpl();
		transportFactory = new TransportFactory(properties);
	}

	/**
	 * Allocate a new buffer
	 * 
	 * @param type
	 *            The type of the buffer
	 * @param subtype
	 *            The subtype of the buffer
	 * @return The new buffer
	 * @throws ConnectionException
	 *             If the buffer was unknown or invalid.
	 * @throws ConfigurationException
	 */
	public Buffer tpalloc(String type, String subtype)
			throws ConnectionException, ConfigurationException {
		if (type == null) {
			throw new ConnectionException(ConnectionImpl.TPEINVAL,
					"No type provided");
		} else {
			log.debug("Initializing a new: " + type);
			try {
				Class clazz = Class.forName(getClass().getPackage().getName() + "."
						+ type + "_Impl");
				Constructor ctor = clazz.getConstructor(String.class);
				return (Buffer) ctor.newInstance(subtype);
			} catch (InvocationTargetException t) {
				if (t.getCause() instanceof ConfigurationException) {
					throw ((ConfigurationException)t.getCause());
				}
				throw new ConnectionException(ConnectionImpl.TPENOENT,
						"Type was not known: " + type, t);
			} catch (Throwable t) {
				throw new ConnectionException(ConnectionImpl.TPENOENT,
						"Type was not known: " + type, t);
			}
		}
	}

	/**
	 * Synchronous call.
	 * 
	 * @param svc
	 *            The name of the service to call
	 * @param buffer
	 *            The inbound data
	 * @param flags
	 *            The flags to use
	 * @return The returned buffer
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 * @throws ConfigurationException
	 */
	public Response tpcall(String svc, Buffer buffer, int flags)
			throws ConnectionException, ConfigurationException {
		log.debug("tpcall");
		int tpacallFlags = flags;
		tpacallFlags &= ~TPNOCHANGE;
		int cd = tpacall(svc, buffer, tpacallFlags);
		try {
			return receive(cd, flags);
		} finally {
			tpcancel(cd);
		}
	}

	/**
	 * Asynchronous call
	 * 
	 * @param svc
	 *            The name of the service to call
	 * @param toSend
	 *            The inbound data
	 * @param flags
	 *            The flags to use
	 * @return The connection descriptor
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 */
	public int tpacall(String svc, Buffer toSend, int flags)
			throws ConnectionException {
		log.debug("tpacall");
		int toCheck = flags
				& ~(TPNOTRAN | TPNOREPLY | TPNOBLOCK | TPNOTIME | TPSIGRSTRT);
		if (toCheck != 0) {
			log.trace("invalid flags remain: " + toCheck);
			throw new ConnectionException(ConnectionImpl.TPEINVAL,
					"Invalid flags remain: " + toCheck);
		}

		svc = svc.substring(0, Math.min(
				ConnectionImpl.XATMI_SERVICE_NAME_LENGTH, svc.length()));

		String qtype = (String) properties.get("blacktie." + svc + ".type");

		log.debug(svc + " qtype is " + qtype + " and flags is " + flags);
		if ("topic".equals(qtype) && (flags & TPNOREPLY) == 0) {
			log.warn(svc + " type is " + qtype + " and MUST have TPNOREPLY set");
			throw new ConnectionException(ConnectionImpl.TPEINVAL, svc
					+ " type is " + qtype + " and MUST have TPNOREPLY set");
		}
		int correlationId = 0;
		synchronized (this) {
			correlationId = ++nextId;
			log.trace("Allocated next sessionId: " + correlationId);
		}
		Transport transport = getTransport(svc);
		Receiver endpoint = transport.createReceiver(correlationId,
				responseMonitor, null);
		temporaryQueues.put(correlationId, endpoint);
		log.trace("Added a queue for: " + correlationId);
		// TODO HANDLE TRANSACTION
		String type = null;
		String subtype = null;
		int len = 0;
		byte[] data = null;
		if (toSend != null) {
			CodecFactory factory = new CodecFactory(this);
			String coding_type = properties.getProperty("blacktie." + svc
					+ ".coding_type");
			Codec codec = factory.getCodec(coding_type);
			data = codec.encode((BufferImpl) toSend);
			// data = toSend.serialize();
			type = toSend.getType();
			subtype = toSend.getSubtype();
			len = toSend.getLen();
		}

		String timeToLive = properties.getProperty("TimeToLive");
		int ttl = 0;

		// Don't set ttl when tpacall and TPNOREPLY set
		if (timeToLive != null
				&& ((flags & ConnectionImpl.TPNOREPLY) != ConnectionImpl.TPNOREPLY)) {
			ttl = Integer.parseInt(timeToLive) * 1000;
			log.debug("Set ttl: " + ttl);
		}
		transport.getSender(svc, false).send(endpoint.getReplyTo(), (short) 0,
				0, data, len, correlationId, flags, ttl, type, subtype);
		if ((flags & ConnectionImpl.TPNOREPLY) == ConnectionImpl.TPNOREPLY) {
			correlationId = 0;
		}
		log.debug("Returning cd: " + correlationId);
		return correlationId;
	}

	/**
	 * Cancel the outstanding asynchronous call.
	 * 
	 * @param cd
	 *            The connection descriptor
	 * @throws ConnectionException
	 *             If the client cannot be cleaned up.
	 */
	public int tpcancel(int cd) throws ConnectionException {
		log.debug("tpcancel: " + cd);
		int toReturn = -1;
		Receiver endpoint = temporaryQueues.remove(cd);
		if (endpoint != null) {
			log.debug("closing endpoint");
			endpoint.close();
			log.debug("endpoint closed");
			toReturn = 0;
		} else {
			log.debug("No endpoint available");
			throw new ConnectionException(ConnectionImpl.TPEBADDESC, "cd " + cd
					+ " does not exist");
		}
		log.debug("tpcancel returning: " + toReturn);
		return toReturn;
	}

	/**
	 * Get the reply for an asynchronous call.
	 * 
	 * @param cd
	 *            The connection descriptor to use
	 * @param flags
	 *            The flags to use
	 * @return The response from the server
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 * @throws ConfigurationException
	 */
	public Response tpgetrply(int cd, int flags)
			throws ConnectionException, ConfigurationException {
		log.debug("tpgetrply: " + cd);
		int toCheck = flags
				& ~(TPGETANY | TPNOCHANGE | TPNOBLOCK | TPNOTIME | TPSIGRSTRT);
		if (toCheck != 0) {
			log.trace("invalid flags remain: " + toCheck);
			throw new ConnectionException(ConnectionImpl.TPEINVAL,
					"Invalid flags remain: " + toCheck);
		}

		synchronized (tpGetAnySessions) {
			if ((flags & ConnectionImpl.TPGETANY) == ConnectionImpl.TPGETANY) {
				if ((flags & ConnectionImpl.TPNOBLOCK) != ConnectionImpl.TPNOBLOCK) {
					int timeout = 0;
					if ((flags & ConnectionImpl.TPNOTIME) != ConnectionImpl.TPNOTIME) {
						timeout = Integer.parseInt(properties
								.getProperty("ReceiveTimeout"))
								* 1000
								+ Integer.parseInt(properties
										.getProperty("TimeToLive")) * 1000;
					}
					if (tpGetAnySessions.size() == 0) {
						try {
							tpGetAnySessions.wait(timeout);
						} catch (InterruptedException e) {
							throw new ConnectionException(
									ConnectionImpl.TPESYSTEM, "Could not wait",
									e);
						}
					}
					if (tpGetAnySessions.size() == 0) {
						throw new ConnectionException(ConnectionImpl.TPETIME,
								"No message arrived");
					}
				} else if (tpGetAnySessions.size() == 0) {
					throw new ConnectionException(ConnectionImpl.TPEBLOCK,
							"No message arrived");
				}
				cd = tpGetAnySessions.remove(0);
			}
		}

		Response toReturn = receive(cd, flags);
		tpcancel(cd);
		log.debug("tpgetrply returning: " + toReturn);
		return toReturn;
	}

	/**
	 * Handle the initiation of a conversation with the server.
	 * 
	 * @param svc
	 *            The name of the service
	 * @param toSend
	 *            The outbound buffer
	 * @param flags
	 *            The flags to use
	 * @return The connection descriptor
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 */
	public Session tpconnect(String svc, Buffer toSend, int flags)
			throws ConnectionException {
		log.debug("tpconnect: " + svc);

		svc = svc.substring(0, Math.min(
				ConnectionImpl.XATMI_SERVICE_NAME_LENGTH, svc.length()));
		// Initiate the session
		svc = svc.substring(0, Math.min(
				ConnectionImpl.XATMI_SERVICE_NAME_LENGTH, svc.length()));
		int correlationId = 0;
		synchronized (this) {
			correlationId = nextId++;
		}
		Transport transport = getTransport(svc);
		SessionImpl session = new SessionImpl(this, svc, transport,
				correlationId);

		Receiver receiver = session.getReceiver();
		// TODO HANDLE TRANSACTION
		String type = null;
		String subtype = null;
		int len = 0;
		byte[] data = null;
		if (toSend != null) {
			CodecFactory factory = new CodecFactory(this);
			String coding_type = properties.getProperty("blacktie." + svc
					+ ".coding_type");
			Codec codec = factory.getCodec(coding_type);
			data = codec.encode((BufferImpl) toSend);
			// data = toSend.serialize();
			type = toSend.getType();
			subtype = toSend.getSubtype();
			len = toSend.getLen();
		}

		String timeToLive = properties.getProperty("TimeToLive");
		int ttl = 0;

		if (timeToLive != null) {
			ttl = Integer.parseInt(timeToLive) * 1000;
		}
		log.debug("tpconnect sending data");
		session.getSender().send(receiver.getReplyTo(), (short) 0, 0, data,
				len, correlationId, flags | TPCONV, ttl, type, subtype);

		byte[] response = null;
		try {
			log.debug("tpconnect receiving data");
			X_OCTET_Impl odata = (X_OCTET_Impl) session.tprecv(0);
			// TODO THIS SHOULD BE A BETTER ERROR AND CHECKED IF TPCONV AND NOT
			// CONV
			response = odata.getByteArray();
			log.debug("tpconnect received data");
		} catch (ResponseException e) {
			response = ((X_OCTET_Impl) e.getReceived()).getByteArray();
			log.debug("Caught an exception with data", e);
		} catch (ConnectionException e) {
			session.close();
			throw new ConnectionException(e.getTperrno(), "Could not connect");
		} catch (ConfigurationException e) {
			session.close();
			throw new ConnectionException(ConnectionImpl.TPEOS,
					"Configuration exception: " + e.getMessage(), e);
		}
		byte[] ack = new byte[4];
		byte[] bytes = "ACK".getBytes();
		System.arraycopy(bytes, 0, ack, 0, 3);
		boolean connected = response == null ? false : Arrays.equals(ack,
				response);
		if (!connected) {
			log.error("Could not connect");
			session.close();
			throw new ConnectionException(ConnectionImpl.TPESYSTEM,
					"Could not connect");
		}
		session.setCreatorState(flags);
		sessions.put(correlationId, session);
		log.trace("Added session: " + correlationId);

		// Return a handle to allow the connection to send/receive data on
		log.debug("Created session: " + correlationId);
		return session;
	}

	/**
	 * Close any resources associated with this connection
	 * 
	 * @throws ConnectionException
	 *             If an open session cannot be cancelled or disconnected.
	 */
	public void close() throws ConnectionException {
		log.debug("Close connection called: " + this);

		// MUST close the session first to remove the temporary queue
		SessionImpl[] sessions = new SessionImpl[this.sessions.size()];
		sessions = this.sessions.values().toArray(sessions);
		for (int i = 0; i < sessions.length; i++) {
			log.debug("closing session: " + sessions[i].getCd());
			sessions[i].tpdiscon();
			log.debug("Closed open session: " + sessions[i].getCd());
		}
		this.sessions.clear();
		log.trace("Removed all sessions");

		Receiver[] receivers = new Receiver[temporaryQueues.size()];
		receivers = temporaryQueues.values().toArray(receivers);
		for (int i = 0; i < receivers.length; i++) {
			log.debug("closing receiver");
			tpcancel(receivers[i].getCd());
			log.debug("Closed open receiver");
		}
		temporaryQueues.clear();
		log.trace("Temporary queues cleared");

		if (serviceSession != null) {
			log.debug("closing service session");
			serviceSession.close();
			serviceSession = null;
			log.debug("Closed open service session");
		}

		Iterator<Transport> transports = this.transports.values().iterator();
		while (transports.hasNext()) {
			Transport transport = transports.next();
			log.debug("closing transport");
			transport.close();
			log.debug("closed transport");
		}
		this.transports.clear();
		this.connectionFactory.removeConnection(this);
		transportFactory.close();
		log.debug("Close connection finished");
	}

	private Transport getTransport(String serviceName)
			throws ConnectionException {
		Transport toReturn = transports.get(serviceName);
		if (toReturn == null) {
			toReturn = transportFactory.createTransport();
			transports.put(serviceName, toReturn);
		}
		return toReturn;
	}

	/**
	 * Retrieve a response.
	 * 
	 * @param cd
	 *            The connection descriptor
	 * @param flags
	 *            The flags to use
	 * @return The response
	 * @throws ConnectionException
	 *             If the response cannot be retrieved.
	 * @throws ConfigurationException
	 */
	private Response receive(int cd, int flags) throws ConnectionException,
			ConfigurationException {
		log.debug("receive: " + cd);
		Receiver endpoint = temporaryQueues.get(cd);
		if (endpoint == null) {
			throw new ConnectionException(ConnectionImpl.TPEBADDESC,
					"Session does not exist: " + cd);
		}
		Message message = endpoint.receive(flags);
		Buffer buffer = null;
		if (message.type != null && !message.type.equals("")) {
			CodecFactory factory = new CodecFactory(this);
			String coding_type = properties.getProperty("blacktie."
					+ message.serviceName + ".coding_type");
			Codec codec = factory.getCodec(coding_type);
			buffer = codec.decode(message.type, message.subtype, message.data,
					message.len);
			// buffer = tpalloc(message.type, message.subtype, message.len);
			// buffer.deserialize(message.data);
		}
		if (message.rval == ConnectionImpl.TPFAIL) {
			if (message.rcode == ConnectionImpl.TPESVCERR) {
				throw new ResponseException(ConnectionImpl.TPESVCERR,
						"Got an error back from the remote service", -1,
						message.rcode, buffer);
			}
			throw new ResponseException(ConnectionImpl.TPESVCFAIL,
					"Got a fail back from the remote service", -1,
					message.rcode, buffer);
		} else {
			Response response = new Response(cd, message.rval,
					message.rcode, buffer, message.flags);

			log.debug("received returned a response? "
					+ (response == null ? "false" : "true"));
			return response;
		}
	}

	/**
	 * Used by the service side to create a session for handling the client
	 * request.
	 * 
	 * @param name
	 *            The name of the service.
	 * @param cd
	 *            The connection descriptor
	 * @param replyTo
	 *            The client to respond to
	 * @return The session to use for the service invocation
	 * @throws ConnectionException
	 *             In case the transport cannot be established.
	 */
	public SessionImpl createServiceSession(String name, int cd, Object replyTo)
			throws ConnectionException {
		log.trace("Creating the service session");
		if (serviceSession != null) {
			throw new ConnectionException(ConnectionImpl.TPEPROTO,
					"Second service session creation attempt, was: "
							+ serviceSession.getCd() + " new: " + cd);
		}
		Transport transport = getTransport(name);
		serviceSession = new SessionImpl(this, transport, cd, replyTo);
		log.trace("Created the service session: " + cd);
		return serviceSession;
	}

	/**
	 * Does this connection have any open sessions? Used to determine if a
	 * service has unanswered requests prior to a tpreturn.
	 * 
	 * @return True, if there are open conversations or asynchronous XATMI calls
	 *         open.
	 */
	public boolean hasOpenSessions() {
		return sessions.size() > 0 || temporaryQueues.size() > 0;
	}

	/**
	 * Detach the open session, called during {@link SessionImpl#close()}
	 * 
	 * @param session
	 *            The session that is closing.
	 */
	void removeSession(SessionImpl session) {
		log.debug("Removing session: " + session.getCd());
		// May be a no-op
		boolean remove = false;
		Iterator<Integer> iterator = sessions.keySet().iterator();
		while (iterator.hasNext()) {
			Integer next = iterator.next();
			if (next.intValue() == session.getCd()) {
				iterator.remove();
				log.trace("Removed session: " + session.getCd());
				remove = true;
				break;
			} else {
				log.trace("Ignoring: " + next);
			}
		}
		if (!remove) {
			log.debug("Session did not exist: " + session.getCd() + " size: "
					+ sessions.size());
		}

		if (session.equals(serviceSession)) {
			serviceSession = null;
		}
		log.debug("Removed session: " + session.getCd());
	}

	/**
	 * This class allows the session to notify the connection when a response is
	 * delivered or consumed.
	 */
	private class ResponseMonitorImpl implements ResponseMonitor {
		/**
		 * Handle the response update.
		 */
		public void responseReceived(int sessionId, boolean remove) {
			synchronized (tpGetAnySessions) {
				if (!remove) {
					log.trace("tpgetanyCallback adding: " + sessionId);
					tpGetAnySessions.add(sessionId);
					tpGetAnySessions.notify();
				} else {
					log.trace("tpgetanyCallback removing: " + sessionId);
					for (int i = 0; i < tpGetAnySessions.size(); i++) {
						if (sessionId == tpGetAnySessions.get(i)) {
							tpGetAnySessions.remove(i);
							log.trace("tpgetanyCallback removed: " + sessionId);
							break;
						}
					}
				}
			}
		}
	}
}
