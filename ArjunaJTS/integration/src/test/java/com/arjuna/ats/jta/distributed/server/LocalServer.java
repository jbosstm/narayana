/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed.server;

import java.io.IOException;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;

public interface LocalServer {

	public void initialise(LookupProvider lookupProvider, String nodeName, int portOffset, String[] clusterBuddies, ClassLoader classLoaderForTransactionManager)
			throws CoreEnvironmentBeanException, IOException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException;

	public ClassLoader getClassLoader();

	public String getNodeName();

	public TransactionManager getTransactionManager() throws NotSupportedException, SystemException;

	public void doRecoveryManagerScan(boolean shortenSafetyInterval);

	public long getTimeLeftBeforeTransactionTimeout() throws RollbackException;

	public Xid locateOrImportTransactionThenResumeIt(int remainingTimeout, Xid toImport) throws XAException, InvalidTransactionException,
			IllegalStateException, SystemException, IOException;

	public RemoteServer connectTo();

	public XAResource generateProxyXAResource(String remoteServerName, Xid xid) throws SystemException, IOException;

	public Synchronization generateProxySynchronization(String remoteServerName, Xid toRegisterAgainst);

	public Xid getCurrentXid() throws SystemException;

	public void shutdown() throws Exception;

    public XAResource generateProxyXAResource(String nextServerNodeName, Xid proxyRequired, boolean handleError) throws SystemException, IOException;
}