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
package com.arjuna.ats.jta.distributed.server;

import java.io.IOException;

import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
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

	public void doRecoveryManagerScan(boolean hackSafetyInterval);

	public long getTimeLeftBeforeTransactionTimeout() throws RollbackException;

	public void storeRootTransaction() throws SystemException;

	public void removeRootTransaction(Xid toMigrate);

	public Xid locateOrImportTransactionThenResumeIt(int remainingTimeout, Xid toImport) throws XAException, InvalidTransactionException,
			IllegalStateException, SystemException, IOException;

	public RemoteServer connectTo();

	public XAResource generateProxyXAResource(String remoteServerName, Xid xid) throws SystemException, IOException;

	public Synchronization generateProxySynchronization(String remoteServerName, Xid toRegisterAgainst);

	public Xid getCurrentXid() throws SystemException;

	public void shutdown() throws Exception;
}
