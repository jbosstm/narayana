/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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

package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

public class TestResource implements XAResource
{

	public void commit (Xid id, boolean onePhase) throws XAException
	{
		System.out.println("XA_COMMIT[" + id + "]");
	}

	public void end (Xid xid, int flags) throws XAException
	{
		System.out.println("XA_END[" + xid + "] Flags=" + flags);
	}

	public void forget (Xid xid) throws XAException
	{
		System.out.println("XA_FORGET[" + xid + "]");
	}

	public int getTransactionTimeout () throws XAException
	{
		return (_timeout);
	}

	public boolean isSameRM (XAResource xares) throws XAException
	{
		return (xares.equals(this));
	}

	public int prepare (Xid xid) throws XAException
	{
		System.out.println("XA_PREPARE[" + xid + "]");

		return (XA_OK);

		// throw new XAException();
	}

	public Xid[] recover (int flag) throws XAException
	{
		System.out.println("RECOVER[" + flag + "]");
		return (null);
	}

	public void rollback (Xid xid) throws XAException
	{
		System.out.println("XA_ROLLBACK[" + xid + "]");
	}

	public boolean setTransactionTimeout (int seconds) throws XAException
	{
		_timeout = seconds;
		return (true);
	}

	public void start (Xid xid, int flags) throws XAException
	{
		System.out.println("XA_START[" + xid + "] Flags=" + flags);
	}

	protected int _timeout = 0;

}
