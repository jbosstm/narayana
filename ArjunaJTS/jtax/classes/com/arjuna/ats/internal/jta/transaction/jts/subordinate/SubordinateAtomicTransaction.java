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
 * Copyright (C) 2003,
 * 
 * Hewlett-Packard Arjuna Labs, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: SubordinateAtomicTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts.subordinate;

import javax.transaction.Status;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.WrongTransaction;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

/**
 * A subordinate JTA transaction; used when importing another
 * transaction context.
 * 
 * @author mcl
 */

public class SubordinateAtomicTransaction extends com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction
{

	public SubordinateAtomicTransaction (ControlWrapper tx)
	{
		super(tx);
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.atomictxnobegin
	 *          [com.arjuna.ats.internal.jta.transaction.jts.atomictxnobegin]
	 *          Cannot call begin on imported AtomicTransaction instance.
	 */

	/**
	 * Does not change thread-to-tx association as base class commit does.
	 */

	public synchronized void end (boolean report_heuristics)
			throws NoTransaction, HeuristicMixed, HeuristicHazard,
			WrongTransaction, SystemException
	{
		throw new WrongTransaction();
	}
	
	/**
	 * Does not change thread-to-tx association as base class rollback does.
	 */

	public synchronized void abort () throws NoTransaction, WrongTransaction,
			SystemException
	{
		throw new WrongTransaction();
	}
	
	/**
	 * 
	 * @return TwoPhaseOutcome
	 * @throws SystemException
	 */
	
	public int doPrepare () throws SystemException
	{
		ServerTransaction stx = getTransaction();
		
		// TODO make sure synchronizations go off
		// require registration of synchronization interposed resource as well!
		
		if (stx != null)
                    return stx.doPrepare();
            else
                    return TwoPhaseOutcome.INVALID_TRANSACTION;
	}
	
	/**
	 * 
	 * @return ActionStatus
	 * @throws SystemException
	 */
	
	public int doCommit () throws SystemException
	{	
		ServerTransaction stx = getTransaction();
		int outcome = ActionStatus.INVALID;
		
		try
		{
		    if (stx != null)
                        return stx.doPhase2Commit();
		}
		catch (final Exception ex)
		{
		    ex.printStackTrace();
		}

		return ActionStatus.H_HAZARD;
	}
	
	/**
	 * 
	 * @return ActionStatus
	 * @throws SystemException
	 */
	
	public int doRollback () throws SystemException
	{
		ServerTransaction stx = getTransaction();
		int outcome = ActionStatus.INVALID;
		
		try
		{	
		    if (stx != null)
                        return stx.doPhase2Abort();
		}
		catch (final Exception ex)
		{
		    ex.printStackTrace();
		}
		
                return ActionStatus.H_HAZARD;
	}
	
	public int doOnePhaseCommit () throws SystemException
	{
	    // https://jira.jboss.org/jira/browse/JBTM-504
	    
	    try
	    {
	        // TODO check if we should be using TxControl.isBeforeCompletionWhenRollbackOnly in local JTA too.
	        
	        ServerTransaction stx = getTransaction();

	        if (stx != null)
	            stx.doCommit(true);
	    }
	    catch (final INVALID_TRANSACTION ex)
	    {
	        return ActionStatus.INVALID;
	    }
	    catch (final TRANSACTION_ROLLEDBACK ex)
	    {
	        return ActionStatus.ABORTED;
	    }
	    catch (final UNKNOWN ex)
	    {
	        return ActionStatus.COMMITTING;  // recovery to kick in.
	    }
	    catch (final Exception ex)
	    {
	        return ActionStatus.H_HAZARD;
	    }

	    return ActionStatus.COMMITTED;
	}
	
	public void doForget () throws SystemException
	{
		ServerTransaction stx = getTransaction();
		
		try
		{	
			if (stx != null)
				stx.doForget();
		}
		catch (final Exception ex)
		{
		    ex.printStackTrace();
		}
	}
	
	public boolean doBeforeCompletion () throws SystemException
	{
	    ServerTransaction stx = getTransaction();
	    
	    try
	    {
	        if (stx != null)
	        {
	            stx.doBeforeCompletion();
	            
	            return true;
	        }
	    }
	    catch (final Exception ex)
	    {
	        ex.printStackTrace();
	    }
	           
            return false;
	}
	
	/**
	 * By default the BasicAction class only allows the termination of a
	 * transaction if it's the one currently associated with the thread. We
	 * override this here.
	 * 
	 * @return <code>false</code> to indicate that this transaction can only
	 *         be terminated by the right thread.
	 */

	protected boolean checkForCurrent ()
	{
		return false;
	}

	private ServerTransaction getTransaction ()
	{
		ServerControlWrapper scw = (ServerControlWrapper) super._theAction;
		ServerControl sc = (ServerControl) scw.getImple();
		
		return (ServerTransaction) sc.getImplHandle();
	}
	
	    /*
	     * We have these here because it's possible that synchronizations aren't
	     * called explicitly either side of commit/rollback due to JCA API not supporting
	     * them directly. We do though and in which case it's possible that they
	     * can be driven through two routes and we don't want to get into a mess
	     * due to that.
	     */
	    
	    private boolean _doneBefore = false;
	    private boolean _beforeOutcome = false;
}
