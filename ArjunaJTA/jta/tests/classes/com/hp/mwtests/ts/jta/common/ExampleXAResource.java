/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd.,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ExampleXAResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.common;

import javax.transaction.xa.*;

public abstract class ExampleXAResource implements XAResource
{

	public ExampleXAResource ()
	{
		/*
		 * Add this instance to a reaper, such that if it is still active when
		 * the timeout goes off, the reaper will roll back the work.
		 */
	}

	public void start (Xid xid, int flags) throws XAException
	{
		if (!validXid(xid))
			throw new XAException(XAException.XAER_NOTA);

		switch (flags)
		{
		case XAResource.TMNOFLAGS:
			associateNewUniqueTransaction(xid);
			break;
		case XAResource.TMJOIN:
			joinExistingTransaction(xid);
			break;
		case XAResource.TMRESUME:
			resumeOldTransaction(xid);
			break;
		default:
			throw new XAException(XAException.XAER_PROTO);
		}
	}

	public void end (Xid xid, int flags) throws XAException
	{
		if (!validXid(xid))
			throw new XAException(XAException.XAER_NOTA);

		switch (flags)
		{
		case XAResource.TMSUSPEND:
			temporarilySuspendBranch(xid);
			break;
		case XAResource.TMFAIL:
			endAssociationAndRollback(xid);
			break;
		case XAResource.TMSUCCESS:
			endAssociation(xid);
			break;
		default:
			throw new XAException(XAException.XAER_PROTO);
		}
	}

	public int prepare (Xid xid) throws XAException
	{
		if (!validXid(xid))
			throw new XAException(XAException.XAER_NOTA);

		if (!validResourceManager(xid))
			throw new XAException(XAException.XAER_RMFAIL);

		return resourceManagerPrepare(xid);
	}

	public void commit (Xid xid, boolean onePhase) throws XAException
	{
		if (!validXid(xid))
			throw new XAException(XAException.XAER_NOTA);

		if (!validResourceManager(xid))
			throw new XAException(XAException.XAER_RMFAIL);

		if (!onePhase && !resourceManagerPrepared(xid))
			throw new XAException(XAException.XAER_PROTO);

		resourceManagerCommit(xid, onePhase);
	}

	public void rollback (Xid xid) throws XAException
	{
		if (!validXid(xid))
			throw new XAException(XAException.XAER_NOTA);

		if (!validResourceManager(xid))
			throw new XAException(XAException.XAER_RMFAIL);

		resourceManagerRollback(xid);
	}

	public void forget (Xid xid) throws XAException
	{
		if (!validXid(xid))
			throw new XAException(XAException.XAER_NOTA);

		if (!validResourceManager(xid))
			throw new XAException(XAException.XAER_RMFAIL);

		if (!resourceManagerPrepared(xid))
			throw new XAException(XAException.XAER_PROTO);

		resourceManagerForget(xid);
	}

	public Xid[] recover (int flag) throws XAException
	{
		switch (flag)
		{
		case XAResource.TMNOFLAGS:
		{
			if (recoveryScanStarted())
			{
				return indoubtTransactions();
			}
			else
				throw new XAException(XAException.XAER_PROTO);
		}
		case XAResource.TMSTARTRSCAN:
		{
			if (recoveryScanStarted())
				throw new XAException(XAException.XAER_PROTO);
			else
				startRecoveryScan();
		}
			break;
		case XAResource.TMENDRSCAN:
		{
			if (recoveryScanStarted())
				endRecoveryScan();
			else
				throw new XAException(XAException.XAER_PROTO);
		}
			break;
		default:
		{
			if ((flag & XAResource.TMSTARTRSCAN & XAResource.TMENDRSCAN) != 0)
			{
				if (recoveryScanStarted())
					throw new XAException(XAException.XAER_PROTO);
				else
				{
					startRecoveryScan();

					Xid[] indoubts = indoubtTransactions();

					endRecoveryScan();

					return indoubts;
				}
			}
			else
				throw new XAException(XAException.XAER_PROTO);
		}
		}

		return null;
	}

	public int getTransactionTimeout () throws XAException
	{
		return timeout;
	}

	public boolean setTransactionTimeout (int seconds) throws XAException
	{
		if (seconds >= 0)
		{
			timeout = seconds;

			return true;
		}
		else
			return false;
	}

	public boolean isSameRM (XAResource xares) throws XAException
	{
		return (xares == this);
	}

	/*
	 * Determine whether or not this is an Xid instance we can deal with.
	 */

	protected abstract boolean validXid (Xid xid);

	/*
	 * Ensure that this Xid is unique within the context of this RM. If it is
	 * then associate the RM (and thread) with the transaction identified by the
	 * Xid, such that all work performed on the RM is transactional.
	 */

	protected abstract void associateNewUniqueTransaction (Xid xid)
			throws XAException;

	/*
	 * Ensure that this Xid is one we have seen before and is still active. If
	 * it is, then associate the RM as before. If the thread is already
	 * associated with a transaction, then throw an appropriate XAException.
	 */

	protected abstract void joinExistingTransaction (Xid xid)
			throws XAException;

	/*
	 * Ensure that this Xid is one we have seen before and have previously
	 * suspended. If it isn't then throw an XAException. If it is, then resume
	 * the association.
	 */

	protected abstract void resumeOldTransaction (Xid xid) throws XAException;

	/*
	 * Ensure that this Xid is one we know about. If it isn't then throw an
	 * XAException. If it is, then suspend the association of the RM (and
	 * thread) with the transaction. Suspend is different to ending, because the
	 * association can be (should be) resumed later or ended.
	 */

	protected abstract void temporarilySuspendBranch (Xid xid)
			throws XAException;

	/*
	 * Ensure that this Xid is one we know about. If it isn't then it's a
	 * protocol error and we should throw an appropriate XAException. If it is,
	 * then end the association with the RM.
	 */

	protected abstract void endAssociation (Xid xid) throws XAException;

	/*
	 * Ensure that this Xid is one we know about. If it isn't then it's a
	 * protocol error and we should throw an appropriate XAException. If it is,
	 * then end the association with the RM and roll back the work performed by
	 * the RM in the scope of that transaction.
	 */

	protected abstract void endAssociationAndRollback (Xid xid)
			throws XAException;

	/*
	 * Is there a valid RM associated with this transaction?
	 */

	protected abstract boolean validResourceManager (Xid xid);

	/*
	 * Find the RM associated with this transaction and ask it to prepare the
	 * work done in the scope of the transaction. Returns either OK or READ
	 * ONLY.
	 */

	protected abstract int resourceManagerPrepare (Xid xid) throws XAException;

	/*
	 * Find the RM associated with this transaction and ask it to commit the
	 * work done in the scope of the transaction.
	 */

	protected abstract void resourceManagerCommit (Xid xid, boolean onePhase)
			throws XAException;

	/*
	 * Find the RM associated with this transaction and ask it to roll back the
	 * work done in the scope of the transaction.
	 */

	protected abstract void resourceManagerRollback (Xid xid)
			throws XAException;

	/*
	 * Find the RM associated with this transaction and ask it to forget any
	 * heuristic information on behalf of the transaction.
	 */

	protected abstract void resourceManagerForget (Xid xid) throws XAException;

	/*
	 * Find the RM associated with this transaction and determine if it has been
	 * prepared.
	 */

	protected abstract boolean resourceManagerPrepared (Xid xid)
			throws XAException;

	/*
	 * Has the recovery scan begun?
	 */

	protected abstract boolean recoveryScanStarted () throws XAException;

	/*
	 * Return the list of indoubt (and heuristic) transactions.
	 */

	protected abstract Xid[] indoubtTransactions () throws XAException;

	/*
	 * Start the recovery scan.
	 */

	protected abstract void startRecoveryScan () throws XAException;

	/*
	 * End the recovery scan.
	 */

	protected abstract void endRecoveryScan () throws XAException;

	private int timeout = 0;

}
