/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.astests.ASCrashRecovery01;

import org.jboss.jbossts.qa.astests.taskdefs.ClientAction;
import org.jboss.jbossts.qa.astests.recovery.ASFailureSpec;
import org.jboss.jbossts.qa.astests.taskdefs.ASTestConfig;
import org.jboss.jbossts.qa.astests.taskdefs.TransactionLog;
import org.jboss.jbossts.qa.astests.crash.CrashRem;
import org.jboss.jbossts.qa.astests.crash.CrashRemHome;
import org.jboss.remoting.CannotConnectException;
import org.apache.tools.ant.BuildException;

import java.util.Map;
import java.io.*;

import javax.rmi.PortableRemoteObject;
import javax.transaction.*;
import javax.naming.NamingException;

public class Test03 implements ClientAction
{
	// the longest time to wait in millis before declaring a test a failed (overridable)
	private static final int MAX_TEST_TIME = 180000;

	private ASTestConfig config;
	private boolean isCMT = false;
	private boolean clientTx = false;
	private boolean isDebug = false;
	private boolean expectFailure = false;
	private int maxTestTime = MAX_TEST_TIME;

	private String storeDir = null;
//	private String storeImple = "HashedActionStore";
	private String storeImple = "com.arjuna.ats.internal.arjuna.objectstore.HashedActionStore";
	private String storeType = null; //"StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction";
	private TransactionLog store;
	private int existingUids;

	private String name = "Test";
	private String serverName = "default";

	public boolean execute(ASTestConfig config, Map<String, String> params)
	{
		StringBuilder sb = new StringBuilder();
		ASFailureSpec[] fspecs = null;

		this.config = config;

		for (Map.Entry<String, String> me : params.entrySet())
		{
			String key = me.getKey().trim();
			String val = me.getValue().trim();

			if ("name".equals(key))
				name = val;
			else if ("cmt".equals(key))
				isCMT = val.equalsIgnoreCase("true");
			else if ("debug".equals(key))
				isDebug = val.equalsIgnoreCase("true");
			else if ("serverName".equals(key))
				serverName = val;
			else if ("storeType".equals(key))
				storeType = val;
			else if ("storeDir".equals(key))
				storeDir = val;
			else if ("clientTx".equals(key))
				clientTx = val.equalsIgnoreCase("true");
			else if ("storeImple".equals(key))
				storeImple = val;
			else if ("testTime".equals(key))
				maxTestTime = parseInt(val, "parameter testTime should represent a number of seconds: ");
			else if ("specs".equals(key))
				fspecs = parseSpecs(val, sb);
			else if ("wait".equals(key))
				suspendFor(Integer.parseInt(val));
		}

		sb.insert(0, ":\n").insert(0, name).insert(0, "Executing test ");

		System.out.println(sb);

		ClassLoader loader1 = Thread.currentThread().getContextClassLoader();
		ClassLoader loader2 = this.getClass().getClassLoader();

		try
		{
			String serverPath = config.getServerPath(serverName);

			// switch class loaders since a custom ant task runs with a different loader from the loader
			// that loader that loaded the class
//			Thread.currentThread().setContextClassLoader(loader2);

			// get a handle to the transaction logs
			if (storeDir == null)
				storeDir = serverPath + "data/tx-object-store";
			else
				storeDir = serverPath + storeDir;

			store = new TransactionLog(storeDir, storeImple);

			if (expectFailure)
			{
				// this test may halt the VM so make sure the transaction log is empty
				// before starting the test - then the pass/fail check is simply to
				// test whether or not the log is empty (see recoverUids() below).
				try
				{
					store.clearXids(storeType);
				}
				catch (Exception ignore)
				{
				}

				existingUids = getPendingUids();
			}

			// run the crash test
			return crashTest(fspecs);
		}
		catch (Exception e)
		{
			if (isDebug)
				e.printStackTrace();

			throw new BuildException(e);
		}
		finally
		{
//			Thread.currentThread().setContextClassLoader(loader1);
		}
	}

	public boolean cancel() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException("TODO");
	}

	private ASFailureSpec[] parseSpecs(String specArg, StringBuilder sb)
	{
		ASFailureSpec[] fspecs = config.parseSpecs(specArg);

		for (ASFailureSpec spec : fspecs)
		{
			String name = (spec == null ? "INVALID" : spec.getName());

			if (spec != null && spec.willTerminateVM())
				expectFailure = true;

			sb.append("\t").append(name).append('\n');
		}

		return fspecs;
	}

	private int parseInt(String intValue, String errorMessage) throws IllegalArgumentException
	{
		try
		{
			return Integer.parseInt(intValue);
		}
		catch (NumberFormatException e)
		{
			System.out.println(errorMessage + e.getMessage());

			throw new IllegalArgumentException(e);
		}
	}

	// count how many pending transaction branches there are in the transaction log
	private int getPendingUids()
	{
		try
		{
			return store.getIds(storeType).size();
		}
		catch (Exception e)
		{
			e.printStackTrace();

			return -1;
		}
	}

	private CrashRem lookup(String name) throws Exception
	{
		Object o = config.getNamingContext(serverName).lookup(name);
		CrashRemHome home = (CrashRemHome) PortableRemoteObject.narrow(o, CrashRemHome.class);

		return home.create();
	}

	private UserTransaction startTx() throws NamingException, SystemException, NotSupportedException
	{
		UserTransaction tx = (UserTransaction) config.getNamingContext(serverName).lookup("UserTransaction");

		tx.begin();

		return tx;
	}

	private boolean crashTest(ASFailureSpec[] sa) throws Exception
	{
		UserTransaction tx = null;

		try
		{
			CrashRem cr = lookup(isCMT ? CrashRem.CMT_JNDI_NAME : CrashRem.BMT_JNDI_NAME);

			if (clientTx)
				tx = startTx();

			String res = cr.testXA(sa);

			return "Passed".equalsIgnoreCase(res);
		}
		catch (CannotConnectException e)
		{
			if (expectFailure)
			{
				print("Failure was expected: " + e.getMessage());

				return recoverUids();
			}
			else
			{
				System.err.println("XACrashTest:crashTest: Caught " + e);

				e.printStackTrace();
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			System.err.println("XACrashTest:crashTest: Caught " + t);
		}
		finally {
			if (clientTx)
				try
				{
					tx.commit();
				}
				catch (Throwable e)
				{
					System.out.println("User tx commit failure: " + e.getMessage());
				}
		}

		return false;
	}

	/**
	 * Wait for any pending transactions to recover by restaring the AS.
	 * @return true if all pending branches have been recovered
	 * @throws IOException if the server cannot be started
	 */
	private boolean recoverUids() throws IOException
	{
		int retryPeriod = 30000;
		int maxWait = maxTestTime;
		int pendingUids;

		do
		{
			pendingUids = getPendingUids();

			if (pendingUids == -1)
				return false;   // object store error

			if (pendingUids <= existingUids)
				return true;	// all uids recovered

			pendingUids -= existingUids;

			print("waiting for " + pendingUids + " branches");

			// wait for the server to start up the first time through
			if (maxWait == maxTestTime)
				config.startServer(serverName);

			suspendFor(retryPeriod);
			maxWait -= retryPeriod;
		} while (maxWait > 0);

		// the test failed to recover some uids - clear them out ready for the next test
		try
		{
			store.clearXids(storeType);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return false;
	}

	private void suspendFor(int millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			System.out.println("Test " + name + " interupted");
		}
	}

	static void print(String msg)
	{
		System.out.println(msg);
	}
}
