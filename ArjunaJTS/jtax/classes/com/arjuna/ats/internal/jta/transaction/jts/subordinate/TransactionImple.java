/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.transaction.jts.subordinate;

import jakarta.transaction.HeuristicCommitException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction;
import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.jta.exceptions.InactiveTransactionException;
import com.arjuna.ats.jta.exceptions.InvalidTerminationStateException;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;

public class TransactionImple extends
                com.arjuna.ats.internal.jta.transaction.jts.TransactionImple
{

        /**
         * Create a new transaction with the specified timeout.
         */

        public TransactionImple (AtomicTransaction imported)
        {
                super(imported);

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
                /*
                 * throw new IllegalStateException(
                 * jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.subordinate.invalidstate"));
                 */

                throw new InvalidTerminationStateException();
        }

        /**
         * This is a subordinate transaction, so any attempt to commit it or roll it
         * back directly, should fail.
         */

        public void rollback () throws java.lang.IllegalStateException,
                        java.lang.SecurityException, jakarta.transaction.SystemException
        {
                /*
                 * throw new IllegalStateException(
                 * jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.subordinate.invalidstate"));
                 */

                throw new InvalidTerminationStateException();
        }

        /**
         * Drive the subordinate transaction through the prepare phase. Any
         * enlisted participants will also be prepared as a result.
         *
         * @return a TwoPhaseOutcome representing the result.
         *
         * @throws SystemException thrown if any error occurs.
         */

        public int doPrepare ()
        {
                try
                {
                        SubordinateAtomicTransaction subAct = (SubordinateAtomicTransaction) super._theTransaction;

                        if (!endSuspendedRMs())
                        {
                                try
                                {
                                        _theTransaction.rollbackOnly();
                                }
                                catch (org.omg.CosTransactions.NoTransaction ex)
                                {
                                        // shouldn't happen! ignore because prepare will fail next anyway.
                                }
                        }

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
                        return TwoPhaseOutcome.INVALID_TRANSACTION;
                }
        }

        /**
         * Drive the subordinate transaction to commit. It must have previously
         * been prepared.
         *
         *@return true if the transaction was committed
         *
         * @throws IllegalStateException thrown if the transaction has not been prepared
         * or is unknown.
         * @throws HeuristicMixedException thrown if a heuristic mixed outcome occurs
         * (where some participants committed whilst others rolled back).
         * @throws HeuristicRollbackException thrown if the transaction rolled back.
         * @throws SystemException thrown if some other error occurs.
         */

        public boolean doCommit () throws IllegalStateException,
                        HeuristicMixedException, HeuristicRollbackException, HeuristicCommitException,
                        SystemException
        {
                try
                {
                        SubordinateAtomicTransaction subAct = (SubordinateAtomicTransaction) super._theTransaction;

                        int res = subAct.doCommit();

                        switch (res)
                        {
                        case ActionStatus.H_COMMIT:
                            throw new HeuristicCommitException();
                        case ActionStatus.COMMITTED:
                                break;
                        case ActionStatus.COMMITTING:
                            return false;
                        case ActionStatus.ABORTED:
                        case ActionStatus.ABORTING:
                        case ActionStatus.H_ROLLBACK:
                                throw new HeuristicRollbackException();
                        case ActionStatus.H_HAZARD:
                        case ActionStatus.H_MIXED:
                            throw new HeuristicMixedException();
                        case ActionStatus.INVALID:
                                throw new IllegalStateException();
                        default:
                                throw new HeuristicMixedException(); // not sure what happened,
                        // so err on the safe side!
                        }
                }
                catch (ClassCastException ex)
                {
                    ex.printStackTrace();

                    UnexpectedConditionException unexpectedConditionException = new UnexpectedConditionException(ex.toString());
                    unexpectedConditionException.initCause(ex);
                    throw unexpectedConditionException;
                }
                finally
                {
                        TransactionImple.removeTransaction(this);
                }
                return true;
        }

        /**
         * Drive the subordinate transaction to roll back. It need not have been previously
         * prepared.
         *
         * @throws IllegalStateException thrown if the transaction is not known by the
         * system or has been previously terminated.
         * @throws HeuristicMixedException thrown if a heuristic mixed outcome occurs
         * (can only happen if the transaction was previously prepared and then only if
         * some participants commit whilst others roll back).
         * @throws HeuristicCommitException thrown if the transaction commits (can only
         * happen if it was previously prepared).
         * @throws SystemException thrown if any other error occurs.
         */

        public void doRollback () throws IllegalStateException,
                        HeuristicMixedException, HeuristicCommitException, HeuristicRollbackException, SystemException
        {
                try
                {
                        SubordinateAtomicTransaction subAct = (SubordinateAtomicTransaction) super._theTransaction;

                        if (!endSuspendedRMs())
                        {
                            jtaxLogger.i18NLogger.warn_jtax_transaction_jts_endsuspendfailed1();
                        }

                        int res = subAct.doRollback();

                        switch (res)
                        {
                        case ActionStatus.ABORTED:
                        case ActionStatus.ABORTING:
                            break;
                        case ActionStatus.H_ROLLBACK:
                            throw new HeuristicRollbackException();
                        case ActionStatus.COMMITTED:
                        case ActionStatus.COMMITTING:
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
                    ex.printStackTrace();

                    UnexpectedConditionException unexpectedConditionException = new UnexpectedConditionException(ex.toString());
                    unexpectedConditionException.initCause(ex);
                    throw unexpectedConditionException;
                }
                finally
                {
                        TransactionImple.removeTransaction(this);
                }
        }

        /**
         * Drive the transaction to commit. It should not have been previously
         * prepared and will be the only resource in the global transaction.
         *
         * @throws IllegalStateException if the transaction has already terminated
         * @throws jakarta.transaction.HeuristicRollbackException thrown if the transaction
         * rolls back.
         */

        public void doOnePhaseCommit () throws IllegalStateException, jakarta.transaction.RollbackException,
                        jakarta.transaction.HeuristicMixedException, jakarta.transaction.SystemException
        {
                try
                {
                        SubordinateAtomicTransaction subAct = (SubordinateAtomicTransaction) super._theTransaction;

                        if (!endSuspendedRMs())
                        {
                                try
                                {
                                        _theTransaction.rollbackOnly();
                                }
                                catch (org.omg.CosTransactions.NoTransaction ex)
                                {
                                        // shouldn't happen! ignore because commit will fail next anyway.
                                }
                        }

                        int status = subAct.doOnePhaseCommit(); // ActionStatus status = TwoPhaseOutcome

                        TransactionImple.removeTransaction(this);

                        switch (status)
                        {
                        case ActionStatus.COMMITTED:
                        case ActionStatus.H_COMMIT:
                        case ActionStatus.COMMITTING:
                                break;
                        case ActionStatus.ABORTED:
                        case ActionStatus.ABORTING:
                            throw new RollbackException();
                        case ActionStatus.H_ROLLBACK:
                        case ActionStatus.H_HAZARD:
                        case ActionStatus.H_MIXED:
                        default:
                                throw new jakarta.transaction.HeuristicMixedException();
                        case ActionStatus.INVALID:
                                throw new InvalidTerminationStateException();
                        }
                }
                catch (ClassCastException ex)
                {
                        ex.printStackTrace();

                        throw new IllegalStateException(ex);
                }
        }

        /**
         * Called to tell the transaction to forget any heuristics.
         *
         * @throws IllegalStateException thrown if the transaction cannot
         * be found.
         */

        public void doForget () throws IllegalStateException
        {
                try
                {
                        SubordinateAtomicTransaction subAct = (SubordinateAtomicTransaction) super._theTransaction;

                        subAct.doForget();
                }
                catch (ClassCastException ex)
                {
                        ex.printStackTrace();

                        throw new IllegalStateException(ex);
                }
        }
        
        public boolean doBeforeCompletion () throws jakarta.transaction.SystemException
        {
            try
            {
                SubordinateAtomicTransaction subAct = (SubordinateAtomicTransaction) super._theTransaction;
                
                return subAct.doBeforeCompletion();
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();

                UnexpectedConditionException unexpectedConditionException = new UnexpectedConditionException(ex.toString());
                unexpectedConditionException.initCause(ex);
                
                throw unexpectedConditionException;
            }
        }

        public String toString ()
        {
                if (super._theTransaction == null)
                        return "TransactionImple < jts-subordinate, NoTransaction >";
                else
                {
                        return "TransactionImple < jts-subordinate, "
                                        + super._theTransaction.get_uid() + " >";
                }
        }

        protected void commitAndDisassociate ()
                        throws jakarta.transaction.RollbackException,
                        jakarta.transaction.HeuristicMixedException,
                        jakarta.transaction.HeuristicRollbackException,
                        java.lang.SecurityException, jakarta.transaction.SystemException,
                        java.lang.IllegalStateException
        {
                /*
                 * throw new IllegalStateException(
                 * jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.subordinate.invalidstate"));
                 */

                throw new InactiveTransactionException();
        }

        protected void rollbackAndDisassociate ()
                        throws java.lang.IllegalStateException,
                        java.lang.SecurityException, jakarta.transaction.SystemException
        {
                /*
                 * throw new IllegalStateException(
                 * jtaxLogger.loggerI18N.getString("com.arjuna.ats.internal.jta.transaction.jts.subordinate.invalidstate"));
                 */

                throw new InactiveTransactionException();
        }

}