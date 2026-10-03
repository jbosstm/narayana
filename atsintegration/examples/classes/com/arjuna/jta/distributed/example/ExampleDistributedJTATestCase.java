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
package com.arjuna.jta.distributed.example;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.jboss.tm.TransactionTimeoutConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.jta.distributed.example.server.IsolatableServersClassLoader;
import com.arjuna.jta.distributed.example.server.LocalServer;
import com.arjuna.jta.distributed.example.server.LookupProvider;

/**
 * This example shows how to use the JTA in a distributed manner.
 * 
 * In this example, LocalServer references should be considered to be activities
 * that are performed on a local application server.
 * 
 * The method propagateTransaction is used to simulate invoking a remote server
 * and should be considered the socket boundary between servers. If you look
 * closely what I do to simulate this is use ClassLoaders so that servers dont
 * share the same address space in the VM and won't therefore interfere with
 * each other - inspired!
 * 
 * Note the use of LocalServer and RemoteServer is just an example, the
 * transport is responsible for creating objects that perform similar
 * capabilities to these.
 * 
 * Note that calls to getting the remaining time of a transaction may
 * programatic configurably trigger a rollback exception which is good for
 * certain situations, the example though guards "migrations" by checking their
 * state before propagation - I recommend all transports do the same.
 * 
 * IMPORTANT: Although this example shows points at which the transport is
 * expected to persist data, it does not define concretely the mechanisms to do
 * so, nor should it be considered sufficient for reliably persisting this data.
 * For instance, we do not flush to disk.
 */
public class ExampleDistributedJTATestCase {
	/**
	 * This is to simulate JNDI.
	 */
	private static LookupProvider lookupProvider = LookupProvider.getInstance();

	/**
	 * The list of server node names
	 */
	private static String[] serverNodeNames = new String[] { "1000", "2000", "3000" };

	/**
	 * A list of port offsets to use for the servers
	 */
	private static int[] serverPortOffsets = new int[] { 1000, 2000, 3000 };

	/**
	 * For each of the server nodes, a list of the other servers in the cluster
	 */
	private static String[][] clusterBuddies = new String[][] { new String[] { "2000", "3000" }, new String[] { "1000", "3000" },
			new String[] { "1000", "2000" } };

	/**
	 * The example stores a reference to all local servers as a convenience
	 */
	private static LocalServer[] localServers = new LocalServer[serverNodeNames.length];

	/**
	 * Initialise references to the local and remote servers.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws CoreEnvironmentBeanException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws NoSuchFieldException
	 */
	@BeforeClass
	public static void setup() throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, ClassNotFoundException,
			CoreEnvironmentBeanException, IOException, IllegalArgumentException, NoSuchFieldException {
		for (int i = 0; i < localServers.length; i++) {
			// Create each instance of a server with its own private
			// classloader, ensure all access to the local server instance is
			// done within the scope of this classloader, this is to simulate a
			// transports different address space
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			IsolatableServersClassLoader classLoaderForTransactionManager = new IsolatableServersClassLoader(null, ExampleDistributedJTATestCase.class
					.getPackage().getName(), contextClassLoader);
			IsolatableServersClassLoader classLoader = new IsolatableServersClassLoader(ExampleDistributedJTATestCase.class.getPackage().getName(), null,
					classLoaderForTransactionManager);
			localServers[i] = (LocalServer) classLoader.loadClass("com.arjuna.jta.distributed.example.server.impl.ServerImpl").newInstance();
			Thread.currentThread().setContextClassLoader(localServers[i].getClassLoader());
			localServers[i].initialise(lookupProvider, serverNodeNames[i], serverPortOffsets[i], clusterBuddies[i], classLoaderForTransactionManager);
			// This is a short cut, normally remote servers would not be the
			// same as the local servers and would be a tranport layer
			// abstraction
			lookupProvider.bind(i, localServers[i].connectTo());
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	/**
	 * This example starts a transaction at the local server, it then performs
	 * the steps required to propagate the transaction to a chain of remote
	 * servers.
	 * 
	 * The nodesToFlowTo is a test abstraction that allows the example to
	 * simulate conditional business logic that would propagate requests around
	 * the cluster to access various business logic silos.
	 * 
	 * @throws NotSupportedException
	 * @throws SystemException
	 * @throws IllegalStateException
	 * @throws RollbackException
	 * @throws XAException
	 * @throws SecurityException
	 * @throws HeuristicMixedException
	 * @throws HeuristicRollbackException
	 * @throws IOException
	 */
	@Test
	public void testMigrateTransaction() throws NotSupportedException, SystemException, IllegalStateException, RollbackException, XAException,
			SecurityException, HeuristicMixedException, HeuristicRollbackException, IOException {

		// The example does not set a timeout for transactions, we have unit
		// tests that do
		int startingTimeout = 0;

		// The list of further nodes to propagate this transaction through
		// These names are the transport allocated names, they happen to be
		// string forms of the transaction manager node name but if you follow
		// the code through you will see they could have been anything
		List<String> nodesToFlowTo = new LinkedList<String>(Arrays.asList(new String[] { "2000", "3000", "2000", "1000", "2000", "3000", "1000", "3000" }));

		// Start out at the first server
		LocalServer originalServer = localServers[0];
		// Access to this local server must be done by its own classloader to
		// ensure the servers remain separate
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(originalServer.getClassLoader());

		// THIS SIMULATES NORMAL BUSINESS LOGIC IN BMT/CMT (interceptors?)
		{
			// Get a reference on the transaction manager and create a
			// transaction
			TransactionManager transactionManager = originalServer.getTransactionManager();
			transactionManager.setTransactionTimeout(startingTimeout);
			transactionManager.begin();

			// Do some simulated local work on the TestResource and register a
			// TestSynchronization
			Transaction transaction = transactionManager.getTransaction();
			transaction.registerSynchronization(new TestSynchronization(originalServer.getNodeName()));
			transaction.enlistResource(new TestResource(originalServer.getNodeName()));
		}

		// This is where we start to propagate the transaction - it is all
		// transport related code - watch closely ;)
		if (!nodesToFlowTo.isEmpty()) {

			TransactionManager transactionManager = originalServer.getTransactionManager();
			Transaction transaction = transactionManager.getTransaction();
			int status = transaction.getStatus();

			// Only propagate active transactions - this may be inactive through
			// user code (rollback/setRollbackOnly) or it may be inactive due to
			// the transaction reaper
			if (status == Status.STATUS_ACTIVE) {
				// Stash away the root transaction, this is needed in case a
				// subordinate naughtily comes back to this server part way
				// through
				// so we can return the original transaction to them
				originalServer.storeRootTransaction();

				// Peek at the next node - this is just a test abstraction to
				// simulate where business logic might decide to access an EJB
				// at a
				// server with a remoting name
				String nextServerNodeName = nodesToFlowTo.get(0);

				// Check the remaining timeout - false is passed in so the call
				// doesn't raise a rollback exception
				int remainingTimeout = (int) (((TransactionTimeoutConfiguration) transactionManager).getTimeLeftBeforeTransactionTimeout(false) / 1000);
				// Get the Xid to propagate
				Xid currentXid = originalServer.getCurrentXid();
				// Suspend the transaction locally
				transactionManager.suspend();

				// WE CAN NOW PROPAGATE THE TRANSACTION
				DataReturnedFromRemoteServer dataReturnedFromRemoteServer = propagateTransaction(nodesToFlowTo, remainingTimeout, currentXid);

				// After the call retuns, resume the local transaction
				transactionManager.resume(transaction);
				// Enlist the proxy XA resource with the local transaction so
				// that
				// it can propagate the transaction completion events to the
				// subordinate
				transaction.enlistResource(originalServer.generateProxyXAResource(nextServerNodeName, dataReturnedFromRemoteServer.getRemoteXidCreated()));
				// Register a synchronization that can proxy the
				// beforeCompletion
				// event to the remote side, after completion events are the
				// responsibility of the remote server to initiate
				transaction.registerSynchronization(originalServer.generateProxySynchronization(nextServerNodeName, currentXid));

				// Deference the local copy of the current transaction so the GC
				// can
				// free it
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
			}
		}
		// Again - this is business logic in BMT/CMT (interceptors?)
		{
			TransactionManager transactionManager = originalServer.getTransactionManager();
			// Commit the local transaction!
			// This should propagate to the nodes required!
			transactionManager.commit();
		}
		// Reset the test classloader
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	/**
	 * This work is simulated to be performed in a remote server.
	 * 
	 * @param nodesToFlowTo
	 * @param remainingTimeout
	 * @param toMigrate
	 * @return
	 * @throws RollbackException
	 * @throws IllegalStateException
	 * @throws XAException
	 * @throws SystemException
	 * @throws NotSupportedException
	 * @throws IOException
	 */
	private DataReturnedFromRemoteServer propagateTransaction(List<String> nodesToFlowTo, int remainingTimeout, Xid toMigrate) throws RollbackException,
			IllegalStateException, XAException, SystemException, NotSupportedException, IOException {
		// Do some test setup to initialize this method as it if was being
		// invoked in a remote server
		String currentServerName = nodesToFlowTo.remove(0);
		// Do some work to convert the remote server name to an index against
		// the cache of local servers - clearly IRL this is not required as we
		// are at the server :)
		int index = (Integer.valueOf(currentServerName) / 1000) - 1;
		LocalServer currentServer = localServers[index];
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(currentServer.getClassLoader());

		// Check if this server has seen this transaction before - this is
		// crucial to ensure that calling servers will only lay down a proxy if
		// they are the first visitor to this server.
		Xid requiresProxyAtPreviousServer = currentServer.locateOrImportTransactionThenResumeIt(remainingTimeout, toMigrate);

		{
			// Perform work on the migrated transaction
			TransactionManager transactionManager = currentServer.getTransactionManager();
			Transaction transaction = transactionManager.getTransaction();
			// Do some simple work on local dummy resources and synchronizations
			transaction.registerSynchronization(new TestSynchronization(currentServer.getNodeName()));
			transaction.enlistResource(new TestResource(currentServer.getNodeName()));
		}

		// If there are any more nodes to simulate a flow to
		if (!nodesToFlowTo.isEmpty()) {

			TransactionManager transactionManager = currentServer.getTransactionManager();
			Transaction transaction = transactionManager.getTransaction();
			int status = transaction.getStatus();

			// Only propagate active transactions - this may be inactive through
			// user code (rollback/setRollbackOnly) or it may be inactive due to
			// the transaction reaper
			if (status == Status.STATUS_ACTIVE) {
				// Get the transport specific representation of the remote
				// server
				// name
				String nextServerNodeName = nodesToFlowTo.get(0);

				// Determine the remaining timeout to propagate
				remainingTimeout = (int) (((TransactionTimeoutConfiguration) transactionManager).getTimeLeftBeforeTransactionTimeout(false) / 1000);
				// Get the XID to propagate
				Xid currentXid = currentServer.getCurrentXid();
				// Suspend the transaction ready for propagation
				transactionManager.suspend();
				// Propagate the transaction - in the example I return a boolean
				// to
				// indicate whether this caller is the first client to establish
				// the
				// subordinate transaction at the remote node
				DataReturnedFromRemoteServer dataReturnedFromRemoteServer = propagateTransaction(nodesToFlowTo, remainingTimeout, currentXid);
				// Resume the transaction locally, ready for any more local work
				// and
				// to add the proxy resource and sync if needed
				transactionManager.resume(transaction);
				// If this caller was the first entity to propagate the
				// transaction
				// to the remote server
				if (dataReturnedFromRemoteServer.getRemoteXidCreated() != null) {
					// Formally enlist the resource
					transaction.enlistResource(currentServer.generateProxyXAResource(nextServerNodeName, dataReturnedFromRemoteServer.getRemoteXidCreated()));
					// Register a sync
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

		public Xid getRemoteXidCreated() {
			return proxyRequired;
		}

		public int getTransactionState() {
			return transactionState;
		}
	}
}
