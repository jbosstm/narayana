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
package com.arjuna.ats.jta.distributed.server.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.jboss.tm.ExtendedJBossXATerminator;
import org.jboss.tm.TransactionImportResult;
import org.jboss.tm.JBossXATerminator;
import org.jboss.tm.TransactionTimeoutConfiguration;

import com.arjuna.ats.arjuna.common.CoordinatorEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.RecoveryEnvironmentBean;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.utils.ManualProcessId;
import com.arjuna.ats.internal.jbossatx.jta.XAResourceRecordWrappingPluginImpl;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoveryXids;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import com.arjuna.ats.jbossatx.jta.TransactionManagerService;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.distributed.TestResourceRecovery;
import com.arjuna.ats.jta.distributed.server.LocalServer;
import com.arjuna.ats.jta.distributed.server.LookupProvider;
import com.arjuna.ats.jta.distributed.server.RemoteServer;

public class ServerImpl implements LocalServer {

	private String nodeName;
	private RecoveryManagerService recoveryManagerService;
	private TransactionManagerService transactionManagerService;
	private RecoveryManager _recoveryManager;
	private ClassLoader classLoaderForTransactionManager;

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
		recoveryModuleClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.SubordinateAtomicActionRecoveryModule");
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
				com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, null);
		actionStoreObjectStoreEnvironmentBean.setObjectStoreDir(System.getProperty("user.dir") + "/distributedjta-tests/tx-object-store/" + nodeName);

		ObjectStoreEnvironmentBean stateStoreObjectStoreEnvironmentBean = com.arjuna.common.internal.util.propertyservice.BeanPopulator.getNamedInstance(
				com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, "stateStore");
		stateStoreObjectStoreEnvironmentBean.setObjectStoreDir(System.getProperty("user.dir") + "/distributedjta-tests/tx-object-store/" + nodeName);

		ObjectStoreEnvironmentBean communicationStoreObjectStoreEnvironmentBean = com.arjuna.common.internal.util.propertyservice.BeanPopulator
				.getNamedInstance(com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean.class, "communicationStore");
		communicationStoreObjectStoreEnvironmentBean.setObjectStoreDir(System.getProperty("user.dir") + "/distributedjta-tests/tx-object-store/" + nodeName);

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
		xaResourceOrphanFilterClassNames.add("com.arjuna.ats.internal.jta.recovery.arjunacore.SubordinationManagerXAResourceOrphanFilter");
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
	public void shutdown() throws Exception {
		recoveryManagerService.stop();
		TransactionReaper.transactionReaper().terminate(false);
	}

	@Override
	public void doRecoveryManagerScan(boolean shortenSafetyInterval) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(getClassLoader());
		int originalSafetyInterval = -1;

		if (shortenSafetyInterval) {
			try {
				Field safetyIntervalMillis = RecoveryXids.class.getDeclaredField("safetyIntervalMillis");
				safetyIntervalMillis.setAccessible(true);
				originalSafetyInterval = (Integer) safetyIntervalMillis.get(null);
				safetyIntervalMillis.set(null, 0);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			try {
				Field safetyIntervalMillis = RecoveryXids.class.getDeclaredField("safetyIntervalMillis");
				safetyIntervalMillis.setAccessible(true);
				originalSafetyInterval = (Integer) safetyIntervalMillis.get(null);
				safetyIntervalMillis.set(null, 60000);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}

		_recoveryManager.scan();

		if (shortenSafetyInterval) {
			try {
				Field safetyIntervalMillis = RecoveryXids.class.getDeclaredField("safetyIntervalMillis");
				safetyIntervalMillis.setAccessible(true);
				safetyIntervalMillis.set(null, originalSafetyInterval);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} else {
			try {
				Field safetyIntervalMillis = RecoveryXids.class.getDeclaredField("safetyIntervalMillis");
				safetyIntervalMillis.setAccessible(true);
				safetyIntervalMillis.set(null, originalSafetyInterval);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	@Override
	public TransactionManager getTransactionManager() {
		return transactionManagerService.getTransactionManager();
	}

	@Override
	public Xid locateOrImportTransactionThenResumeIt(int remainingTimeout, Xid toResume) throws XAException, IllegalStateException, SystemException,
			IOException {
		JBossXATerminator xaTerminator = transactionManagerService.getJbossXATerminator();

		if (!ExtendedJBossXATerminator.class.isInstance(xaTerminator)) {
			System.out.printf("ExtendedJBossXATerminator: FAIL not an instance");
			return null;
		}

		ExtendedJBossXATerminator extendedJBossXATerminator = (ExtendedJBossXATerminator) xaTerminator;

		boolean subordinateCreated = false;
		Transaction transaction = extendedJBossXATerminator.getTransaction(toResume);

		if (transaction == null) {
			TransactionImportResult transactionImportResult = extendedJBossXATerminator.importTransaction(toResume, remainingTimeout);
			subordinateCreated = transactionImportResult.isNewImportedTransaction();
			transaction = transactionImportResult.getTransaction();
		}

		transactionManagerService.getTransactionManager().resume(transaction);

		return subordinateCreated ? ((com.arjuna.ats.jta.transaction.Transaction) transaction).getTxId() : null;
	}

	@Override
	public String getNodeName() {
		return nodeName;
	}

	@Override
	public long getTimeLeftBeforeTransactionTimeout() throws RollbackException {
		return ((TransactionTimeoutConfiguration) transactionManagerService.getTransactionManager()).getTimeLeftBeforeTransactionTimeout(true);
	}

	@Override
	public Xid getCurrentXid() throws SystemException {
		TransactionImple transaction = ((TransactionImple) transactionManagerService.getTransactionManager().getTransaction());
		return transaction.getTxId();
	}

	@Override
	public ProxyXAResource generateProxyXAResource(String remoteServerName, Xid migratedXid) throws SystemException, IOException {
		return new ProxyXAResource(nodeName, remoteServerName, migratedXid, false);
	}
	
	@Override
    public ProxyXAResource generateProxyXAResource(String remoteServerName, Xid migratedXid, boolean handleError) throws SystemException, IOException {
        return new ProxyXAResource(nodeName, remoteServerName, migratedXid, handleError);
    }

	@Override
	public Synchronization generateProxySynchronization(String remoteServerName, Xid toRegisterAgainst) {
		return new ProxySynchronization(nodeName, remoteServerName, toRegisterAgainst);
	}

	@Override
	public RemoteServer connectTo() {
		return new RemoteServerImpl();
	}

}
