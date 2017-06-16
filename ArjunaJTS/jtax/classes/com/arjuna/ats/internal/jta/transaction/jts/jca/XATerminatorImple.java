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
 * $Id: XATerminatorImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts.jca;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.transaction.HeuristicCommitException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.spi.XATerminatorExtensions;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.SubordinateAtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.xa.XidImple;
import org.jboss.tm.ExtendedJBossXATerminator;
import org.jboss.tm.TransactionImportResult;
import com.arjuna.ats.jts.extensions.Arjuna;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * The XATerminator implementation.
 * 
 * @author mcl
 */

public class XATerminatorImple implements javax.resource.spi.XATerminator, XATerminatorExtensions, ExtendedJBossXATerminator
{

    public void commit (Xid xid, boolean onePhase) throws XAException
    {
        try
        {
            SubordinateTransaction tx = SubordinationManager
                    .getTransactionImporter().getImportedTransaction(xid);

            if (tx == null)
                throw new XAException(XAException.XAER_INVAL);

            if (tx.baseXid() != null) // activate failed?
            {
                if (onePhase)
                    tx.doOnePhaseCommit();
                else
                    if (!tx.doCommit()) {
                        throw new XAException(XAException.XAER_RMFAIL);
                    }

                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);
            }
            else
                throw new XAException(XAException.XA_RETRY);
        }
        catch (RollbackException e)
        {
            SubordinationManager.getTransactionImporter()
                    .removeImportedTransaction(xid);
            XAException xaException = new XAException(XAException.XA_RBROLLBACK);
            xaException.initCause(e);
            throw xaException;
        }
        catch (XAException ex)
        {
            // resource hasn't had a chance to recover yet

            if (ex.errorCode != XAException.XA_RETRY)
            {
                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);
            }

            throw ex;
        }
        catch (final HeuristicCommitException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURCOM);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (HeuristicRollbackException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURRB);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (HeuristicMixedException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURMIX);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (final IllegalStateException ex)
        {
            SubordinationManager.getTransactionImporter()
                    .removeImportedTransaction(xid);

            XAException xaException = new XAException(XAException.XAER_NOTA);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (SystemException ex)
        {
            SubordinationManager.getTransactionImporter()
                    .removeImportedTransaction(xid);

            XAException xaException = new XAException(XAException.XAER_RMERR);
            xaException.initCause(ex);
            throw xaException;
        }
    }

    public void forget (Xid xid) throws XAException
    {
        try
        {
            SubordinateTransaction tx = SubordinationManager
                    .getTransactionImporter().getImportedTransaction(xid);

            if (tx == null)
                throw new XAException(XAException.XAER_INVAL);

            tx.doForget();
        }
        catch (Exception ex)
        {
            XAException xaException = new XAException(XAException.XAER_RMERR);
            xaException.initCause(ex);
            throw xaException;
        }
        finally
        {
            SubordinationManager.getTransactionImporter()
                    .removeImportedTransaction(xid);
        }
    }

    public int prepare (Xid xid) throws XAException
    {
        try
        {
            SubordinateTransaction tx = SubordinationManager
                    .getTransactionImporter().getImportedTransaction(xid);

            if (tx == null)
                throw new XAException(XAException.XAER_INVAL);

            switch (tx.doPrepare())
            {
            case TwoPhaseOutcome.PREPARE_READONLY:
                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);

                return XAResource.XA_RDONLY;
            case TwoPhaseOutcome.PREPARE_NOTOK:
                try
                {
                    rollback(xid);
                }
                catch (final Throwable ex)
                {
                    // if we failed to prepare then rollback should not fail!
                }

                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);

                throw new XAException(XAException.XA_RBROLLBACK);
            case TwoPhaseOutcome.PREPARE_OK:
                return XAResource.XA_OK;
            case TwoPhaseOutcome.INVALID_TRANSACTION:
                throw new XAException(XAException.XAER_NOTA);
            default:
                throw new XAException(XAException.XA_RBOTHER);
            }
        }
        catch (XAException ex)
        {
            throw ex;
        }
    }

    public Xid[] recover (int flag) throws XAException
    {
        /*
         * Requires going through the objectstore for the states of imported
         * transactions. Our own crash recovery takes care of transactions
         * imported via CORBA, Web Services etc.
         */

        /*
         * Requires going through the objectstore for the states of imported
         * transactions. Our own crash recovery takes care of transactions
         * imported via CORBA, Web Services etc.
         */

        switch (flag)
        {
        case XAResource.TMSTARTRSCAN: // check the object store
            if (_recoveryStarted)
                throw new XAException(XAException.XAER_PROTO);
            else {
                _recoveryStarted = true;
                if (XARecoveryModule.getRegisteredXARecoveryModule() != null) {
                    XARecoveryModule.getRegisteredXARecoveryModule().periodicWorkFirstPass();
                }
            }
            break;
        case XAResource.TMENDRSCAN: // null op for us
            if (_recoveryStarted) {
                _recoveryStarted = false;
                if (XARecoveryModule.getRegisteredXARecoveryModule() != null) {
                    XARecoveryModule.getRegisteredXARecoveryModule().periodicWorkSecondPass();
                }
            }
            else
                throw new XAException(XAException.XAER_PROTO);
            return null;
        case XAResource.TMNOFLAGS:
            if (_recoveryStarted)
                break;
        default:
            throw new XAException(XAException.XAER_PROTO);
        }

        // if we are here, then check the object store
        try {
            return doRecover(null, null);
        } catch (NotSupportedException nse) {
            throw new IllegalStateException(nse);
        }
    }

    public void rollback (Xid xid) throws XAException
    {
        // JBTM-927 this can happen if the transaction has been rolled back by
        // the TransactionReaper
        SubordinateTransaction tx = null;
        try {
            tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        } catch (XAException xae) {
            if (xae.errorCode == XAException.XA_RBROLLBACK) {
                SubordinationManager.getTransactionImporter().removeImportedTransaction(xid);
                return;
            }
            throw xae;
        }

        try
        {
            if (tx == null)
                throw new XAException(XAException.XAER_INVAL);

            if (tx.baseXid() != null)
            {
                tx.doRollback();

                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);
            }
            else
                throw new XAException(XAException.XA_RETRY);
        }
        catch (XAException ex)
        {
            // resource hasn't had a chance to recover yet

            if (ex.errorCode != XAException.XA_RETRY)
            {
                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);
            }

            throw ex;
        }
        catch (HeuristicCommitException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURCOM);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (final HeuristicRollbackException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURRB);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (HeuristicMixedException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURMIX);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (final IllegalStateException ex)
        {
            SubordinationManager.getTransactionImporter()
                    .removeImportedTransaction(xid);

            XAException xaException = new XAException(XAException.XAER_NOTA);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (SystemException ex)
        {
            SubordinationManager.getTransactionImporter()
                    .removeImportedTransaction(xid);

            XAException xaException = new XAException(XAException.XAER_RMERR);
            xaException.initCause(ex);
            throw xaException;
        }
    }

    public boolean beforeCompletion (Xid xid) throws javax.transaction.SystemException
    {
        try
        {
            SubordinateTransaction tx = SubordinationManager
                    .getTransactionImporter().getImportedTransaction(xid);

            if (tx == null)
                throw new UnexpectedConditionException();

           return tx.doBeforeCompletion();
        }
        catch (final Exception ex)
        {
            UnexpectedConditionException e = new UnexpectedConditionException();
            
            e.initCause(ex);
            
            throw e;
        }
    }

    private boolean _recoveryStarted = false;

    // Extended methods for remoting

    private static final Xid[] NO_XIDS = new Xid[0];

    public Transaction getTransaction(Xid xid) throws XAException {
        // first see if the xid is a root coordinator
        Transaction transaction = TransactionImple.getTransaction(new XidImple(xid).getTransactionUid());
        // second see if the xid is a subordinate txn
        if(transaction == null) {
            transaction = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        }
        return transaction;
    }

    public TransactionImportResult importTransaction(Xid xid, int timeoutIfNew) throws XAException {
        return SubordinationManager.getTransactionImporter().importRemoteTransaction(xid, timeoutIfNew);
    }

    public SubordinateTransaction getImportedTransaction(Xid xid) throws XAException {
        final TransactionImporter transactionImporter = SubordinationManager.getTransactionImporter();
        return transactionImporter.getImportedTransaction(xid);
    }

    public Transaction getTransactionById(Object id) {
        return TransactionImple.getTransaction((Uid) id);
    }

    public Object getCurrentTransactionId() {
        com.arjuna.ats.internal.jta.transaction.jts.TransactionImple transaction = TransactionImple.getTransaction();
        if (transaction == null)
            return null;

        return transaction.get_uid();
    }

    public void removeImportedTransaction(Xid xid) throws XAException {
        SubordinationManager.getTransactionImporter().removeImportedTransaction(xid);
    }

    public Xid[] getXidsToRecoverForParentNode(boolean recoverInFlight, String parentNodeName, int recoveryFlags) throws XAException {
        final Set<Xid> xidsToRecover = new HashSet<Xid>();
        if (recoverInFlight) {
            final TransactionImporter transactionImporter = SubordinationManager.getTransactionImporter();
            if (transactionImporter instanceof TransactionImporterImple) {
                throw new UnsupportedOperationException(jtaxLogger.i18NLogger.get_not_supported());
            }
        }
        final javax.resource.spi.XATerminator xaTerminator = SubordinationManager.getXATerminator();
        if (xaTerminator instanceof XATerminatorImple) {
            throw new UnsupportedOperationException(jtaxLogger.i18NLogger.get_not_supported());
        } else {
            final Xid[] inDoubtTransactions = xaTerminator.recover(recoveryFlags);
            if (inDoubtTransactions != null) {
                xidsToRecover.addAll(Arrays.asList(inDoubtTransactions));
            }
        }
        return xidsToRecover.toArray(NO_XIDS);
    }

    /**
     * <p>
     * Recovering /JCA section of object store.
     * The filtering functionality on xid or parentNodeName is not permitted and throws {@link NotSupportedException}.<br>
     * Expected to be called only with null parameters <code>doRecover(null, null)</code>
     *
     * @param xid has to be null
     * @param parentNodeName  has to be null
     * @return array of subordinate recovered xids
     * @throws XAException  if recovery operation fails for the XA protocol reason
     * @throws NotSupportedException  if not null params are passes as method parameters
     */
    @Override
    public Xid[] doRecover(Xid xid, String parentNodeName) throws XAException, NotSupportedException {
        if(xid != null || parentNodeName != null)
            throw new NotSupportedException("doRecover method works only with null arguments");

        Xid[] indoubt = null;
        try
        {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            InputObjectState states = new InputObjectState();

            // only look in the JCA section of the object store
            if (recoveryStore.allObjUids(ServerTransaction.getType(), states) && (states.notempty()))
            {
                Stack<Transaction> values = new Stack<Transaction>();
                boolean finished = false;

                do
                {
                    Uid uid = null;

                    try
                    {
                        uid = UidHelper.unpackFrom(states);
                    }
                    catch (IOException ex)
                    {
                        jtsLogger.i18NLogger.info_fail_to_read_subordinate_uid(recoveryStore, states, ex);

                        finished = true;
                    }

                    if (uid.notEquals(Uid.nullUid()))
                    {
                        Transaction tx = SubordinationManager.getTransactionImporter().recoverTransaction(uid);

                        if (tx != null)
                            values.push(tx);
                    }
                    else
                        finished = true;

                }
                while (!finished);

                if (values.size() > 0)
                {
                    int index = 0;

                    indoubt = new Xid[values.size()];

                    while (!values.empty())
                    {
                        TransactionImple id = (TransactionImple) values.pop();

                        indoubt[index] = id.baseXid();

                        index++;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            jtsLogger.i18NLogger.info_fail_to_dorecover(xid, parentNodeName, ex);
        }

        return indoubt;

    }

    @Override
    public boolean isRecoveryByNodeOrXidSupported() {
        return false;
    }

}
