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

import java.util.Arrays;
import java.util.Set;

import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporterImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import com.arjuna.ats.jta.distributed.server.RemoteServer;

public class RemoteServerImpl implements RemoteServer {
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

	@Override
	public int getTransactionCount() {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			return TransactionImple.getTransactions().size();
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}
}
