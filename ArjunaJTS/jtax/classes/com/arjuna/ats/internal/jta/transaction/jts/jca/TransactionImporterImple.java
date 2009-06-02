/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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

package com.arjuna.ats.internal.jta.transaction.jts.jca;

import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.xa.*;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.CleanupSynchronization;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.jta.xa.XidImple;

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
		return importTransaction(xid, 0);
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
	
	public SubordinateTransaction importTransaction (Xid xid, int timeout) throws XAException
	{
		if (xid == null)
			throw new IllegalArgumentException();
		
		/*
		 * Check to see if we haven't already imported this thing.
		 */
		
		SubordinateTransaction imported = getImportedTransaction(xid);
		
		if (imported == null)
		{	
			imported = new TransactionImple(timeout, xid);
			
			_transactions.put(new XidImple(xid), imported);
		}
		
		/*
                 * Register the cleanup synchronization immediately.
                 */
                
                try
                {
                    imported.registerSynchronization(new CleanupSynchronization(xid));
                }
                catch (final SystemException ex)
                {
                    throw new XAException(XAException.XAER_RMERR);
                }
                catch (final RollbackException ex)
                {
                    throw new XAException(XAException.XA_RBROLLBACK);
                }
                
		return imported;
	}

	public SubordinateTransaction recoverTransaction (Uid actId) throws XAException
	{
		if (actId == null)
			throw new IllegalArgumentException();
		
		TransactionImple recovered = new TransactionImple(actId);
		TransactionImple tx = (TransactionImple) _transactions.get(recovered.baseXid());

		if (tx == null)
		{
			recovered.recordTransaction();

			_transactions.put(recovered.baseXid(), recovered);
			
			return recovered;
		}
		else
			return tx;
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
		
		SubordinateTransaction tx = _transactions.get(new XidImple(xid));
		
		if (tx == null)
			return null;

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

		_transactions.remove(new XidImple(xid));
	}
	
	private static ConcurrentHashMap<Xid, SubordinateTransaction> _transactions = new ConcurrentHashMap<Xid, SubordinateTransaction>();
	
}
