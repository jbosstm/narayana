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
package com.arjuna.ats.jta.distributed;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jboss.byteman.rule.exception.ExecuteException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.jta.distributed.server.CompletionCounter;
import com.arjuna.ats.jta.distributed.server.IsolatableServersClassLoader;
import com.arjuna.ats.jta.distributed.server.LocalServer;
import com.arjuna.ats.jta.distributed.server.LookupProvider;

@RunWith(BMUnitRunner.class)
public class SimpleIsolatedServers {
	private static String[] serverNodeNames = new String[] { "1000", "2000", "3000" };
	private static int[] serverPortOffsets = new int[] { 1000, 2000, 3000 };
	private static String[][] clusterBuddies = new String[][] { new String[] { "2000", "3000" }, new String[] { "1000", "3000" },
			new String[] { "1000", "2000" } };
	private static LookupProvider lookupProvider = LookupProvider.getInstance();
	private static LocalServer[] localServers = new LocalServer[serverNodeNames.length];
	private static CompletionCounter completionCounter = CompletionCounter.getInstance();

	@BeforeClass
	public static void setup() throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException,
			CoreEnvironmentBeanException, IOException, IllegalArgumentException, NoSuchFieldException {
		for (int i = 0; i < serverNodeNames.length; i++) {
			boot(i);
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		// Enable it if you need to ensure the folder is empty for some reason
		if (false) {
			File file = new File(System.getProperty("user.dir") + "/distributedjta-tests/");
			boolean delete = !file.exists() ? true : deleteDir(file);
			if (!delete) {
				throw new Exception("Could not delete folder");
			}
		}
		for (int i = 0; i < localServers.length; i++) {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(localServers[i].getClassLoader());
			localServers[i].shutdown();
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
		completionCounter.reset();
		lookupProvider.clear();
	}

	private static void reboot(String serverName) throws Exception {
		// int index = (Integer.valueOf(serverName) / 1000) - 1;
		for (int i = 0; i < localServers.length; i++) {
			if (localServers[i].getNodeName().equals(serverName)) {
				ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(localServers[i].getClassLoader());
				localServers[i].shutdown();
				Thread.currentThread().setContextClassLoader(contextClassLoader);

				boot(i);
				break;
			}
		}

	}

	private static void boot(int index) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, IllegalArgumentException, CoreEnvironmentBeanException, IOException, NoSuchFieldException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		IsolatableServersClassLoader classLoaderForTransactionManager = new IsolatableServersClassLoader(null, SimpleIsolatedServers.class.getPackage()
				.getName(), contextClassLoader);
		IsolatableServersClassLoader classLoader = new IsolatableServersClassLoader(SimpleIsolatedServers.class.getPackage().getName(), null,
				classLoaderForTransactionManager);
		localServers[index] = (LocalServer) classLoader.loadClass("com.arjuna.ats.jta.distributed.server.impl.ServerImpl").newInstance();
		Thread.currentThread().setContextClassLoader(localServers[index].getClassLoader());
		localServers[index].initialise(lookupProvider, serverNodeNames[index], serverPortOffsets[index], clusterBuddies[index],
				classLoaderForTransactionManager);
		lookupProvider.bind(index, localServers[index].connectTo());
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}

	/**
	 * Ensure that two servers can start up and call recover on the same server
	 * 
	 * The JCA XATerminator call wont allow intermediary calls to
	 * XATerminator::recover between TMSTARTSCAN and TMENDSCAN. This is fine for
	 * distributed JTA.
	 * 
	 * @throws XAException
	 * @throws IOException
	 * @throws DummyRemoteException
	 */
	@Test
	@BMScript("leave-subordinate-orphan")
	public void testSimultaneousRecover() throws Exception {
		System.out.println("testSimultaneousRecover");
		tearDown();
		setup();
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
		final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
		{
			Thread thread = new Thread(new Runnable() {
				public void run() {
					int startingTimeout = 0;
					try {
						LocalServer originalServer = getLocalServer("1000");
						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
						TransactionManager transactionManager = originalServer.getTransactionManager();
						transactionManager.setTransactionTimeout(startingTimeout);
						transactionManager.begin();
						Transaction originalTransaction = transactionManager.getTransaction();
						int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
						Xid currentXid = originalServer.getCurrentXid();
						originalServer.storeRootTransaction();
						transactionManager.suspend();
						DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
								new LinkedList<String>(Arrays.asList(new String[] { "2000" })), remainingTimeout, currentXid, 2, false, false);
						transactionManager.resume(originalTransaction);
						XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
						originalTransaction.enlistResource(proxyXAResource);
						originalServer.removeRootTransaction(currentXid);
						transactionManager.commit();
						Thread.currentThread().setContextClassLoader(classLoader);
					} catch (ExecuteException e) {
						System.err.println("Should be a thread death but cest la vie");
						synchronized (phase2CommitAborted) {
							phase2CommitAborted.incrementCount();
							phase2CommitAborted.notify();
						}
					} catch (LinkageError t) {
						System.err.println("Should be a thread death but cest la vie");
						synchronized (phase2CommitAborted) {
							phase2CommitAborted.incrementCount();
							phase2CommitAborted.notify();
						}
					} catch (Throwable t) {
						System.err.println("Should be a thread death but cest la vie");
						synchronized (phase2CommitAborted) {
							phase2CommitAborted.incrementCount();
							phase2CommitAborted.notify();
						}
					}
				}
			}, "Orphan-creator");
			thread.start();
		}

		{
			Thread thread = new Thread(new Runnable() {
				public void run() {
					int startingTimeout = 0;
					try {
						LocalServer originalServer = getLocalServer("2000");
						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
						TransactionManager transactionManager = originalServer.getTransactionManager();
						transactionManager.setTransactionTimeout(startingTimeout);
						transactionManager.begin();
						Transaction originalTransaction = transactionManager.getTransaction();
						int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
						Xid currentXid = originalServer.getCurrentXid();
						originalServer.storeRootTransaction();
						transactionManager.suspend();
						DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
								new LinkedList<String>(Arrays.asList(new String[] { "1000" })), remainingTimeout, currentXid, 2, false, false);
						transactionManager.resume(originalTransaction);
						XAResource proxyXAResource = originalServer.generateProxyXAResource("1000", performTransactionalWork.getProxyRequired());
						originalTransaction.enlistResource(proxyXAResource);
						originalServer.removeRootTransaction(currentXid);
						transactionManager.commit();
						Thread.currentThread().setContextClassLoader(classLoader);
					} catch (ExecuteException e) {
						System.err.println("Should be a thread death but cest la vie");
						synchronized (phase2CommitAborted) {
							phase2CommitAborted.incrementCount();
							phase2CommitAborted.notify();
						}
					} catch (LinkageError t) {
						System.err.println("Should be a thread death but cest la vie");
						synchronized (phase2CommitAborted) {
							phase2CommitAborted.incrementCount();
							phase2CommitAborted.notify();
						}
					} catch (Throwable t) {
						System.err.println("Should be a thread death but cest la vie");
						synchronized (phase2CommitAborted) {
							phase2CommitAborted.incrementCount();
							phase2CommitAborted.notify();
						}
					}
				}
			}, "Orphan-creator");
			thread.start();
		}
		synchronized (phase2CommitAborted) {
		    int waitedCount = 0;
			while (phase2CommitAborted.getCount() < 2) {
				phase2CommitAborted.wait(5000);
				waitedCount++;
	            if (waitedCount > 2 && phase2CommitAborted.getCount() < 2) {
	                fail("Servers were not aborted");
	            }           
			}
		}

		reboot("1000");
		reboot("2000");
		reboot("3000");

		final CompletionCountLock lock = new CompletionCountLock();
		{
			new Thread(new Runnable() {
				public void run() {
					getLocalServer("2000").doRecoveryManagerScan(true);
					lock.incrementCount();
				}
			}, "RecMan2000").start();
		}

		{
			new Thread(new Runnable() {
				public void run() {
					getLocalServer("1000").doRecoveryManagerScan(true);
					lock.incrementCount();
				}
			}, "RecMan1000").start();
		}

		synchronized (lock) {
			while (lock.getCount() < 2) {
				lock.wait();
			}
		}

		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 3);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 3);

	}

	/**
	 * Ensure that subordinate XA resource orphans created during 2PC can be
	 * recovered
	 */
	@Test
	@BMScript("leaveorphan")
	public void testTwoPhaseXAResourceOrphan() throws Exception {
		System.out.println("testTwoPhaseXAResourceOrphan");
		tearDown();
		setup();
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int startingTimeout = 0;
				try {
					LocalServer originalServer = getLocalServer("1000");
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
					TransactionManager transactionManager = originalServer.getTransactionManager();
					transactionManager.setTransactionTimeout(startingTimeout);
					transactionManager.begin();
					Transaction originalTransaction = transactionManager.getTransaction();
					int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
					Xid currentXid = originalServer.getCurrentXid();
					originalServer.storeRootTransaction();
					transactionManager.suspend();
					DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
							new LinkedList<String>(Arrays.asList(new String[] { "2000" })), remainingTimeout, currentXid, 1, false, false);
					transactionManager.resume(originalTransaction);
					XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
					originalTransaction.enlistResource(proxyXAResource);
					// Needs a second resource to make sure we dont get the one
					// phase optimization happening
					originalTransaction.enlistResource(new TestResource(originalServer.getNodeName(), false));
					originalServer.removeRootTransaction(currentXid);
					transactionManager.commit();
					Thread.currentThread().setContextClassLoader(classLoader);
				} catch (Error t) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}, "Orphan-creator");
		thread.start();
		synchronized (phase2CommitAborted) {
			if (phase2CommitAborted.getCount() < 1) {
				phase2CommitAborted.wait(5000);
			}
            if (phase2CommitAborted.getCount() < 1) {
                fail("Servers were not aborted");
            }
		}

		reboot("1000");
		reboot("2000");
		reboot("3000");

		{

			assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
			getLocalServer("2000").doRecoveryManagerScan(true);
			assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 1);
		}
		{
			assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
			getLocalServer("1000").doRecoveryManagerScan(true);
			assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
		}
	}

	/**
	 * Ensure that subordinate XA resource orphans created during 1PC (at root)
	 * can be recovered
	 */
	@Test
	@BMScript("leaveorphan")
	public void testOnePhaseXAResourceOrphan() throws Exception {
		System.out.println("testOnePhaseXAResourceOrphan");
		tearDown();
		setup();
		assertTrue("" + completionCounter.getCommitCount("3000"), completionCounter.getCommitCount("3000") == 0);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int startingTimeout = 0;
				try {
					LocalServer originalServer = getLocalServer("1000");
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
					TransactionManager transactionManager = originalServer.getTransactionManager();
					transactionManager.setTransactionTimeout(startingTimeout);
					transactionManager.begin();
					Transaction originalTransaction = transactionManager.getTransaction();
					int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
					Xid currentXid = originalServer.getCurrentXid();
					originalServer.storeRootTransaction();
					transactionManager.suspend();
					DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
							new LinkedList<String>(Arrays.asList(new String[] { "2000" })), remainingTimeout, currentXid, 2, false, false);
					transactionManager.resume(originalTransaction);
					XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
					originalTransaction.enlistResource(proxyXAResource);
					originalServer.removeRootTransaction(currentXid);
					transactionManager.commit();
					Thread.currentThread().setContextClassLoader(classLoader);
				} catch (Error t) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				} catch (Throwable t) {
					t.printStackTrace();
					// synchronized (phase2CommitAborted) {
					// phase2CommitAborted.incrementPhase2CommitAborted();
					// phase2CommitAborted.notify();
					// }
				}
			}
		}, "Orphan-creator");
		thread.start();
		synchronized (phase2CommitAborted) {
			if (phase2CommitAborted.getCount() < 1) {
				phase2CommitAborted.wait(5000);
			}
            if (phase2CommitAborted.getCount() < 1) {
                fail("Servers were not aborted");
            }
		}

		reboot("1000");
		reboot("2000");
		reboot("3000");

		{

			assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
			getLocalServer("2000").doRecoveryManagerScan(true);
			assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 1);
		}
		{
			assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
			getLocalServer("1000").doRecoveryManagerScan(true);
			assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
			assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
		}
	}

	/**
	 * Ensure that subordinate transaction orphans created during 1PC (at root)
	 * can be recovered
	 */
	@Test
	@BMScript("leave-subordinate-orphan")
	public void testOnePhaseSubordinateOrphan() throws Exception {
		System.out.println("testOnePhaseSubordinateOrphan");
		tearDown();
		setup();
		assertTrue("" + completionCounter.getCommitCount("3000"), completionCounter.getCommitCount("3000") == 0);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int startingTimeout = 0;
				try {
					LocalServer originalServer = getLocalServer("1000");
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
					TransactionManager transactionManager = originalServer.getTransactionManager();
					transactionManager.setTransactionTimeout(startingTimeout);
					transactionManager.begin();
					Transaction originalTransaction = transactionManager.getTransaction();
					int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
					Xid currentXid = originalServer.getCurrentXid();
					originalServer.storeRootTransaction();
					transactionManager.suspend();
					DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
							new LinkedList<String>(Arrays.asList(new String[] { "2000" })), remainingTimeout, currentXid, 2, false, false);
					transactionManager.resume(originalTransaction);
					XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
					originalTransaction.enlistResource(proxyXAResource);
					originalServer.removeRootTransaction(currentXid);
					transactionManager.commit();
					Thread.currentThread().setContextClassLoader(classLoader);
				} catch (ExecuteException e) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				} catch (LinkageError t) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				} catch (Throwable t) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				}
			}
		}, "Orphan-creator");
		thread.start();
		synchronized (phase2CommitAborted) {
			if (phase2CommitAborted.getCount() < 1) {
				phase2CommitAborted.wait(5000);
			}
            if (phase2CommitAborted.getCount() < 1) {
                fail("Servers were not aborted");
            }
		}
		reboot("1000");
		reboot("2000");
		reboot("3000");
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
		getLocalServer("1000").doRecoveryManagerScan(true);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 1);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 2);

	}

	/**
	 * Check that if transaction was in flight when a root crashed, when
	 * recovered it can terminate it.
	 * 
	 * recoverFor first greps the logs for any subordinates that are owned by
	 * "parentNodeName" then it greps the list of currently running transactions
	 * to see if any of them are owned by "parentNodeName" this is covered by
	 * testRecoverInflightTransaction basically what can happen is:
	 * 
	 * 1. TM1 starts tx 2. propagate to TM2 3. TM1 crashes 4. we need to
	 * rollback TM2 as it is now orphaned the detail being that as TM2 hasn't
	 * prepared we cant just grep the logs at TM2 as there wont be one
	 */
	@Test
	@BMScript("leaverunningorphan")
	public void testRecoverInflightTransaction() throws Exception {
		System.out.println("testRecoverInflightTransaction");
		tearDown();
		setup();

		assertTrue("" + completionCounter.getCommitCount("3000"), completionCounter.getCommitCount("3000") == 0);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int startingTimeout = 0;
				try {
					LocalServer originalServer = getLocalServer("1000");
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
					TransactionManager transactionManager = originalServer.getTransactionManager();
					transactionManager.setTransactionTimeout(startingTimeout);
					transactionManager.begin();
					Transaction originalTransaction = transactionManager.getTransaction();
					int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
					Xid currentXid = originalServer.getCurrentXid();
					originalServer.storeRootTransaction();
					transactionManager.suspend();
					DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
							new LinkedList<String>(Arrays.asList(new String[] { "2000" })), remainingTimeout, currentXid, 2, false, false);
					transactionManager.resume(originalTransaction);
					XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
					originalTransaction.enlistResource(proxyXAResource);
					originalServer.removeRootTransaction(currentXid);
					transactionManager.commit();
					Thread.currentThread().setContextClassLoader(classLoader);
				} catch (ExecuteException e) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				} catch (LinkageError t) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				} catch (Throwable t) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				}
			}
		}, "Orphan-creator");
		thread.start();
		synchronized (phase2CommitAborted) {
			if (phase2CommitAborted.getCount() < 1) {
				phase2CommitAborted.wait(5000);
			}
            if (phase2CommitAborted.getCount() < 1) {
                fail("Servers were not aborted");
            }
		}
		reboot("1000");
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
		getLocalServer("1000").doRecoveryManagerScan(true);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("Rollback count at 1000: " + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 1);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 2);
	}

	/**
	 * Top down recovery of a prepared transaction
	 */
	@Test
	@BMScript("fail2pc")
	public void testRecovery() throws Exception {
		System.out.println("testRecovery");
		tearDown();
		setup();
		assertTrue("" + completionCounter.getCommitCount("3000"), completionCounter.getCommitCount("3000") == 0);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("3000"), completionCounter.getRollbackCount("3000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
		final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
		Thread thread = new Thread(new Runnable() {
			public void run() {
				int startingTimeout = 0;
				List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(new String[] { "1000", "2000", "3000", "2000", "1000", "2000", "3000",
						"1000", "3000" }));
				try {
					doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, true, false);
				} catch (ExecuteException e) {
					System.err.println("Should be a thread death but cest la vie");
					synchronized (phase2CommitAborted) {
						phase2CommitAborted.incrementCount();
						phase2CommitAborted.notify();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		thread.start();
		synchronized (phase2CommitAborted) {
			if (phase2CommitAborted.getCount() < 1) {
				phase2CommitAborted.wait(5000);
			}
            if (phase2CommitAborted.getCount() < 1) {
                fail("Servers were not aborted");
            }
		}

		reboot("1000");
		reboot("2000");
		reboot("3000");

		getLocalServer("1000").doRecoveryManagerScan(false);

		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 4);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 4);
		assertTrue("" + completionCounter.getCommitCount("3000"), completionCounter.getCommitCount("3000") == 3);
		assertTrue("" + completionCounter.getRollbackCount("3000"), completionCounter.getRollbackCount("3000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
	}

	@Test
	public void testOnePhaseCommit() throws Exception {
		System.out.println("testOnePhaseCommit");
		tearDown();
		setup();
		LocalServer originalServer = getLocalServer("1000");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
		TransactionManager transactionManager = originalServer.getTransactionManager();
		transactionManager.setTransactionTimeout(0);
		transactionManager.begin();
		Transaction originalTransaction = transactionManager.getTransaction();
		int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
		Xid currentXid = originalServer.getCurrentXid();
		originalServer.storeRootTransaction();
		transactionManager.suspend();
		DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(new LinkedList<String>(Arrays.asList(new String[] { "2000" })),
				remainingTimeout, currentXid, 1, false, false);
		transactionManager.resume(originalTransaction);
		XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
		originalTransaction.enlistResource(proxyXAResource);
		originalServer.removeRootTransaction(currentXid);
		transactionManager.commit();
		Thread.currentThread().setContextClassLoader(classLoader);

		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 1);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 1);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
	}

	@Test
	public void testUnPreparedRollback() throws Exception {
		System.out.println("testUnPreparedRollback");
		tearDown();
		setup();
		LocalServer originalServer = getLocalServer("1000");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
		TransactionManager transactionManager = originalServer.getTransactionManager();
		transactionManager.setTransactionTimeout(0);
		transactionManager.begin();
		Transaction originalTransaction = transactionManager.getTransaction();
		int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
		Xid currentXid = originalServer.getCurrentXid();
		originalServer.storeRootTransaction();
		transactionManager.suspend();
		DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(new LinkedList<String>(Arrays.asList(new String[] { "2000" })),
				remainingTimeout, currentXid, 1, false, false);
		transactionManager.resume(originalTransaction);
		XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
		originalTransaction.enlistResource(proxyXAResource);
		originalTransaction.registerSynchronization(originalServer.generateProxySynchronization("2000", currentXid));
		originalServer.removeRootTransaction(currentXid);
		transactionManager.rollback();
		Thread.currentThread().setContextClassLoader(classLoader);

		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 1);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 1);
	}

	@Test
	public void testMigrateTransactionRollbackOnlyCommit() throws Exception {
		System.out.println("testMigrateTransactionRollbackOnlyCommit");
		int startingTimeout = 0;
		List<String> nodesToFlowTo = new LinkedList<String>(
				Arrays.asList(new String[] { "1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000" }));
		doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, true, true);
	}

	@Test
	public void testMigrateTransactionRollbackOnlyRollback() throws Exception {
		System.out.println("testMigrateTransactionRollbackOnlyRollback");
		int startingTimeout = 0;
		List<String> nodesToFlowTo = new LinkedList<String>(
				Arrays.asList(new String[] { "1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000" }));
		doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, false, true);
	}

	@Test
	public void testMigrateTransactionCommit() throws Exception {
		System.out.println("testMigrateTransactionCommit");
		int startingTimeout = 0;
		List<String> nodesToFlowTo = new LinkedList<String>(
				Arrays.asList(new String[] { "1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000" }));
		doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, true, false);
	}

	@Test
	public void testMigrateTransactionCommitDiamond() throws Exception {
		System.out.println("testMigrateTransactionCommitDiamond");

		int startingTimeout = 0;
		List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(new String[] { "1000", "2000", "1000", "3000", "1000", "2000", "3000" }));
		doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, true, false);
	}

	@Test
	public void testMigrateTransactionRollback() throws Exception {
		System.out.println("testMigrateTransactionRollback");
		int startingTimeout = 0;
		List<String> nodesToFlowTo = new LinkedList<String>(
				Arrays.asList(new String[] { "1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000" }));
		doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, false, false);
	}

	@Test
	public void testMigrateTransactionRollbackDiamond() throws Exception {
		System.out.println("testMigrateTransactionRollbackDiamond");
		int startingTimeout = 0;
		List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(new String[] { "1000", "2000", "1000", "3000", "1000", "2000", "3000" }));
		doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, false, false);
	}

	@Test
	public void testMigrateTransactionSubordinateTimeout() throws Exception {
		System.out.println("testMigrateTransactionSubordinateTimeout");
		tearDown();
		setup();
		int rootTimeout = 10000;
		int subordinateTimeout = 1;
		LocalServer originalServer = getLocalServer("1000");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
		TransactionManager transactionManager = originalServer.getTransactionManager();
		transactionManager.setTransactionTimeout(rootTimeout);
		transactionManager.begin();
		Transaction originalTransaction = transactionManager.getTransaction();
		Xid currentXid = originalServer.getCurrentXid();
		originalServer.storeRootTransaction();
		originalTransaction.enlistResource(new TestResource(originalServer.getNodeName(), false));
		transactionManager.suspend();

		// Migrate a transaction
		LocalServer currentServer = getLocalServer("2000");
		ClassLoader parentsClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(currentServer.getClassLoader());
		Xid migratedXid = currentServer.locateOrImportTransactionThenResumeIt(subordinateTimeout, currentXid);
		currentServer.getTransactionManager().getTransaction().enlistResource(new TestResource(currentServer.getNodeName(), false));
		currentServer.getTransactionManager().suspend();
		Thread.currentThread().setContextClassLoader(parentsClassLoader);

		// Complete the transaction at the original server
		transactionManager.resume(originalTransaction);
		XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", migratedXid);
		originalTransaction.enlistResource(proxyXAResource);
		originalServer.removeRootTransaction(currentXid);
		Thread.currentThread().sleep((subordinateTimeout + 1) * 1000);
		try {
			transactionManager.commit();
			fail("Did not rollback");
		} catch (RollbackException rbe) {
			// GOOD!
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 1);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 2);
	}

	@Test
	public void testTransactionReaperIsCleanedUp() throws Exception {
		System.out.println("testTransactionReaperIsCleanedUp");
		tearDown();
		setup();
		int rootTimeout = 5;
		LocalServer originalServer = getLocalServer("1000");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
		TransactionManager transactionManager = originalServer.getTransactionManager();
		transactionManager.setTransactionTimeout(rootTimeout);
		transactionManager.begin();
		Transaction originalTransaction = transactionManager.getTransaction();
		Xid currentXid = originalServer.getCurrentXid();
		originalServer.storeRootTransaction();
		originalTransaction.enlistResource(new TestResource(originalServer.getNodeName(), false));
		int subordinateTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
		transactionManager.suspend();

		// Migrate a transaction
		LocalServer currentServer = getLocalServer("2000");
		ClassLoader parentsClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(currentServer.getClassLoader());
		Xid migratedXid = currentServer.locateOrImportTransactionThenResumeIt(subordinateTimeout, currentXid);
		currentServer.getTransactionManager().getTransaction().enlistResource(new TestResource(currentServer.getNodeName(), false));
		currentServer.getTransactionManager().suspend();
		Thread.currentThread().setContextClassLoader(parentsClassLoader);

		// Complete the transaction at the original server
		transactionManager.resume(originalTransaction);
		XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", migratedXid);
		originalTransaction.enlistResource(proxyXAResource);
		originalServer.removeRootTransaction(currentXid);
		transactionManager.commit();
		Thread.currentThread().setContextClassLoader(classLoader);
		assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 1);
		assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 2);
		assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
		assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);

		Thread.currentThread().sleep((subordinateTimeout + 4) * 1000);
	}

	private void doRecursiveTransactionalWork(int startingTimeout, List<String> nodesToFlowTo, boolean commit, boolean rollbackOnlyOnLastNode) throws Exception {
		tearDown();
		setup();

		List<String> uniqueServers = new ArrayList<String>();
		Iterator<String> iterator = nodesToFlowTo.iterator();
		while (iterator.hasNext()) {
			String intern = iterator.next().intern();
			if (!uniqueServers.contains(intern)) {
				uniqueServers.add(intern);
			}
		}
		// Start out at the first server
		int totalCompletionCount = nodesToFlowTo.size() + uniqueServers.size() - 1;
		String startingServer = nodesToFlowTo.get(0);
		LocalServer originalServer = getLocalServer(startingServer);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
		TransactionManager transactionManager = originalServer.getTransactionManager();
		transactionManager.setTransactionTimeout(startingTimeout);
		transactionManager.begin();
		Transaction transaction = transactionManager.getTransaction();
		int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
		Xid currentXid = originalServer.getCurrentXid();
		originalServer.storeRootTransaction();
		transactionManager.suspend();
		DataReturnedFromRemoteServer dataReturnedFromRemoteServer = performTransactionalWork(nodesToFlowTo, remainingTimeout, currentXid, 1, true,
				rollbackOnlyOnLastNode);
		transactionManager.resume(transaction);
		originalServer.removeRootTransaction(currentXid);

		// Align the local state with the returning state of the
		// transaction
		// from the subordinate
		switch (dataReturnedFromRemoteServer.getTransactionState()) {
		case Status.STATUS_MARKED_ROLLBACK:
		case Status.STATUS_ROLLEDBACK:
		case Status.STATUS_ROLLING_BACK:
			switch (transaction.getStatus()) {
			case Status.STATUS_MARKED_ROLLBACK:
			case Status.STATUS_ROLLEDBACK:
			case Status.STATUS_ROLLING_BACK:
				transaction.setRollbackOnly();
			}
			break;
		default:
			break;
		}

		if (commit) {
			try {
				transactionManager.commit();
				assertTrue(completionCounter.getTotalCommitCount() == totalCompletionCount);
			} catch (RollbackException e) {
				if (!rollbackOnlyOnLastNode) {
					assertTrue(completionCounter.getTotalRollbackCount() == totalCompletionCount);
				}
			}
		} else {
			transactionManager.rollback();
			assertTrue(completionCounter.getTotalRollbackCount() == totalCompletionCount);
		}
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	private DataReturnedFromRemoteServer performTransactionalWork(List<String> nodesToFlowTo, int remainingTimeout, Xid toMigrate,
			int numberOfResourcesToRegister, boolean addSynchronization, boolean rollbackOnlyOnLastNode) throws RollbackException, IllegalStateException,
			XAException, SystemException, NotSupportedException, IOException {
		String currentServerName = nodesToFlowTo.remove(0);
		LocalServer currentServer = getLocalServer(currentServerName);

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(currentServer.getClassLoader());

		Xid requiresProxyAtPreviousServer = currentServer.locateOrImportTransactionThenResumeIt(remainingTimeout, toMigrate);

		// Perform work on the migrated transaction
		{
			TransactionManager transactionManager = currentServer.getTransactionManager();
			Transaction transaction = transactionManager.getTransaction();
			if (addSynchronization) {
				transaction.registerSynchronization(new TestSynchronization(currentServer.getNodeName()));
			}
			for (int i = 0; i < numberOfResourcesToRegister; i++) {
				transaction.enlistResource(new TestResource(currentServer.getNodeName(), false));
			}

			if (rollbackOnlyOnLastNode && nodesToFlowTo.isEmpty()) {
				transaction.setRollbackOnly();
			}
		}

		if (!nodesToFlowTo.isEmpty()) {

			TransactionManager transactionManager = currentServer.getTransactionManager();
			Transaction transaction = transactionManager.getTransaction();
			int status = transaction.getStatus();

			// Only propagate active transactions - this may be inactive through
			// user code (rollback/setRollbackOnly) or it may be inactive due to
			// the transaction reaper
			if (status == Status.STATUS_ACTIVE) {
				String nextServerNodeName = nodesToFlowTo.get(0);

				// FLOW THE TRANSACTION
				remainingTimeout = (int) (currentServer.getTimeLeftBeforeTransactionTimeout() / 1000);

				// STORE AND SUSPEND THE TRANSACTION
				Xid currentXid = currentServer.getCurrentXid();
				transactionManager.suspend();

				DataReturnedFromRemoteServer dataReturnedFromRemoteServer = performTransactionalWork(nodesToFlowTo, remainingTimeout, currentXid,
						numberOfResourcesToRegister, addSynchronization, rollbackOnlyOnLastNode);
				transactionManager.resume(transaction);

				// Create a proxy for the new server if necessary, this can
				// orphan
				// the remote server but XA recovery will handle that on the
				// remote
				// server
				// The alternative is to always create a proxy but this is a
				// performance drain and will result in multiple subordinate
				// transactions and performance issues
				if (dataReturnedFromRemoteServer.getProxyRequired() != null) {
					XAResource proxyXAResource = currentServer.generateProxyXAResource(nextServerNodeName, dataReturnedFromRemoteServer.getProxyRequired());
					transaction.enlistResource(proxyXAResource);
					transaction.registerSynchronization(currentServer.generateProxySynchronization(nextServerNodeName, toMigrate));
				}

				// Align the local state with the returning state of the
				// transaction
				// from the subordinate
				switch (dataReturnedFromRemoteServer.getTransactionState()) {
				case Status.STATUS_MARKED_ROLLBACK:
				case Status.STATUS_ROLLEDBACK:
				case Status.STATUS_ROLLING_BACK:
					switch (transaction.getStatus()) {
					case Status.STATUS_MARKED_ROLLBACK:
					case Status.STATUS_ROLLEDBACK:
					case Status.STATUS_ROLLING_BACK:
						transaction.setRollbackOnly();
					}
					break;
				default:
					break;
				}
			}
		}
		TransactionManager transactionManager = currentServer.getTransactionManager();
		int transactionState = transactionManager.getStatus();
		// SUSPEND THE TRANSACTION WHEN YOU ARE READY TO RETURN TO YOUR CALLER
		transactionManager.suspend();
		// Return to the previous caller back over the transport/classloader
		// boundary in this case
		Thread.currentThread().setContextClassLoader(classLoader);
		return new DataReturnedFromRemoteServer(requiresProxyAtPreviousServer, transactionState);
	}

	private static LocalServer getLocalServer(String jndiName) {
		int index = (Integer.valueOf(jndiName) / 1000) - 1;
		return localServers[index];
	}

	private class CompletionCountLock {
		private int count;

		public int getCount() {
			return count;
		}

		public synchronized void incrementCount() {
			this.count++;
			this.notify();
		}
	}

	/**
	 * This is the transactional data the transport needs to return from remote
	 * instances.
	 */
	private class DataReturnedFromRemoteServer {
		private Xid proxyRequired;

		private int transactionState;

		public DataReturnedFromRemoteServer(Xid proxyRequired, int transactionState) {
			this.proxyRequired = proxyRequired;
			this.transactionState = transactionState;
		}

		public Xid getProxyRequired() {
			return proxyRequired;
		}

		public int getTransactionState() {
			return transactionState;
		}
	}
}
