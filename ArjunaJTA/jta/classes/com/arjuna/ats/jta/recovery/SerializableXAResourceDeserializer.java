/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
