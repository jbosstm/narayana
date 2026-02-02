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

import java.io.Serializable;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.XAResourceWrapper;

import com.arjuna.jta.distributed.example.server.LookupProvider;

/**
 * I chose for this class to implement XAResourceWrapper so that I can provide a
 * name to the Transaction manager for it to store in its XID.
 * 
 * <p>
 * In the normal situation, a ProxyXAResource is Serialized, therefore we do not
 * get the chance to recover the transactions in a call to
 * XAResource::recover(), therefore the ProxyXAResource must tell the remote
 * side when it calls each method, whether or not to attempt to recover the
 * transaction before invoking its transactional directive, this may be seen in
 * the prepare, commit, rollback and forget method.
 */
public class ProxyXAResource implements XAResource, XAResourceWrapper, Serializable {

	private int transactionTimeout;
	private String remoteServerName;
	private String localServerName;
	private transient boolean nonerecovered;

	private Xid migratedXid;

	/**
	 * Create a new proxy to the remote server.
	 * 
	 * @param LookupProvider
	 *            .getLookupProvider()
	 * @param localServerName
	 * @param remoteServerName
	 */
	public ProxyXAResource(String localServerName, String remoteServerName, Xid migratedXid) {
		this.localServerName = localServerName;
		this.remoteServerName = remoteServerName;
		this.migratedXid = migratedXid;
		this.nonerecovered = true;
	}

	/**
	 * Constructor for fallback bottom up recovery.
	 * 
	 * @param localServerName
	 * @param remoteServerName
	 */
	public ProxyXAResource(String localServerName, String remoteServerName) {
		this.localServerName = localServerName;
		this.remoteServerName = remoteServerName;
	}

	/**
	 * Store the XID.
	 */
	@Override
	public void start(Xid xid, int flags) throws XAException {
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_START   [" + xid + "]");
	}

	/**
	 * Reference the XID.
	 */
	@Override
	public void end(Xid xid, int flags) throws XAException {
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_END     [" + xid + "]");
	}

	/**
	 * This propagates the transaction directive in a transport specific manner.
	 */
	@Override
	public int prepare(Xid xid) throws XAException {
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_PREPARE [" + xid + "]");

		Xid toPropagate = migratedXid != null ? migratedXid : xid;
		int propagatePrepare = LookupProvider.getInstance().lookup(remoteServerName).prepare(toPropagate, !nonerecovered);
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_PREPARED");
		return propagatePrepare;
	}

	/**
	 * This propagates the transaction directive in a transport specific manner.
	 */
	@Override
	public void commit(Xid xid, boolean onePhase) throws XAException {
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_COMMIT  [" + xid + "]");

		Xid toPropagate = migratedXid != null ? migratedXid : xid;
		LookupProvider.getInstance().lookup(remoteServerName).commit(toPropagate, onePhase, !nonerecovered);
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_COMMITED");
	}

	/**
	 * This propagates the transaction directive in a transport specific manner.
	 */
	@Override
	public void rollback(Xid xid) throws XAException {
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_ROLLBACK[" + xid + "]");

		Xid toPropagate = migratedXid != null ? migratedXid : xid;
		LookupProvider.getInstance().lookup(remoteServerName).rollback(toPropagate, !nonerecovered);
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_ROLLBACKED");
	}

	/**
	 * This method is used when the local side is attempting to detect
	 * subordinate transactions that prepared remotely but the local
	 * ProxyXAResource has not prepared and are therefore orphaned.
	 */
	@Override
	public Xid[] recover(int flag) throws XAException {
		if ((flag & XAResource.TMSTARTRSCAN) == XAResource.TMSTARTRSCAN) {
			System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_RECOVER [XAResource.TMSTARTRSCAN]");
		}
		if ((flag & XAResource.TMENDRSCAN) == XAResource.TMENDRSCAN) {
			System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_RECOVER [XAResource.TMENDRSCAN]");
		}

		Xid[] toReturn = LookupProvider.getInstance().lookup(remoteServerName).recoverFor(localServerName);

		if (toReturn != null) {
			for (int i = 0; i < toReturn.length; i++) {
				System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_RECOVERD: " + toReturn[i]);
			}
		}
		return toReturn;
	}

	@Override
	public void forget(Xid xid) throws XAException {
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_FORGET  [" + xid + "]");

		Xid toPropagate = migratedXid != null ? migratedXid : xid;
		LookupProvider.getInstance().lookup(remoteServerName).forget(toPropagate, !nonerecovered);
		System.out.println("     ProxyXAResource (" + localServerName + ":" + remoteServerName + ") XA_FORGETED[" + xid + "]");
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return transactionTimeout;
	}

	@Override
	public boolean setTransactionTimeout(int seconds) throws XAException {
		this.transactionTimeout = seconds;
		return true;
	}

	@Override
	public boolean isSameRM(XAResource xares) throws XAException {
		boolean toReturn = false;
		if (xares instanceof ProxyXAResource) {
			if (((ProxyXAResource) xares).remoteServerName == remoteServerName) {
				toReturn = true;
			}
		}
		return toReturn;
	}

	/**
	 * Not used by the TM.
	 */
	@Override
	public XAResource getResource() {
		return null;
	}

	/**
	 * Not used by the TM.
	 */
	@Override
	public String getProductName() {
		return null;
	}

	/**
	 * Not used by the TM.
	 */
	@Override
	public String getProductVersion() {
		return null;
	}

	/**
	 * This allows the proxy to contain meaningful information in the XID in
	 * case of failure to recover.
	 */
	@Override
	public String getJndiName() {
		return "ProxyXAResource: " + localServerName + " " + remoteServerName;
	}
}
