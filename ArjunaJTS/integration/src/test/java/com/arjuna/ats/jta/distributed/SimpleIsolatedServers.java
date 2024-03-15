/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.byteman.contrib.bmunit.BMScript;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.jboss.byteman.rule.exception.ExecuteException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.jta.distributed.server.CompletionCounter;
import com.arjuna.ats.jta.distributed.server.IsolatableServersClassLoader;
import com.arjuna.ats.jta.distributed.server.LocalServer;
import com.arjuna.ats.jta.distributed.server.LookupProvider;

@RunWith(BMUnitRunner.class)
public class SimpleIsolatedServers {
    private String[] serverNodeNames = new String[]{"1000", "2000", "3000"};
    private int[] serverPortOffsets = new int[]{1000, 2000, 3000};
    private String[][] clusterBuddies = new String[serverNodeNames.length][];
    private LookupProvider lookupProvider = LookupProvider.getInstance();
    private LocalServer[] localServers = new LocalServer[serverNodeNames.length];
    private CompletionCounter completionCounter = CompletionCounter.getInstance();

    @Before
    public void setup() throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException,
            CoreEnvironmentBeanException, IOException, IllegalArgumentException, NoSuchFieldException {

        for (int i = 0; i < serverNodeNames.length; i++) {
            List<String> otherNodes = new ArrayList<String>();
            for (int j = 0; j < serverNodeNames.length; j++) {
                if (j != i) {
                    otherNodes.add(serverNodeNames[j]);
                }
            }
            clusterBuddies[i] = otherNodes.toArray(new String[0]);
        }

        for (int i = 0; i < serverNodeNames.length; i++) {
            boot(i);
        }
    }

//	public static boolean deleteDir(File dir) {
//		if (dir.isDirectory()) {
//			String[] children = dir.list();
//			for (int i = 0; i < children.length; i++) {
//				boolean success = deleteDir(new File(dir, children[i]));
//				if (!success) {
//					return false;
//				}
//			}
//		}
//
//		// The directory is now empty so delete it
//		return dir.delete();
//	}

    @After
    public void tearDown() throws Exception {
        // Enable it if you need to ensure the folder is empty for some reason
//		if (false) {
//			File file = new File(System.getProperty("user.dir") + "/distributedjta-tests/");
//			boolean delete = !file.exists() ? true : deleteDir(file);
//			if (!delete) {
//				throw new Exception("Could not delete folder");
//			}
//		}
        for (int i = 0; i < localServers.length; i++) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(localServers[i].getClassLoader());
            localServers[i].shutdown();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        completionCounter.reset();
        lookupProvider.clear();
    }

    private void reboot(String serverName) throws Exception {
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

    private void boot(int index) throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException,
            ClassNotFoundException, IllegalArgumentException, CoreEnvironmentBeanException, IOException, NoSuchFieldException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        IsolatableServersClassLoader classLoaderForTransactionManager = new IsolatableServersClassLoader(null, SimpleIsolatedServers.class.getPackage()
                .getName(), contextClassLoader);
        IsolatableServersClassLoader classLoader = new IsolatableServersClassLoader(SimpleIsolatedServers.class.getPackage().getName(), null,
                classLoaderForTransactionManager);
        localServers[index] = (LocalServer) classLoader.loadClass("com.arjuna.ats.jta.distributed.server.impl.ServerImpl").newInstance();
        Thread.currentThread().setContextClassLoader(classLoaderForTransactionManager);
        localServers[index].initialise(lookupProvider, serverNodeNames[index], serverPortOffsets[index], clusterBuddies[index],
                classLoaderForTransactionManager);
        lookupProvider.bind(index, localServers[index].connectTo());
        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    /**
     * Ensure that two servers can start up and call recover on the same server
     * <p>
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
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
        final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
        synchronized (phase2CommitAborted) {
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
                            transactionManager.suspend();
                            DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
                                    new LinkedList<String>(Arrays.asList(new String[]{"2000"})), remainingTimeout, currentXid, 2, false, false);
                            transactionManager.resume(originalTransaction);
                            XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
                            originalTransaction.enlistResource(proxyXAResource);
                            transactionManager.commit();
                            Thread.currentThread().setContextClassLoader(classLoader);
                            synchronized (phase2CommitAborted) {
                                phase2CommitAborted.notify();
                            }
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
                            transactionManager.suspend();
                            DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
                                    new LinkedList<String>(Arrays.asList(new String[]{"1000"})), remainingTimeout, currentXid, 2, false, false);
                            transactionManager.resume(originalTransaction);
                            XAResource proxyXAResource = originalServer.generateProxyXAResource("1000", performTransactionalWork.getProxyRequired());
                            originalTransaction.enlistResource(proxyXAResource);
                            transactionManager.commit();
                            Thread.currentThread().setContextClassLoader(classLoader);
                            synchronized (phase2CommitAborted) {
                                phase2CommitAborted.notify();
                            }
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
            int waitedCount = 0;
            while (waitedCount < 2) {
                phase2CommitAborted.wait(50000);
                waitedCount++;
                if (waitedCount == 2 && phase2CommitAborted.getCount() < 2) {
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
                    try {
                        getLocalServer("2000").doRecoveryManagerScan(true);
                    } finally {
                        lock.incrementCount();
                    }
                }
            }, "RecMan2000").start();
        }

        {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        getLocalServer("1000").doRecoveryManagerScan(true);
                    } finally {
                        lock.incrementCount();
                    }
                }
            }, "RecMan1000").start();
        }

        synchronized (lock) {
            int numberOfNotificationToGet = 2;
            while (numberOfNotificationToGet > 0 && lock.getCount() < 2) {
                lock.wait(300000);
                numberOfNotificationToGet--;
            }

            if (lock.getCount() < 2) {
                fail("Did not get notification for both recovery runs, deadlock in recovery manager scan detected");
                ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "kill -3 $PPID");
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();
                InputStream inputStream = process.getInputStream();
                final byte[] bytes = new byte[4096];
                int bytesRead = inputStream.read(bytes);
                while (bytesRead > -1) {
                    System.out.write(bytes, 0, bytesRead);
                    bytesRead = inputStream.read(bytes);
                }
            }
        }

        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
        // JBTM-1260 simultaneous recover can cause spurious Xid rollback of normally completed Xid, should not be an issue
        // JBTM-1345 simulatenous recover can cause spurious Xid rollback of completed resources, should not be a data integrity issue
        assertTrue("" + completionCounter.getRollbackCount("1000"), Arrays.asList(new Integer[]{3, 4, 5}).contains(completionCounter.getRollbackCount("1000")));
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("2000"), Arrays.asList(new Integer[]{3, 4, 5}).contains(completionCounter.getRollbackCount("2000")));

    }

    /**
     * Ensure that subordinate XA resource orphans created during 2PC can be
     * recovered
     */
    @Test
    @BMScript("leaveorphan")
    public void testTwoPhaseXAResourceOrphan() throws Exception {
        System.out.println("testTwoPhaseXAResourceOrphan");
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
                    transactionManager.suspend();
                    DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
                            new LinkedList<String>(Arrays.asList(new String[]{"2000"})), remainingTimeout, currentXid, 1, false, false);
                    transactionManager.resume(originalTransaction);
                    XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
                    originalTransaction.enlistResource(proxyXAResource);
                    // Needs a second resource to make sure we dont get the one
                    // phase optimization happening
                    originalTransaction.enlistResource(new TestResource(originalServer.getNodeName(), false));
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
                phase2CommitAborted.wait(50000);
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
                    transactionManager.suspend();
                    DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
                            new LinkedList<String>(Arrays.asList(new String[]{"2000"})), remainingTimeout, currentXid, 2, false, false);
                    transactionManager.resume(originalTransaction);
                    XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
                    originalTransaction.enlistResource(proxyXAResource);
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
                phase2CommitAborted.wait(50000);
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
    @BMScript("leave-subordinate-orphan2")
    public void testOnePhaseSubordinateOrphan() throws Exception {
        System.out.println("testOnePhaseSubordinateOrphan");
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
                    transactionManager.suspend();
                    DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
                            new LinkedList<String>(Arrays.asList(new String[]{"2000"})), remainingTimeout, currentXid, 2, false, false);
                    transactionManager.resume(originalTransaction);
                    XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
                    originalTransaction.enlistResource(proxyXAResource);
                    transactionManager.commit();
                    Thread.currentThread().setContextClassLoader(classLoader);
                    synchronized (phase2CommitAborted) {
                        phase2CommitAborted.notify();
                    }
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
                phase2CommitAborted.wait(50000);
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
     * <p>
     * recoverFor first greps the logs for any subordinates that are owned by
     * "parentNodeName" then it greps the list of currently running transactions
     * to see if any of them are owned by "parentNodeName" this is covered by
     * testRecoverInflightTransaction basically what can happen is:
     * <p>
     * 1. TM1 starts tx 2. propagate to TM2 3. TM1 crashes 4. we need to
     * rollback TM2 as it is now orphaned the detail being that as TM2 hasn't
     * prepared we cant just grep the logs at TM2 as there wont be one
     */
    @Test
    @BMScript("leaverunningorphan")
    public void testRecoverInflightTransaction() throws Exception {
        System.out.println("testRecoverInflightTransaction");
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
                    transactionManager.suspend();
                    DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
                            new LinkedList<String>(Arrays.asList(new String[]{"2000"})), remainingTimeout, currentXid, 2, false, false);
                    transactionManager.resume(originalTransaction);
                    XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
                    originalTransaction.enlistResource(proxyXAResource);
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
                phase2CommitAborted.wait(50000);
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
     * Check that if transaction was in flight when a root crashed, when
     * recovered it can terminate it.
     * <p>
     * recoverFor first greps the logs for any subordinates that are owned by
     * "parentNodeName" then it greps the list of currently running transactions
     * to see if any of them are owned by "parentNodeName" this is covered by
     * testRecoverInflightTransaction basically what can happen is:
     * <p>
     * 1. TM1 starts tx 2. propagate to TM2 3. TM1 crashes 4. we need to
     * rollback TM2 as it is now orphaned the detail being that as TM2 hasn't
     * prepared we cant just grep the logs at TM2 as there wont be one
     */
    @Test
    @BMScript("leaverunningorphan")
    public void testRecoverReapedInflightTransaction() throws Exception {
        System.out.println("testRecoverInflightTransaction");
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
        final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
        synchronized (phase2CommitAborted) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    int startingTimeout = 2;
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
                        transactionManager.suspend();
                        DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(
                                new LinkedList<String>(Arrays.asList(new String[]{"2000"})), remainingTimeout, currentXid, 2, false, false);
                        transactionManager.resume(originalTransaction);
                        XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
                        originalTransaction.enlistResource(proxyXAResource);
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
            if (phase2CommitAborted.getCount() < 1) {
                phase2CommitAborted.wait(50000);
            }
            if (phase2CommitAborted.getCount() < 1) {
                fail("Servers were not aborted");
            }
        }

        // Wait for reap on server 2
        synchronized (completionCounter) {
            while (completionCounter.getRollbackCount("2000") != 2) {
                completionCounter.wait();
            }
        }

        assertTrue("Expected 1, was: " + LookupProvider.getInstance().lookup("2000").getTransactionCount(), LookupProvider.getInstance().lookup("2000").getTransactionCount() == 1);
        reboot("1000");
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 2);
        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
        getLocalServer("1000").doRecoveryManagerScan(true);
        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 0);
        assertTrue("Rollback count at 1000: " + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 1);
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 2);
        assertTrue("Expected 0 transactions, was: " + LookupProvider.getInstance().lookup("2000").getTransactionCount(), LookupProvider.getInstance().lookup("2000").getTransactionCount() == 0);
    }

    /**
     * Top down recovery of a prepared transaction
     */
    @Test
    @BMScript("fail2pc")
    public void testRecovery() throws Exception {
        System.out.println("testRecovery");
        for (String nodeName : serverNodeNames) {
            assertTrue("" + completionCounter.getCommitCount(nodeName), completionCounter.getCommitCount(nodeName) == 0);
            assertTrue("" + completionCounter.getRollbackCount(nodeName), completionCounter.getRollbackCount(nodeName) == 0);
        }

        final CompletionCountLock phase2CommitAborted = new CompletionCountLock();
        Thread thread = new Thread(new Runnable() {
            public void run() {
                int startingTimeout = 0;
                List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(serverNodeNames));
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
            int count = 0;
            while (phase2CommitAborted.getCount() != 1 && count < 20) {
                phase2CommitAborted.wait(50000);
                if (phase2CommitAborted.getCount() != 1) {
                    count++;
                } else {
                    break;
                }
            }
            if (phase2CommitAborted.getCount() < 1) {
                fail("Servers were not aborted");
            }
        }

        for (String nodeName : serverNodeNames) {
            reboot(nodeName);
        }

        getLocalServer("1000").doRecoveryManagerScan(false);

        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 2);
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 2);
        assertTrue("" + completionCounter.getCommitCount("3000"), completionCounter.getCommitCount("3000") == 1);
        for (String nodeName : serverNodeNames) {
            assertTrue("" + completionCounter.getRollbackCount(nodeName), completionCounter.getRollbackCount(nodeName) == 0);
        }
    }

    /**
     * Top down recovery of a prepared transaction
     */
    @Test
    public void testRecovery2() throws Exception {
        System.out.println("testRecovery2");
        for (String nodeName : serverNodeNames) {
            assertTrue("" + completionCounter.getCommitCount(nodeName), completionCounter.getCommitCount(nodeName) == 0);
            assertTrue("" + completionCounter.getRollbackCount(nodeName), completionCounter.getRollbackCount(nodeName) == 0);
        }

        final CompletionCountLock errors = new CompletionCountLock();
        synchronized (errors) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    int startingTimeout = 0;
                    List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(serverNodeNames));
                    synchronized (errors) {

                        try {
                            doRecursiveTransactionalWork2(startingTimeout, nodesToFlowTo, true, false);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            errors.incrementCount();

                        }
                        errors.notify();
                    }
                }
            });
            thread.start();

            errors.wait();
            if (errors.getCount() > 0) {
                fail("Unexpected errors");
            }
        }

        reboot("2000");

        getLocalServer("3000").doRecoveryManagerScan(true);
        getLocalServer("2000").doRecoveryManagerScan(true);
        getLocalServer("1000").doRecoveryManagerScan(false);

        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 1);
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 2);
        assertTrue("" + completionCounter.getCommitCount("3000"), completionCounter.getCommitCount("3000") == 1);
        for (String nodeName : serverNodeNames) {
            assertTrue("" + completionCounter.getRollbackCount(nodeName), completionCounter.getRollbackCount(nodeName) == 0);
        }
    }

    @Test
    public void testDisabledDynamic1PC() throws Exception {
        System.out.println("testDisabledDynamic1PC");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Transaction originalTransaction;
        int remainingTimeout;
        Xid toMigrate;
        final boolean[] resource1prepared = new boolean[1];
        {
            LocalServer originalServer = getLocalServer("1000");
            Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
            TransactionManager transactionManager = originalServer.getTransactionManager();
            transactionManager.setTransactionTimeout(0);
            transactionManager.begin();
            originalTransaction = transactionManager.getTransaction();
            remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
            toMigrate = originalServer.getCurrentXid();
            transactionManager.suspend();
        }

        Xid requiresProxyAtPreviousServer = null;
        {
            LocalServer currentServer = getLocalServer("2000");

            Thread.currentThread().setContextClassLoader(currentServer.getClassLoader());

            currentServer.locateOrImportTransactionThenResumeIt(remainingTimeout, toMigrate);

            // Perform work on the migrated transaction
            {
                TransactionManager transactionManager = currentServer.getTransactionManager();
                Transaction transaction = transactionManager.getTransaction();
                transaction.enlistResource(new TestResource(currentServer.getNodeName(), true));
                transaction.enlistResource(new TestResource(currentServer.getNodeName(), false) {
                    @Override
                    public synchronized void commit(Xid id, boolean onePhase) throws XAException {
                        assertTrue(resource1prepared[0]);
                        super.commit(id, onePhase);
                    }
                });
            }
        }

        {
            LocalServer originalServer = getLocalServer("1000");
            TransactionManager transactionManager = originalServer.getTransactionManager();
            transactionManager.resume(originalTransaction);
            XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", requiresProxyAtPreviousServer);
            originalTransaction.enlistResource(proxyXAResource);
            originalTransaction.enlistResource(new TestResource(originalServer.getNodeName(), false) {
                @Override
                public synchronized int prepare(Xid xid) throws XAException, Error {
                    resource1prepared[0] = true;
                    return super.prepare(xid);
                }
            });
            transactionManager.commit();
        }
        Thread.currentThread().setContextClassLoader(classLoader);

        assertTrue("" + completionCounter.getCommitCount("1000"), completionCounter.getCommitCount("1000") == 2);
        assertTrue("" + completionCounter.getCommitCount("2000"), completionCounter.getCommitCount("2000") == 1);
        assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 0);
    }

    @Test
    public void testOnePhaseCommit() throws Exception {
        System.out.println("testOnePhaseCommit");
        LocalServer originalServer = getLocalServer("1000");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
        TransactionManager transactionManager = originalServer.getTransactionManager();
        transactionManager.setTransactionTimeout(0);
        transactionManager.begin();
        Transaction originalTransaction = transactionManager.getTransaction();
        int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
        Xid currentXid = originalServer.getCurrentXid();
        transactionManager.suspend();
        DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(new LinkedList<String>(Arrays.asList(new String[]{"2000"})),
                remainingTimeout, currentXid, 1, false, false);
        transactionManager.resume(originalTransaction);
        XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
        originalTransaction.enlistResource(proxyXAResource);
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
        LocalServer originalServer = getLocalServer("1000");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
        TransactionManager transactionManager = originalServer.getTransactionManager();
        transactionManager.setTransactionTimeout(0);
        transactionManager.begin();
        Transaction originalTransaction = transactionManager.getTransaction();
        int remainingTimeout = (int) (originalServer.getTimeLeftBeforeTransactionTimeout() / 1000);
        Xid currentXid = originalServer.getCurrentXid();
        transactionManager.suspend();
        DataReturnedFromRemoteServer performTransactionalWork = performTransactionalWork(new LinkedList<String>(Arrays.asList(new String[]{"2000"})),
                remainingTimeout, currentXid, 1, false, false);
        transactionManager.resume(originalTransaction);
        XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", performTransactionalWork.getProxyRequired());
        originalTransaction.enlistResource(proxyXAResource);
        originalTransaction.registerSynchronization(originalServer.generateProxySynchronization("2000", currentXid));
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
                Arrays.asList(new String[]{"1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000"}));
        doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, true, true);
    }

    @Test
    public void testMigrateTransactionRollbackOnlyRollback() throws Exception {
        System.out.println("testMigrateTransactionRollbackOnlyRollback");
        int startingTimeout = 0;
        List<String> nodesToFlowTo = new LinkedList<String>(
                Arrays.asList(new String[]{"1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000"}));
        doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, false, true);
    }

    @Test
    public void testMigrateTransactionCommit() throws Exception {
        System.out.println("testMigrateTransactionCommit");
        int startingTimeout = 0;
        List<String> nodesToFlowTo = new LinkedList<String>(
                Arrays.asList(new String[]{"1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000"}));
        doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, true, false);
    }

    @Test
    public void testMigrateTransactionCommitDiamond() throws Exception {
        System.out.println("testMigrateTransactionCommitDiamond");

        int startingTimeout = 0;
        List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(new String[]{"1000", "2000", "1000", "3000", "1000", "2000", "3000"}));
        doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, true, false);
    }

    @Test
    public void testMigrateTransactionRollback() throws Exception {
        System.out.println("testMigrateTransactionRollback");
        int startingTimeout = 0;
        List<String> nodesToFlowTo = new LinkedList<String>(
                Arrays.asList(new String[]{"1000", "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000"}));
        doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, false, false);
    }

    @Test
    public void testMigrateTransactionRollbackDiamond() throws Exception {
        System.out.println("testMigrateTransactionRollbackDiamond");
        int startingTimeout = 0;
        List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(new String[]{"1000", "2000", "1000", "3000", "1000", "2000", "3000"}));
        doRecursiveTransactionalWork(startingTimeout, nodesToFlowTo, false, false);
    }

    @Test
    public void testMigrateTransactionSubordinateTimeout() throws Exception {
        System.out.println("testMigrateTransactionSubordinateTimeout");
        tearDown();
        setup();
        int rootTimeout = 10000;
        int subordinateTimeout = 1; // artificially low to ensure the timeout is performed by the subordinate
        LocalServer originalServer = getLocalServer("1000");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
        TransactionManager transactionManager = originalServer.getTransactionManager();
        transactionManager.setTransactionTimeout(rootTimeout);
        transactionManager.begin();
        Transaction originalTransaction = transactionManager.getTransaction();
        Xid currentXid = originalServer.getCurrentXid();
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
        Thread.sleep((subordinateTimeout + 2) * 1000);
        try {
            transactionManager.commit();
            fail("Did not rollback");
        } catch (RollbackException rbe) {
            // GOOD!
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        assertTrue("" + completionCounter.getRollbackCount("2000"), completionCounter.getRollbackCount("2000") == 1);
        // The second rollback invocation (from the proxyXAResource) is ignored because of JBTM-3843, that's why the following condition is == 1
        assertTrue("" + completionCounter.getRollbackCount("1000"), completionCounter.getRollbackCount("1000") == 1);
    }

    @Test
    public void testMigrateTransactionParentTimeout() throws Exception {
        System.out.println("testMigrateTransactionParentTimeout");
        tearDown();
        setup();
        int rootTimeout = 5;
        int subordinateTimeout = 10; // artificially high to ensure the timeout is performed by the parent
        LocalServer originalServer = getLocalServer("1000");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());
        TransactionManager transactionManager = originalServer.getTransactionManager();
        transactionManager.setTransactionTimeout(rootTimeout);
        transactionManager.begin();
        Transaction originalTransaction = transactionManager.getTransaction();
        Xid currentXid = originalServer.getCurrentXid();
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
        System.out.println(new Date() + " resuming");
        transactionManager.resume(originalTransaction);
        System.out.println(new Date() + " generating");
        XAResource proxyXAResource = originalServer.generateProxyXAResource("2000", migratedXid);
        System.out.println(new Date() + " enlisting");
        originalTransaction.enlistResource(proxyXAResource);
        System.out.println(new Date() + " sleeping");
        Thread.sleep(rootTimeout * 2000);
        try {
            System.out.println(new Date() + " committing");
            transactionManager.commit();
            fail("Committed a transaction that should have rolled back");
        } catch (RollbackException rbe) {
            // This is OK
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        assertTrue("" + completionCounter.getCommitCount("2000"),
                completionCounter.getCommitCount("2000") == 0);
        assertTrue("" + completionCounter.getCommitCount("1000"),
                completionCounter.getCommitCount("1000") == 0);
        assertTrue("" + completionCounter.getRollbackCount("2000"),
                completionCounter.getRollbackCount("2000") == 1);
        assertTrue("" + completionCounter.getRollbackCount("1000"),
                completionCounter.getRollbackCount("1000") == 2);
    }

    private void doRecursiveTransactionalWork(int startingTimeout, List<String> nodesToFlowTo, boolean commit, boolean rollbackOnlyOnLastNode) throws Exception {
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
        transactionManager.suspend();
        DataReturnedFromRemoteServer dataReturnedFromRemoteServer = performTransactionalWork(nodesToFlowTo, remainingTimeout, currentXid, 1, true,
                rollbackOnlyOnLastNode);
        transactionManager.resume(transaction);

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

    private void doRecursiveTransactionalWork2(int startingTimeout, List<String> nodesToFlowTo, boolean commit, boolean rollbackOnlyOnLastNode) throws Exception {
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
        transactionManager.suspend();
        DataReturnedFromRemoteServer dataReturnedFromRemoteServer = performTransactionalWork2(nodesToFlowTo, remainingTimeout, currentXid, 1, true,
                rollbackOnlyOnLastNode);
        transactionManager.resume(transaction);

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
                assertTrue("" + completionCounter.getTotalCommitCount(), completionCounter.getTotalCommitCount() == 2);
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

    private DataReturnedFromRemoteServer performTransactionalWork2(List<String> nodesToFlowTo, int remainingTimeout, Xid toMigrate,
                                                                   int numberOfResourcesToRegister, boolean addSynchronization, boolean rollbackOnlyOnLastNode) throws RollbackException, IllegalStateException,
            XAException, SystemException, NotSupportedException, IOException {
        String currentServerName = nodesToFlowTo.remove(0);
        LocalServer currentServer = getLocalServer(currentServerName);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(currentServer.getClassLoader());

        Xid requiresProxyAtPreviousServer = currentServer.locateOrImportTransactionThenResumeIt(remainingTimeout, toMigrate);

        // Perform work on the migrated transaction

        if (!currentServerName.equals("1000")) {
            TransactionManager transactionManager = currentServer.getTransactionManager();
            Transaction transaction = transactionManager.getTransaction();
            if (addSynchronization) {
                transaction.registerSynchronization(new TestSynchronization(currentServer.getNodeName()));
            }
            for (int i = 0; i < numberOfResourcesToRegister; i++) {
                transaction.enlistResource(new TestResource(currentServer.getNodeName(), false, currentServerName.equals("2000")));
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

                DataReturnedFromRemoteServer dataReturnedFromRemoteServer = performTransactionalWork2(nodesToFlowTo, remainingTimeout, currentXid,
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
                    XAResource proxyXAResource = currentServer.generateProxyXAResource(nextServerNodeName, dataReturnedFromRemoteServer.getProxyRequired(), true);
                    transaction.enlistResource(proxyXAResource);
                    transaction.registerSynchronization(currentServer.generateProxySynchronization(nextServerNodeName, toMigrate));
                }

                if (currentServerName.equals("1000")) {
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

    private LocalServer getLocalServer(String jndiName) {
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