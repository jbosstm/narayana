/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.recovery;

import java.io.IOException;
import java.io.ObjectInputStream;

import javax.transaction.xa.XAResource;

/**
 * This is an additional recovery helper that allows clients of the transaction
 * manager to provide a deserializer for their Serializable XAResources. We need
 * this as otherwise the transaction manager may not be able to see the
 * transports classes, for instance in an application server environment.
 */
public interface SerializableXAResourceDeserializer {

	/**
	 * Can this {@link SerializableXAResourceDeserializer} handle the specified
	 * classname.
	 * 
	 * @param className
	 *            The name of the class to deserialize.
	 * 
	 * @return A flag to indicate where the deserializer is aware of the
	 *         Serializable XAResource.
	 */
	public boolean canDeserialze(String className);

	/**
	 * Deserialize the XAResource.
	 * 
	 * @param ois
	 *            The input stream to read from.
	 * @throws IOException
	 *             If the ObjectInputStream.readObject() fails.
	 * @throws ClassNotFoundException
	 *             If the ObjectInputStream.readObject() fails.
     * @return An {@link XAResource}
	 */
	public XAResource deserialze(ObjectInputStream ois) throws IOException, ClassNotFoundException;
}