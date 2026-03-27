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
package com.arjuna.jta.distributed.example.server;

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * This interface attempts to illustrate the network endpoint requirements of
 * the remote transaction server with regards subordinate transactions.
 * 
 * <p>
 * Many of the methods are required to provide a recover parameter, this is
 * because when using Serializable ProxyXAResources, the recover method is not
 * invoked and therfore the remote server will not have had chance to recover
 * the subordinate transactions.
 */
public interface RemoteServer {

	/**
	 * Prepare the subordinate transaction
	 * 
	 * @param xid
	 * @param recover
	 * @return
	 * @throws XAException
	 */
	public int prepare(Xid xid, boolean recover) throws XAException;

	/**
	 * Commit the subordinate transaction.
	 * 
	 * @param xid
	 * @param onePhase
	 * @param recover
	 * @throws XAException
	 */
	public void commit(Xid xid, boolean onePhase, boolean recover) throws XAException;

	/**
	 * Rollback the subordinate transaction.
	 * 
	 * @param xid
	 * @param recover
	 * @throws XAException
	 */
	public void rollback(Xid xid, boolean recover) throws XAException;

	/**
	 * Forget a subordinate transaction.
	 * 
	 * @param xid
	 * @param recover
	 * @throws XAException
	 */
	public void forget(Xid xid, boolean recover) throws XAException;

	/**
	 * Proxy synchronizations will need to invoke this.
	 * 
	 * @param xid
	 * @throws SystemException
	 */
	public void beforeCompletion(Xid xid) throws SystemException;

	/**
	 * This is used by the ProxyXAResourceRecovery helper class to detect
	 * orphaned subordinate transactions.
	 * 
	 * @param localServerName
	 * @return
	 * @throws XAException
	 */
	public Xid[] recoverFor(String localServerName) throws XAException;

}
