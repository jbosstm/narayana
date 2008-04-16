/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Resource02.java,v 1.1 2004/07/12 09:16:54 jcoleman Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery12Resources;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.IOException;
import java.io.Serializable;

public class Resource02
		implements XAResource, Referenceable, Serializable

{
	private Xid _currentXid;
	private Reference _reference;

	public Resource02()
	{
		System.err.println("Resource02()");
	}

	/**
	 * @param param1 <description>
	 * @param param2 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void start(Xid xid, int flags) throws XAException
	{
		System.err.println("start(" + xid + "," + flags + ")");
		_currentXid = xid;
	}

	/**
	 * @param param1 <description>
	 * @param param2 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void end(Xid xid, int flags) throws XAException
	{
		System.err.println("end(" + xid + ", " + flags + ")");
		_currentXid = null;
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public int prepare(Xid xid) throws XAException
	{
		System.err.println("prepare(" + xid + ")");
		return XA_OK;
	}

	/**
	 * @param param1 <description>
	 * @param param2 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void commit(Xid xid, boolean onePhase) throws XAException
	{
		System.err.println("commit(" + xid + ", " + onePhase + ")");
	}

	/**
	 * @param param1 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void rollback(Xid xid) throws XAException
	{
		System.err.println("rollback(" + xid + ")");
	}

	/**
	 * @param param1 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void forget(Xid xid) throws XAException
	{
		System.err.println("forget(" + xid + ")");
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public Xid[] recover(int flag) throws XAException
	{
		System.err.println("recover(" + flag + ")");
		return null;
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public boolean isSameRM(XAResource other) throws XAException
	{
		System.err.println("isSameRM(" + other + ")");
		return (false);
	}

	/**
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public int getTransactionTimeout() throws XAException
	{
		System.err.println("getTransactionTimeout()");
		return 10;
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public boolean setTransactionTimeout(int seconds) throws XAException
	{
		System.err.println("setTransactionTimeout(seconds)");
		return true;
	}

	public void setReference(Reference _reference)
	{
		System.err.println("setReference(" + _reference + ")");
		this._reference = _reference;
	}

	public Reference getReference() throws NamingException
	{
		System.err.println("getReference()");
		return _reference;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		System.err.println("writeObject() (Serialise)");
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		System.err.println("readObject() (Deserialise)");
	}
}
