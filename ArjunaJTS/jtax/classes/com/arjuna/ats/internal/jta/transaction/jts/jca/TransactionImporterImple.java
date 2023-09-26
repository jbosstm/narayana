/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts.jca;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateXidImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import org.jboss.tm.TransactionImportResult;

public class TransactionImporterImple implements TransactionImporter
{
	
	/**
	 * Create a subordinate transaction associated with the
	 * global transaction inflow. No timeout is associated with the
	 * transaction.
	 * 
	 * @param xid the global transaction.
	 * 
	 * @return the subordinate transaction.
	 * 
	 * @throws XAException thrown if there are any errors.
	 */
	
	public SubordinateTransaction importTransaction (Xid xid) throws XAException
	{
		return (SubordinateTransaction) importRemoteTransaction(xid, 0).getTransaction();
	}

    @Override
    public SubordinateTransaction importTransaction(Xid xid, int timeout) throws XAException {
        return (SubordinateTransaction) importRemoteTransaction(xid, timeout).getTransaction();
    }

    /**
	 * Create a subordinate transaction associated with the
	 * global transaction inflow and having a specified timeout.
	 * 
	 * @param xid the global transaction.
	 * @param timeout the timeout associated with the global transaction.
	 * 
	 * @return the subordinate transaction.
	 * 
	 * @throws XAException thrown if there are any errors.
	 */
	
	public TransactionImportResult importRemoteTransaction(Xid xid, int timeout) throws XAException
	{
		if (xid == null)
			throw new IllegalArgumentException();

		return addImportedTransaction(null, new SubordinateXidImple(xid), xid, timeout);
	}

	public SubordinateTransaction recoverTransaction (Uid actId) throws XAException
	{
		if (actId == null)
			throw new IllegalArgumentException();
		
		TransactionImple recovered = new TransactionImple(actId);
		
		if (recovered.baseXid() == null)
		    throw new IllegalArgumentException();

		return (SubordinateTransaction) addImportedTransaction(recovered, recovered.baseXid(), null, 0).getTransaction();
	}
    
	/**
	 * Get the subordinate (imported) transaction associated with the
	 * global transaction.
	 * 
	 * @param xid the global transaction.
	 * 
	 * @return the subordinate transaction or <code>null</code> if there
	 * is none.
	 * 
	 * @throws XAException thrown if there are any errors.
	 */
	
	public SubordinateTransaction getImportedTransaction (Xid xid) throws XAException
	{
		if (xid == null)
			throw new IllegalArgumentException();

		AtomicReference<SubordinateTransaction> holder = _transactions.get(new SubordinateXidImple(xid));
		SubordinateTransaction tx = holder == null ? null : holder.get();

		if (tx == null) {
			/*
			 * Remark: if holder != null and holder.get() == null then the setter is about to
			 * import the transaction but has not yet updated the holder. We implement the getter
			 * (the thing that is trying to terminate the imported transaction) as though the imported
			 * transaction only becomes observable when it has been fully imported.
			 */
			return null;
		}

		// https://issues.jboss.org/browse/JBTM-927
		try {
			if (tx.getStatus() == jakarta.transaction.Status.STATUS_ROLLEDBACK) {
				throw new XAException(XAException.XA_RBROLLBACK);
			}
		} catch (SystemException e) {
			throw new XAException(XAException.XA_RBROLLBACK);
		}

		if (tx.baseXid() == null)
		{
			/*
			 * Try recovery again. If it fails we'll throw a RETRY to the caller who
			 * should try again later.
			 */
            tx.recover();

			return tx;
		}
		else
			return tx;
	}

	/**
	 * Remove the subordinate (imported) transaction.
	 * 
	 * @param xid the global transaction.
	 * 
	 * @throws XAException thrown if there are any errors.
	 */
	
	public void removeImportedTransaction (Xid xid) throws XAException
	{
		if (xid == null)
			throw new IllegalArgumentException();

		AtomicReference<SubordinateTransaction> remove = _transactions.remove(new SubordinateXidImple(xid));
		if (remove != null) {
            synchronized (remove) {
                com.arjuna.ats.internal.jta.transaction.jts.TransactionImple transactionImple = (com.arjuna.ats.internal.jta.transaction.jts.TransactionImple) remove.get();
                while (transactionImple == null) {
                    try {
                        remove.wait();
                        transactionImple = (com.arjuna.ats.internal.jta.transaction.jts.TransactionImple) remove.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new XAException(XAException.XAER_RMFAIL);
                    }
                }
                TransactionImple.removeTransaction(transactionImple);
            }
        }
	}

	/**
	 * This can be used for newly imported transactions or recovered ones.
	 *
	 * @param recoveredTransaction If this is recovery
	 * @param xid if this is import
	 * @param timeout
	 * @return
	 */
	private TransactionImportResult addImportedTransaction(TransactionImple recoveredTransaction, Xid mapKey, Xid xid, int timeout) {
		boolean isNew = false;
		SubordinateXidImple importedXid = new SubordinateXidImple(mapKey);
		// We need to store the imported transaction in a volatile field holder so that it can be shared between threads
		AtomicReference<SubordinateTransaction> holder = new AtomicReference<>();
		AtomicReference<SubordinateTransaction> existing;

		if ((existing = _transactions.putIfAbsent(importedXid, holder)) != null) {
			holder = existing;
		}

		SubordinateTransaction txn = holder.get();

		// Should only be called by the recovery system - this will replace the Transaction with one from disk
		if (recoveredTransaction!= null) {
			synchronized (holder) {
				// now it's safe to add the imported transaction to the holder
				recoveredTransaction.recordTransaction();
				txn = recoveredTransaction;
				holder.set(txn);
				holder.notifyAll();
			}
		}

		if (txn == null) {
			// retry the get under a lock - this double check idiom is safe because AtomicReference is effectively
			// a volatile so can be concurrently accessed by multiple threads
			synchronized (holder) {
				txn = holder.get();
				if (txn == null) {
					txn = new TransactionImple(timeout, xid);
					holder.set(txn);
					holder.notifyAll();
					isNew = true;
				}
			}
		}

		return new TransactionImportResult(txn, isNew);
	}

	private static ConcurrentHashMap<Xid, AtomicReference<SubordinateTransaction>> _transactions = new ConcurrentHashMap<>();
}