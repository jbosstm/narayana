package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import java.io.Serializable;

/**
 * This structure contains the data that the client presented for processing to
 * the service during its invocation from either tpcall, tpacall or tpconnect.
 */
public interface TPSVCINFO extends Serializable {

	/**
	 * Get the name of the service the client thought it invoked
	 * 
	 * @return The name
	 */
	public String getName();

	/**
	 * Get the data
	 * 
	 * @return The data
	 */
	public Buffer getBuffer();

	/**
	 * Get the length of the buffer that was sent
	 * 
	 * @return The length of the buffer
	 */
	public int getLen();

	/**
	 * Get the flags that were issued
	 * 
	 * @return The flags
	 */
	public int getFlags();

	/**
	 * Get the connection descriptor
	 * 
	 * @return The connection descriptor
	 * @throws ConnectionException
	 */
	public Session getSession() throws ConnectionException;

	/**
	 * Get a reference to the connection that the service holds.
	 * 
	 * @return The connection
	 */
	public Connection getConnection();
}
