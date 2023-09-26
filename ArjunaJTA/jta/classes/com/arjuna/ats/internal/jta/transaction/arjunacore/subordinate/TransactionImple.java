/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate;

import java.io.IOException;

import jakarta.transaction.HeuristicCommitException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.xa.XAModifier;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

// https://jira.jboss.org/jira/browse/JBTM-384

public class TransactionImple extends
		com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple
{
	/**
	 * Create a new transaction with the specified timeout.
	 */

	public TransactionImple (int timeout)
	{
		this(new SubordinateAtomicAction(timeout));
	}

	// TODO use the timeout!

	public TransactionImple (AtomicAction act)
	{
		super(act);

		TransactionImple.putTransaction(this);
	}

	/**
	 * This is a subordinate transaction, so any attempt to commit it or roll it
	 * back directly, should fail.
	 */

	public void commit () throws jakarta.transaction.RollbackException,
			jakarta.transaction.HeuristicMixedException,
			jakarta.transaction.HeuristicRollbackException,
			java.lang.SecurityException, jakarta.transaction.SystemException,
			java.lang.IllegalStateException
	{
		throw new IllegalStateException( jtaLogger.i18NLogger.get_transaction_arjunacore_subordinate_invalidstate() );
	}

	/**
	 * This is a subordinate transaction, so any attempt to commit it or roll it
	 * back directly, should fail.
	 */

	public void rollback () throws java.lang.IllegalStateException,
			java.lang.SecurityException, jakarta.transaction.SystemException
	{
		throw new InvalidTerminationStateException( jtaLogger.i18NLogger.get_transaction_arjunacore_subordinate_invalidstate() );
	}

	// Should probably return XA status codes, c.f., XAResource.prepare

	public int doPrepare ()
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			if (!endSuspendedRMs())
				_theTransaction.preventCommit();

			int res = subAct.doPrepare();

			switch (res)
			{
			case TwoPhaseOutcome.PREPARE_READONLY:
			case TwoPhaseOutcome.PREPARE_NOTOK:
				TransactionImple.removeTransaction(this);
				break;
			}

			return res;
		}
		catch (ClassCastException ex)
		{
			jtaLogger.i18NLogger.error_transaction_class_cast_fail(super._theTransaction, ex);

			return TwoPhaseOutcome.INVALID_TRANSACTION;
		}
	}

	public boolean doCommit () throws IllegalStateException,
			HeuristicMixedException, HeuristicRollbackException,
			jakarta.transaction.SystemException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			int res = subAct.doCommit();

			switch (res)
			{
			case ActionStatus.COMMITTED:
			case ActionStatus.H_COMMIT:
				TransactionImple.removeTransaction(this);
				break;
            case ActionStatus.COMMITTING:
                TransactionImple.removeTransaction(this);
                return false;
			case ActionStatus.ABORTED:
			case ActionStatus.ABORTING:
				throw new HeuristicRollbackException();
			case ActionStatus.H_ROLLBACK:
				throw new HeuristicRollbackException();
			case ActionStatus.H_HAZARD:
			case ActionStatus.H_MIXED:
				throw new HeuristicMixedException();
			case ActionStatus.INVALID:
				TransactionImple.removeTransaction(this);

				throw new IllegalStateException();
			default:
				throw new HeuristicMixedException(); // not sure what
			// happened,
			// so err on the safe side!
			}
		}
		catch (ClassCastException ex)
		{
            jtaLogger.i18NLogger.error_transaction_class_cast_fail(super._theTransaction, ex);

            UnexpectedConditionException unexpectedConditionException = new UnexpectedConditionException(ex.toString());
            unexpectedConditionException.initCause(ex);
			throw unexpectedConditionException;
		}
		return true;
	}

	public void doRollback () throws IllegalStateException,
			HeuristicMixedException, HeuristicCommitException, HeuristicRollbackException, SystemException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			if (!endSuspendedRMs())
			{
                jtaLogger.i18NLogger.warn_transaction_arjunacore_endsuspendfailed1();
			}

			// JBTM-927 the transaction reaper may have aborted this transaction already
			int res = subAct.status() == ActionStatus.ABORTED ? ActionStatus.ABORTED : subAct.doRollback();

			switch (res)
			{
			case ActionStatus.ABORTED:
			case ActionStatus.ABORTING:
	                         TransactionImple.removeTransaction(this);

	                                break;
			case ActionStatus.H_ROLLBACK:
				throw new HeuristicRollbackException();
			case ActionStatus.H_COMMIT:
				throw new HeuristicCommitException();
			case ActionStatus.H_HAZARD:
			case ActionStatus.H_MIXED:
				throw new HeuristicMixedException();
			default:
				throw new HeuristicMixedException();
			}
		}
		catch (ClassCastException ex)
		{
			jtaLogger.i18NLogger.error_transaction_class_cast_fail(super._theTransaction, ex);

            UnexpectedConditionException unexpectedConditionException = new UnexpectedConditionException(ex.toString());
            unexpectedConditionException.initCause(ex);
			throw unexpectedConditionException;
		}
	}

	/**
	 * 
	 * @throws IllegalStateException
	 * 
	 * @deprecated Only called from a test
	 */
	public void doForget () throws IllegalStateException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			subAct.doForget();
		}
		catch (ClassCastException ex)
		{
			throw new IllegalStateException(ex);
		}
		finally
		{
			TransactionImple.removeTransaction(this);
		}
	}

	public void doOnePhaseCommit () throws IllegalStateException,
			jakarta.transaction.HeuristicMixedException, jakarta.transaction.SystemException, RollbackException
	{
		try
		{
			SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;

			if (!endSuspendedRMs())
				_theTransaction.preventCommit();

			int status = subAct.doOnePhaseCommit();

			switch (status)
			{
			case ActionStatus.COMMITTED:
			case ActionStatus.COMMITTING:
				TransactionImple.removeTransaction(this);
				break;
			case ActionStatus.H_COMMIT:
				TransactionImple.removeTransaction(this);
				throw new jakarta.transaction.HeuristicMixedException(jtaLogger.i18NLogger.get_onephase_heuristic_commit_failure(super._theTransaction));
			case ActionStatus.ABORTED:
	                 case ActionStatus.ABORTING:
	                 case ActionStatus.H_ROLLBACK:
                TransactionImple.removeTransaction(this);
                // JBTM-428. Note also this may be because the tx was set rollback only,
                // in which case IllegalState may be a better option?
                throw new RollbackException();
	                case ActionStatus.INVALID:
	                                throw new InvalidTerminationStateException();
			case ActionStatus.H_HAZARD:
			case ActionStatus.H_MIXED:
			default:
				throw new jakarta.transaction.HeuristicMixedException();
			}
		}
		catch (ClassCastException ex)
		{
            jtaLogger.i18NLogger.error_transaction_class_cast_fail(super._theTransaction, ex);

            UnexpectedConditionException unexpectedConditionException = new UnexpectedConditionException(ex.toString());
            unexpectedConditionException.initCause(ex);
			throw unexpectedConditionException;
		}
	}
	
	public boolean doBeforeCompletion () throws jakarta.transaction.SystemException
	{
	    try
	    {
	        SubordinateAtomicAction subAct = (SubordinateAtomicAction) super._theTransaction;
	        
	        return subAct.doBeforeCompletion();
	    }
	    catch (final Exception ex)
	    {
			jtaLogger.i18NLogger.error_transaction_class_cast_fail(super._theTransaction, ex);

	        UnexpectedConditionException unexpectedConditionException = new UnexpectedConditionException(ex.toString());
	        unexpectedConditionException.initCause(ex);
	        
	        throw unexpectedConditionException;
	    }
	}

	public String toString ()
	{
		if (super._theTransaction == null)
			return "TransactionImple < ac-subordinate, NoTransaction >";
		else
		{
			return "TransactionImple < ac-subordinate, "
					+ super._theTransaction + " >";
		}
	}

	protected void commitAndDisassociate ()
			throws jakarta.transaction.RollbackException,
			jakarta.transaction.HeuristicMixedException,
			jakarta.transaction.HeuristicRollbackException,
			java.lang.SecurityException, jakarta.transaction.SystemException,
			java.lang.IllegalStateException
	{
		throw new InvalidTerminationStateException();
	}

	protected void rollbackAndDisassociate ()
			throws java.lang.IllegalStateException,
			java.lang.SecurityException, jakarta.transaction.SystemException
	{
		throw new InvalidTerminationStateException();
	}

	/**
	 * Because of recovery, it is possible that a transaction may not be able to
	 * activate itself from the log initially, forcing us to retry later.
	 *
	 * @return <code>true</code> if the transaction was activated, <code>false</code>
	 * otherwise.
	 */

    public boolean activated ()
    {
    	return true;
    }

	@Override
	protected Xid createXid(boolean branch, XAModifier theModifier, XAResource xaResource) throws IOException, ObjectStoreException
	{
		Xid xid = baseXid();

		// We can have subordinate XIDs that can be editted
		if (xid.getFormatId() != XATxConverter.FORMAT_ID)
			return xid;

		Integer eisName = null;
        if(branch) {
            if(_xaResourceRecordWrappingPlugin != null) {
                eisName = _xaResourceRecordWrappingPlugin.getEISName(xaResource);
            }
        }
		xid = new XidImple(xid, branch, eisName);

		if (theModifier != null)
		{
			try
			{
				xid = theModifier.createXid((XidImple) xid);
			}
			catch (Exception e)
			{
				jtaLogger.i18NLogger.warn_cant_create_xid_of_xid(xid, branch, eisName, e);
			}
		}

		return xid;
	}

}