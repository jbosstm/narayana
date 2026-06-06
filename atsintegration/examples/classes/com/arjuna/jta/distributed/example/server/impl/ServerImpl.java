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
package com.arjuna.jta.distributed.example.server.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.InvalidTransactionException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.jboss.tm.XAResourceRecovery;

import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.internal.arjuna.utils.ManualProcessId;
import com.arjuna.ats.internal.jbossatx.jta.XAResourceRecordWrappingPluginImpl;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateXidImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jbossatx.jta.TransactionManagerService;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.jta.distributed.example.TestResourceRecovery;
import com.arjuna.jta.distributed.example.server.LocalServer;
import com.arjuna.jta.distributed.example.server.LookupProvider;
import com.arjuna.jta.distributed.example.server.RemoteServer;

/**
 * Most of this class is self explanatory, the main part to take note of is how
 * <code>locateOrImportTransactionThenResumeIt</code>,
 * <code>storeRootTransaction</code> and <code>remoteRootTransaction</code>
 * interact with each other.
 * 
 * <p>
 * It is the responsibility of the root transaction manager to cache the root
 * transaction in a manner that incoming subordinates may be able to resume the
 * root transaction at that node, rather than creating a subordinate which would
 * be inefficient.
 */
public class ServerImpl implements LocalServer {

	private String nodeName;
	private RecoveryManagerService recoveryManagerService;
	private TransactionManagerService transactionManagerService;
	private Map<SubordinateXidImple, TransactionImple> rootTransactionsAsSubordinate = new HashMap<SubordinateXidImple, TransactionImple>();
	private RecoveryManager _recoveryManager;
	private ClassLoader classLoaderForTransactionManager;

	/**
	 * This is typically done by the application server.
	 * 
	 * The addition required for the distributed JTA code is:
	 * RecoveryManagerService::addSerializableXAResourceDeserializer()
	 * 
	 * You must also register with RecoveryManagerService::addXAResourceRecovery
	 * an {@link XAResourceRecovery} for your Proxy XA Resources so that they
	 * can find orphan subordinate transactions.
	 */
	public void initialise(LookupProvider lookupProvider, String nodeName, int portOffset, String[] clusterBuddies, ClassLoader classLoaderForTransactionManager)
			throws CoreEnvironmentBeanException, IOException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		this.nodeName = nodeName;
		this.classLoaderForTransactionManager = classLoaderForTransactionManager;

		RecoveryEnvironmentBean recoveryEnvironmentBean = com.arjuna.ats.arjuna.common.recoveryPropertyManager.getRecoveryEnvironmentBean();
		recoveryEnvironmentBean.setRecoveryBackoffPeriod(1);

		recoveryEnvironmentBean.setRecoveryInetAddress(InetAddress.getByName("localhost"));
		recoveryEnvironmentBean.setRecoveryPort(4712 + portOffset);
		recoveryEnvironmentBean.setTransactionStatusManagerInetAddress(InetAddress.getByName("localhost"));
		recoveryEnvironmentBean.setTransactionStatusManagerPort(4713 + portOffset);
		List<String> recoveryModuleClassNames = new ArrayList<String>();

		recoveryModuleClassNames.add("com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule");
		// recoveryModuleClassNames.add("com.arjuna.ats.internal.txoj.recovery.TORecoveryModule");
		recoveryModuleClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule");
		recoveryEnvironmentBean.setRecoveryModuleClassNames(recoveryModuleClassNames);
		List<String> expiryScannerClassNames = new ArrayList<String>();
		expiryScannerClassNames.add("com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner");
		recoveryEnvironmentBean.setExpiryScannerClassNames(expiryScannerClassNames);
		recoveryEnvironmentBean.setRecoveryActivators(null);

		CoreEnvironmentBean coreEnvironmentBean = com.arjuna.ats.arjuna.common.arjPropertyManager.getCoreEnvironmentBean();
		// coreEnvironmentBean.setSocketProcessIdPort(4714 + nodeName);
		coreEnvironmentBean.setNodeIdentifier(nodeName);
		// coreEnvironmentBean.setSocketProcessIdMaxPorts(1);
		coreEnvironmentBean.setProcessImplementationClassName(ManualProcessId.class.getName());
		coreEnvironmentBean.setPid(portOffset);

		CoordinatorEnvironmentBean coordinatorEnvironmentBean = com.arjuna.ats.arjuna.common.arjPropertyManager.getCoordinatorEnvironmentBean();
		coordinatorEnvironmentBean.setEnableStatistics(false);
		coordinatorEnvironmentBean.setDefaultTimeout(300);
		coordinatorEnvironmentBean.setTransactionStatusManagerEnable(false);
		coordinatorEnvironmentBean.setDefaultTimeout(0);

		ObjectStoreEnvironmentBean actionStoreObjectStoreEnvironmentBean = com.arjuna.common.internal.util.propertyservice.BeanPopulator.getNamedInstance(
				com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, "default");
		actionStoreObjectStoreEnvironmentBean.setObjectStoreDir(System.getProperty("user.dir") + "/distributedjta-examples/tx-object-store/" + nodeName);

		ObjectStoreEnvironmentBean stateStoreObjectStoreEnvironmentBean = com.arjuna.common.internal.util.propertyservice.BeanPopulator.getNamedInstance(
				com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, "stateStore");
		stateStoreObjectStoreEnvironmentBean.setObjectStoreDir(System.getProperty("user.dir") + "/distributedjta-examples/tx-object-store/" + nodeName);

		ObjectStoreEnvironmentBean communicationStoreObjectStoreEnvironmentBean = com.arjuna.common.internal.util.propertyservice.BeanPopulator
				.getNamedInstance(com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, "communicationStore");
		communicationStoreObjectStoreEnvironmentBean.setObjectStoreDir(System.getProperty("user.dir") + "/distributedjta-examples/tx-object-store/" + nodeName);

		JTAEnvironmentBean jTAEnvironmentBean = com.arjuna.ats.jta.common.jtaPropertyManager.getJTAEnvironmentBean();
		jTAEnvironmentBean.setLastResourceOptimisationInterface(org.jboss.tm.LastResource.class);
		jTAEnvironmentBean.setTransactionManagerClassName("com.arjuna.ats.jbossatx.jta.TransactionManagerDelegate");
		jTAEnvironmentBean.setUserTransactionClassName("com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple");
		jTAEnvironmentBean
				.setTransactionSynchronizationRegistryClassName("com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple");
		List<String> xaRecoveryNodes = new ArrayList<String>();
		xaRecoveryNodes.add(nodeName);
		jTAEnvironmentBean.setXaRecoveryNodes(xaRecoveryNodes);

		List<String> xaResourceOrphanFilterClassNames = new ArrayList<String>();

		xaResourceOrphanFilterClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.JTATransactionLogXAResourceOrphanFilter");
		xaResourceOrphanFilterClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.JTANodeNameXAResourceOrphanFilter");
		xaResourceOrphanFilterClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.SubordinateJTAXAResourceOrphanFilter");
		jTAEnvironmentBean.setXaResourceOrphanFilterClassNames(xaResourceOrphanFilterClassNames);
		jTAEnvironmentBean.setXAResourceRecordWrappingPlugin(new XAResourceRecordWrappingPluginImpl());

		recoveryManagerService = new RecoveryManagerService();
		recoveryManagerService.create();
		recoveryManagerService.addXAResourceRecovery(new TestResourceRecovery(nodeName));
		// This MUST be the last XAResourceRecovery class registered or you will
		// get unexpected recovery results, could add a specific interface for
		// this?
		recoveryManagerService.addXAResourceRecovery(new ProxyXAResourceRecovery(nodeName, clusterBuddies));
		recoveryManagerService.addSerializableXAResourceDeserializer(new ProxyXAResourceDeserializer());

		// recoveryManagerService.start();
		_recoveryManager = RecoveryManager.manager();
		RecoveryManager.manager().initialize();

		transactionManagerService = new TransactionManagerService();
		TxControl txControl = new com.arjuna.ats.arjuna.coordinator.TxControl();
		transactionManagerService.setJbossXATerminator(new com.arjuna.ats.internal.jbossatx.jta.jca.XATerminator());
		transactionManagerService
				.setTransactionSynchronizationRegistry(new com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple());
		transactionManagerService.create();
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoaderForTransactionManager;
	}

	@Override
	public RemoteServer connectTo() {
		return new RemoteServerImpl();
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public TransactionManager getTransactionManager() {
		return transactionManagerService.getTransactionManager();
	}

	@Override
	public Xid getCurrentXid() throws SystemException {
		TransactionImple transaction = ((TransactionImple) transactionManagerService.getTransactionManager().getTransaction());
		return transaction.getTxId();
	}

	/**
	 * This factory method is provided purely for the test purposes to ensure
	 * the correct classloader is used.
	 */
	@Override
	public ProxyXAResource generateProxyXAResource(String remoteServerName, Xid migratedXid) throws SystemException {
		return new ProxyXAResource(nodeName, remoteServerName, migratedXid);
	}

	/**
	 * This factory method is provided purely for the test purposes to ensure
	 * the correct classloader is used.
	 */
	@Override
	public Synchronization generateProxySynchronization(String remoteServerName, Xid toRegisterAgainst) {
		return new ProxySynchronization(nodeName, remoteServerName, toRegisterAgainst);
	}

	/**
	 * This method first checks a local <code>Map</code> to ensure that if the
	 * server being flowed to is actually where the root transaction resides
	 * then that transaction is resumed, rather than a subordinate created.
	 */
	@Override
	public Xid locateOrImportTransactionThenResumeIt(int remainingTimeout, Xid toResume) throws XAException, IllegalStateException, SystemException,
			InvalidTransactionException {
		Xid toReturn = null;
		Transaction transaction = rootTransactionsAsSubordinate.get(new SubordinateXidImple(toResume));
		if (transaction == null) {
			transaction = SubordinationManager.getTransactionImporter().getImportedTransaction(toResume);
			if (transaction == null) {
				transaction = SubordinationManager.getTransactionImporter().importTransaction(toResume, remainingTimeout);
				toReturn = ((TransactionImple) transaction).getTxId();
			}
		}
		transactionManagerService.getTransactionManager().resume(transaction);
		return toReturn;
	}

	/**
	 * Cache the root transaction, this is important if the transaction flows
	 * back to this node, then we want to associate resources with the root
	 * transaction, rather than a subordinate.
	 */
	@Override
	public void storeRootTransaction() throws SystemException {
		TransactionImple transaction = ((TransactionImple) transactionManagerService.getTransactionManager().getTransaction());
		Xid txId = transaction.getTxId();
		rootTransactionsAsSubordinate.put(new SubordinateXidImple(txId), transaction);
	}

	/**
	 * After the transaction completes, remove the transaction from the local
	 * cache. This could have been done by a <code>Synchronization</code>.
	 */
	@Override
	public void removeRootTransaction(Xid toMigrate) {
		rootTransactionsAsSubordinate.remove(new SubordinateXidImple(toMigrate));
	}
}
