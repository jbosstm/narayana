package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.AtmiBrokerEnvXML;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.ConnectionImpl;

/**
 * This is a factory that will create connections to remote Blacktie services.
 * 
 * @see Connection
 * @see ConnectionException
 */
public class ConnectionFactory {
	private static final Logger log = LogManager
			.getLogger(ConnectionFactory.class);

	/**
	 * The properties inside the connection factory.
	 */
	private Properties properties = new Properties();

	/**
	 * The connection factory will allocate a connection per thread.
	 */
	private static ThreadLocal<ConnectionImpl> connections = new ThreadLocal<ConnectionImpl>();

	/**
	 * Get the default connection factory
	 * 
	 * @return The connection factory
	 * @throws ConfigurationException
	 *             If the configuration cannot be parsed.
	 */
	public static synchronized ConnectionFactory getConnectionFactory()
			throws ConfigurationException {
		return new ConnectionFactory();
	}

	/**
	 * Create the connection factory
	 * 
	 * @throws ConfigurationException
	 *             In case the configuration could not be loaded
	 */
	private ConnectionFactory() throws ConfigurationException {
		log.debug("Creating connection factory: " + this);
		AtmiBrokerEnvXML xml = new AtmiBrokerEnvXML();
		properties.putAll(xml.getProperties());
		log.debug("Created connection factory: " + this);
	}

	/**
	 * Get the connection for this thread.
	 * 
	 * @return The connection for this thread.
	 * @throws ConfigurationException
	 */
	public Connection getConnection() throws ConfigurationException {
		ConnectionImpl connection = connections.get();
		if (connection == null) {
			connection = new ConnectionImpl(this, properties);
			connections.set(connection);
			log.debug("Returning connection: " + connection);
		}
		return connection;
	}

	/**
	 * Remove the connection from the factory after closure.
	 * 
	 * @param connection
	 *            The connection to remove.
	 */
	public void removeConnection(ConnectionImpl connection) {
		connections.set(null);
	}

	public Properties getProperties() {
		return properties;
	}
}
