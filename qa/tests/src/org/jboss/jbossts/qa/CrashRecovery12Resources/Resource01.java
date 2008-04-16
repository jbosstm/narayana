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
 * $Id: Resource01.java,v 1.1 2004/07/10 22:00:13 jcoleman Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery12Resources;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

public class Resource01
		implements XAResource, Referenceable, Serializable

{
	public static final int NOCRASH = 0;
	public static final int PREPARE = 1;
	public static final int COMMIT = 2;
	public static final int ROLLBACK = 3;

	public static final int NONE = 0;
	public static final int OUT = 1;
	public static final int ERR = 2;

	private int crashIn;
	private int recoverIn;
	private boolean recovering = false;
	;
	private String resultsFile;
	private Xid _currentXid;
	private Reference _reference;

	public Resource01(int crash, String results)
	{
		crashIn = crash;
		resultsFile = results;
		myLog(ERR, "Resource01(" + crash + ", " + results + ")");
	}

	/**
	 * @param param1 <description>
	 * @param param2 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void start(Xid xid, int flags) throws XAException
	{
		myLog(ERR, "start(" + xid + "," + flags + ")");
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
		myLog(ERR, "end(" + xid + ", " + flags + ")");
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
		myLog(ERR, "prepare(" + xid + ")");
		if (crashIn == PREPARE)
		{
			myLog(ERR, "Crashing in prepare");
			myLog(OUT, "Passed");
			System.exit(0);
		}
		if (crashIn == ROLLBACK)
		{
			throw (new XAException(XAException.XA_RBROLLBACK));
		}
		/* Crash in commit, rollback or no crash. */
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
		myLog(ERR, "commit(" + xid + ", " + onePhase + ")");
		if (crashIn == COMMIT)
		{
			myLog(ERR, "Crashing in commit");
			myLog(OUT, "Passed");
			System.exit(0);
		}
		if (recovering)
		{
			if (recoverIn == COMMIT)
			{
				myLog(ERR, "Recovery Passed");
			}
			else
			{
				myLog(ERR, "Recovery Failed");
			}
		}
	}

	/**
	 * @param param1 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void rollback(Xid xid) throws XAException
	{
		myLog(ERR, "rollback(" + xid + ")");
		if (crashIn == ROLLBACK)
		{
			myLog(ERR, "Crashing in rollback");
			myLog(OUT, "Passed");
			System.exit(0);
		}
		if (recovering)
		{
			if (recoverIn == PREPARE || recoverIn == ROLLBACK)
			{
				myLog(ERR, "Recovery Passed");
			}
			else
			{
				myLog(ERR, "Recovery Failed");
			}
		}
	}

	/**
	 * @param param1 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void forget(Xid xid) throws XAException
	{
		myLog(ERR, "forget(" + xid + ")");
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public Xid[] recover(int flag) throws XAException
	{
		myLog(ERR, "recover(" + flag + ")");
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
		myLog(ERR, "isSameRM(" + other + ")");
		return (false);
	}

	/**
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public int getTransactionTimeout() throws XAException
	{
		myLog(ERR, "getTransactionTimeout()");
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
		myLog(ERR, "setTransactionTimeout(seconds)");
		return true;
	}

	private void myLog(int where, String msg)
	{
		try
		{
			FileWriter fw = new FileWriter(resultsFile, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(msg);
			pw.flush();
			pw.close();
			fw.close();
			if (where == OUT)
			{
				System.out.println(msg);
			}
			if (where == ERR)
			{
				System.err.println(msg);
			}
		}
		catch (Throwable ex)
		{
			System.err.println("myLog() caught an exception");
			ex.printStackTrace();
		}
	}

	public void setReference(Reference _reference)
	{
		myLog(ERR, "setReference(" + _reference + ")");
		this._reference = _reference;
	}

	public Reference getReference() throws NamingException
	{
		myLog(ERR, "getReference()");
		return _reference;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		myLog(ERR, "writeObject() (Serialise)");
		out.writeInt(crashIn);
		out.writeObject(resultsFile);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		recoverIn = in.readInt();
		resultsFile = (String) in.readObject();
		recovering = true;
		myLog(ERR, "readObject() (Deserialise)");
	}
}
