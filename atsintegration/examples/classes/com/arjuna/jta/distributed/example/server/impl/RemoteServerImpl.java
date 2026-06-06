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

import java.util.Arrays;
import java.util.Set;

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporterImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import com.arjuna.jta.distributed.example.server.RemoteServer;

/**
 * This class could translate quite easily to a server-side network endpoint
 * interceptor for the client-side ProxyXAResource. One change that is required
 * is to remove the classloader behavior as that is purely to allow the test to
 * run multiple servers within a single VM. When reading this class, tend to
 * ignore the classloader work as that is test scaffolding.
 * 
 * <p>
 * In the normal situation, a ProxyXAResource is Serialized, therefore we do not
 * get the chance to recover the transactions in a call to
 * XAResource::recover(), therefore the ProxyXAResource must tell us when it
 * calls each method, whether or not to attempt to recover the transaction
 * before invoking its transactional directive.
 */
public class RemoteServerImpl implements RemoteServer {
	/**
	 * Remember to ignore the classloader shenanigans when reading the method.
	 * 
	 * @param recover
	 *            Should be set by the clients ProxyXAResource when the client
	 *            knows the remote side needs the transaction loading.
	 */
	@Override
	public int prepare(Xid xid, boolean recover) throws XAException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			if (recover) {
				((XATerminatorImple) SubordinationManager.getXATerminator()).doRecover(xid, null);
			}
			return SubordinationManager.getXATerminator().prepare(xid);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	/**
	 * Remember to ignore the classloader shenanigans when reading the method.
	 * 
	 * @param recover
	 *            Should be set by the clients ProxyXAResource when the client
	 *            knows the remote side needs the transaction loading.
	 */
	@Override
	public void commit(Xid xid, boolean onePhase, boolean recover) throws XAException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			if (recover) {
				((XATerminatorImple) SubordinationManager.getXATerminator()).doRecover(xid, null);
			}
			SubordinationManager.getXATerminator().commit(xid, onePhase);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	/**
	 * Remember to ignore the classloader shenanigans when reading the method.
	 * 
	 * @param recover
	 *            Should be set by the clients ProxyXAResource when the client
	 *            knows the remote side needs the transaction loading.
	 */
	@Override
	public void rollback(Xid xid, boolean recover) throws XAException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			if (recover) {
				((XATerminatorImple) SubordinationManager.getXATerminator()).doRecover(xid, null);
			}
			SubordinationManager.getXATerminator().rollback(xid);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	/**
	 * Remember to ignore the classloader shenanigans when reading the method.
	 * 
	 * @param recover
	 *            Should be set by the clients ProxyXAResource when the client
	 *            knows the remote side needs the transaction loading.
	 */
	@Override
	public void forget(Xid xid, boolean recover) throws XAException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			if (recover) {
				((XATerminatorImple) SubordinationManager.getXATerminator()).doRecover(xid, null);
			}
			SubordinationManager.getXATerminator().forget(xid);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}

	}

	/**
	 * Remember to ignore the classloader shenanigans when reading the method.
	 * 
	 * @param recover
	 *            Should be set by the clients ProxyXAResource when the client
	 *            knows the remote side needs the transaction loading.
	 * @throws SystemException
	 */
	@Override
	public void beforeCompletion(Xid xid) throws SystemException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			((XATerminatorImple) SubordinationManager.getXATerminator()).beforeCompletion(xid);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}

	/**
	 * Remember to ignore the classloader shenanigans when reading the method.
	 */
	@Override
	public Xid[] recoverFor(String localServerName) throws XAException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			Set<Xid> toReturn = ((TransactionImporterImple) SubordinationManager.getTransactionImporter()).getInflightXids(localServerName);
			Xid[] doRecover = ((XATerminatorImple) SubordinationManager.getXATerminator()).doRecover(null, localServerName);
			if (doRecover != null) {
				toReturn.addAll(Arrays.asList(doRecover));
			}
			return toReturn.toArray(new Xid[0]);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}
}
