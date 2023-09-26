/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.tm.TransactionImportResult;

/**
 * A TransactionImporter is used to manager the relationship with external SubordinateTransactions.
 */
public interface TransactionImporter
{
	/**
	 * Create a subordinate transaction associated with the global transaction
	 * inflow. No timeout is associated with the transaction.
	 * 
	 * @param xid
	 *            the global transaction.
	 * 
	 * @return the subordinate transaction.
	 * 
	 * @throws javax.transaction.xa.XAException
	 *             thrown if there are any errors.
	 */
    public SubordinateTransaction importTransaction(Xid xid) throws XAException;

	/**
	 * Create a subordinate transaction associated with the global transaction
	 * inflow and having a specified timeout.
	 * 
	 * @param xid
	 *            the global transaction.
	 * @param timeout
	 *            the timeout associated with the global transaction.
	 * 
	 * @return the subordinate transaction.
	 * 
	 * @throws javax.transaction.xa.XAException
	 *             thrown if there are any errors.
	 */
    public SubordinateTransaction importTransaction(Xid xid, int timeout) throws XAException;

    public TransactionImportResult importRemoteTransaction(Xid xid, int timeout) throws XAException;

	/**
	 * Used to recover an imported transaction.
	 * 
	 * @param actId
	 *            the state to recover.
	 * @return the recovered transaction object.
	 * @throws javax.transaction.xa.XAException
	 */
    public SubordinateTransaction recoverTransaction(Uid actId) throws XAException;

	/**
	 * Get the subordinate (imported) transaction associated with the global
	 * transaction.
	 * 
	 * @param xid
	 *            the global transaction.
	 * 
	 * @return the subordinate transaction or <code>null</code> if there is
	 *         none.
	 * 
	 * @throws javax.transaction.xa.XAException
	 *             thrown if there are any errors.
	 */
    public SubordinateTransaction getImportedTransaction(Xid xid) throws XAException;

	/**
	 * Remove the subordinate (imported) transaction.
	 * 
	 * @param xid
	 *            the global transaction.
	 * 
	 * @throws javax.transaction.xa.XAException
	 *             thrown if there are any errors.
	 */
    public void removeImportedTransaction(Xid xid) throws XAException;

}