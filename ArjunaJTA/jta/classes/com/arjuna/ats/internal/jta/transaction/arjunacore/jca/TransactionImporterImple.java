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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionImporterImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;
import org.jboss.tm.TransactionImportResult;

public class TransactionImporterImple implements TransactionImporter
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

	public SubordinateTransaction importTransaction(Xid xid)
			throws XAException
	{
		return (SubordinateTransaction) importTransaction(xid, 0).getTransaction();
	}

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

	public TransactionImportResult importTransaction(Xid xid, int timeout)
			throws XAException
	{
		if (xid == null)
			throw new IllegalArgumentException();

		/*
		 * the imported transaction map is keyed by xid and the xid used is the one created inside
		 * the TransactionImple ctor (it encodes the node name of this transaction manager) and is
		 * the one returned by TransactionImple#baseXid() so pass in the converted value (using convertXid).
		 */
		return addImportedTransaction(null, convertXid(xid), xid, timeout);
	}

	/**
	 * Used to recover an imported transaction.
	 * 
	 * @param actId
	 *            the state to recover.
	 * @return the recovered transaction object.
	 * @throws javax.transaction.xa.XAException
	 */

	public TransactionImple recoverTransaction(Uid actId)
			throws XAException
	{
		if (actId == null)
			throw new IllegalArgumentException();

		TransactionImple recovered = new TransactionImple(actId);

		if (recovered.baseXid() == null)
		    throw new IllegalArgumentException();
		
		/*
		 * Is the transaction already in the map? This may be the case because
		 * we scan the object store periodically and may get Uids to recover for
		 * transactions that are progressing normally, i.e., do not need
		 * recovery. In which case, we need to ignore them:
		 *
		 * ie calling addImportedTransaction with a non null value for recovered will
		 * call recovered.recordTransaction()
		 */

		return (TransactionImple) addImportedTransaction(recovered, recovered.baseXid(), null, 0).getTransaction();
	}

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

	public SubordinateTransaction getImportedTransaction(Xid xid)
			throws XAException
	{
		if (xid == null)
			throw new IllegalArgumentException();

		AtomicReference<TransactionImple> holder = _transactions.get(new SubordinateXidImple(xid));
		TransactionImple tx = holder == null ? null : holder.get();

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
			if (tx.getStatus() == javax.transaction.Status.STATUS_ROLLEDBACK) {
				throw new XAException(XAException.XA_RBROLLBACK);
			}
		} catch (SystemException e) {
			e.printStackTrace();
			throw new XAException(XAException.XA_RBROLLBACK);
		}

		if (!tx.activated())
		{
			tx.recover();

			return tx;
		}
		else
			return tx;
	}

	/**
	 * Remove the subordinate (imported) transaction.
	 * 
	 * @param xid
	 *            the global transaction.
	 * 
	 * @throws javax.transaction.xa.XAException
	 *             thrown if there are any errors.
	 */

	public void removeImportedTransaction(Xid xid) throws XAException
	{
		if (xid == null)
			throw new IllegalArgumentException();

		_transactions.remove(new SubordinateXidImple(xid));
	}
	
	public Set<Xid> getInflightXids(String parentNodeName) {
		Iterator<AtomicReference<TransactionImple>> iterator = _transactions.values().iterator();
		Set<Xid> toReturn = new HashSet<Xid>();
		while (iterator.hasNext()) {
			AtomicReference<TransactionImple> holder = iterator.next();
			TransactionImple imported = holder.get();

			if (imported != null && imported.getParentNodeName().equals(parentNodeName)) {
				toReturn.add(imported.baseXid());
			}
		}
		return toReturn;
	}

	/**
	 * This can be used for newly imported transactions or recovered ones.
	 *
	 * @param recoveredTransaction If this is recovery
	 * @param mapKey
	 * @param xid if this is import
	 * @param timeout
	 * @return
	 */
	private TransactionImportResult addImportedTransaction(TransactionImple recoveredTransaction, Xid mapKey, Xid xid, int timeout) {
		TransactionImportResult toReturn = new TransactionImportResult();
		SubordinateXidImple importedXid = new SubordinateXidImple(mapKey);
		// We need to store the imported transaction in a volatile field holder so that it can be shared between threads
		AtomicReference<TransactionImple> holder = new AtomicReference<>();
		AtomicReference<TransactionImple> existing;

		if ((existing = _transactions.putIfAbsent(importedXid, holder)) != null) {
			holder = existing;
		}

		TransactionImple txn = holder.get();

		// Should only be called by the recovery system - this will replace the Transaction with one from disk
		if (recoveredTransaction!= null) {
			synchronized (holder) {
				// now it's safe to add the imported transaction to the holder
				recoveredTransaction.recordTransaction();
				txn = recoveredTransaction;
				holder.set(txn);
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
					toReturn.setSubordinateCreated(true);
				}
			}
		}

		toReturn.setTransaction(txn);
		return toReturn;
	}

	private XidImple convertXid(Xid xid)
	{
		if (xid != null && xid.getFormatId() == XATxConverter.FORMAT_ID) {
			XidImple toImport = new XidImple(xid);
			XATxConverter.setSubordinateNodeName(toImport.getXID(), TxControl.getXANodeName());
			return new SubordinateXidImple(toImport);
		} else {
			return new XidImple(xid);
		}
	}

	private static ConcurrentHashMap<SubordinateXidImple, AtomicReference<TransactionImple>> _transactions =
			new ConcurrentHashMap<>();
}

