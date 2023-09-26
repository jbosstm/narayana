/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.CrashRecovery13Impls;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.jta.xa.XidImple;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.IOException;
import java.io.Serializable;

public class ExampleXAResource
		implements XAResource, Referenceable, Serializable
{
	private final static long _sleepTime = 20000;
	private Xid _currentXid;
	private Reference _reference;

	public ExampleXAResource()
	{
		myLog("ExampleXAResource (Constructor)");

	}

	/**
	 * @param param1 <description>
	 * @param param2 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void start(Xid xid, int flags) throws XAException
	{
		myLog("start");
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
		myLog("end");
		_currentXid = null;
	}

	/**
	 * @param param1 <description>
	 * @param param2 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void commit(Xid xid, boolean onePhase) throws XAException
	{
		myLog("commit,xid=" + xid + ",onePhase=" + onePhase);
		mySleep(_sleepTime);
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public int prepare(Xid xid) throws XAException
	{
		myLog("prepare");
		int i = 2;
		if (i == 1)
		{
			throw (new XAException(XAException.XA_RBROLLBACK));
		}

		return XA_OK;
	}

	/**
	 * @param param1 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void rollback(Xid xid) throws XAException
	{
		if (xid.equals(toRecover))
		{
			passed = true;
		}

		myLog("rollback");
	}

	/**
	 * @param param1 <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public void forget(Xid xid) throws XAException
	{
		myLog("forget");
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public Xid[] recover(int flag) throws XAException
	{
		myLog("recover");

		Xid[] xids = new Xid[2];

		if (ExampleXAResource.toRecover == null)
		{
			AtomicAction a = new AtomicAction();

			ExampleXAResource.toRecover = new XidImple(new AtomicAction());
		}

		xids[0] = ExampleXAResource.toRecover;
		xids[1] = new XidImple(new AtomicAction());

		return xids;
	}

	/**
	 * @param param1 <description>
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public boolean isSameRM(XAResource other) throws XAException
	{
		myLog("isSameRM");
		return (false);
	}

	/**
	 * @return <description>
	 * @throws javax.transaction.xa.XAException
	 *          <description>
	 */
	public int getTransactionTimeout() throws XAException
	{
		myLog("getTransactionTimeout");
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
		myLog("setTransactionTimeout");
		return true;
	}

	private void myLog(String msg)
	{
		/*
			try
			{
			  String fileName = "ExampleXAResource.log";
			  FileWriter fw = new FileWriter(fileName, true);
			  PrintWriter pw = new PrintWriter(fw);
			  pw.println(msg);
			  pw.flush();
			  pw.close();
			  fw.close();
				System.out.println("ExampleResource: "+msg);
			}
			catch (Throwable ex)
			{
			  System.out.println("Caught an exception");
			  ex.printStackTrace();
			}
			  */

	}

	private void mySleep(long millis)
	{
		myLog("Sleeping " + millis + " milliseconds");
		try
		{
			Thread.sleep(millis);
		}
		catch (Exception ex)
		{
		}
		myLog("Sleep complete");
	}

	public void setReference(Reference _reference)
	{
		myLog("setReference, _reference=" + _reference);
		this._reference = _reference;
	}

	public Reference getReference() throws NamingException
	{
		myLog("getReference");
		return _reference;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException
	{
		myLog("writeObject (Serialized)");
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		myLog("readObject (Deserialized");
	}

	public static boolean passed = false;

	private static Xid toRecover = null;

}