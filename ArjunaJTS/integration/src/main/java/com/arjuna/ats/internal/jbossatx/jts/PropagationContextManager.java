/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jbossatx.jts;

import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextImporter;

import org.omg.CosTransactions.*;

import jakarta.transaction.Transaction;
import javax.naming.spi.ObjectFactory;
import javax.naming.Name;
import javax.naming.Context;

import java.util.Hashtable;
import java.io.Serializable;

import com.arjuna.ats.arjuna.utils.ThreadUtil;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jts.ExplicitInterposition;
import com.arjuna.ats.jbossatx.logging.jbossatxLogger;




public class PropagationContextManager implements
        TransactionPropagationContextFactory,
        TransactionPropagationContextImporter, ObjectFactory, Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Return a transaction propagation context for the transaction currently
     * associated with the invoking thread, or <code>null</code> if the invoking
     * thread is not associated with a transaction.
     */

    public Object getTransactionPropagationContext ()
    {
        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext - called");
        }

        final String threadId = ThreadUtil.getThreadId();
        ControlWrapper theControl;

        if (threadId != null)
        {
            theControl = OTSImpleManager.current().contextManager().current(
                    threadId);
        }
        else
        {
            theControl = OTSImpleManager.current().contextManager().current();
        }

        try
        {
            final PropagationContext cxt = theControl.get_coordinator()
                    .get_txcontext();
            PropagationContextWrapper pcw = new PropagationContextWrapper(cxt);

            if (jbossatxLogger.logger.isTraceEnabled()) {
                jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext() - returned tpc = "
                        + pcw);
            }

            return pcw;
        }
        catch (Exception e)
        {
        }

        return null;
    }

    /**
     * Return a transaction propagation context for the transaction given as an
     * argument, or <code>null</code> if the argument is <code>null</code> or of
     * a type unknown to this factory.
     */

    public Object getTransactionPropagationContext (Transaction tx)
    {
        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext(Transaction) - called tx = "
                    + tx);
        }

        Transaction oldTx = null;
        Object tpc = null;
        jakarta.transaction.TransactionManager tm = TransactionManager
                .transactionManager();

        try
        {
            oldTx = tm.getTransaction();

            if ((tx == null) || (tx.equals(oldTx)))
            {
                // we are being called in the context of this transaction
                tpc = getTransactionPropagationContext();
            }
            else
            {
                tm.suspend();
                tm.resume(tx);

                tpc = getTransactionPropagationContext();

                tm.suspend();
                tm.resume(oldTx);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.getTransactionPropagationContext(Transaction) - returned tpc = "
                    + tpc);
        }

        return tpc;
    }

    /**
     * Import the transaction propagation context into the transaction manager,
     * and return the resulting transaction. If this transaction propagation
     * context has already been imported into the transaction manager, this
     * method simply returns the <code>Transaction</code> representing the
     * transaction propagation context in the local VM. Returns
     * <code>null</code> if the transaction propagation context is
     * <code>null</code>, or if it represents a <code>null</code> transaction.
     */

    public Transaction importTransactionPropagationContext (Object tpc)
    {
        if (jbossatxLogger.logger.isTraceEnabled()) {
            jbossatxLogger.logger.trace("PropagationContextManager.importTransactionPropagationContext(Object) - called tpc = "
                    + tpc);
        }

        jakarta.transaction.TransactionManager tm = TransactionManager
                .transactionManager();

        if (tpc instanceof PropagationContextWrapper)
        {
            try
            {
                PropagationContext omgTpc = ((PropagationContextWrapper) tpc)
                        .getPropagationContext();
                ExplicitInterposition ei = new ExplicitInterposition(omgTpc,
                        true);
                Transaction newTx = tm.getTransaction();

                if (jbossatxLogger.logger.isTraceEnabled()) {
                    jbossatxLogger.logger.trace("PropagationContextManager.importTransactionPropagationContext(Object) - transaction = "
                            + newTx);
                }

                ei.unregisterTransaction();

                return newTx;
            }
            catch (Exception e)
            {
                jbossatxLogger.i18NLogger.error_jts_PropagationContextManager_exception(e);

                return null;
            }
        }
        else
        {
            jbossatxLogger.i18NLogger.error_jts_PropagationContextManager_unknownctx();

            return null;
        }
    }

    public Object getObjectInstance (Object obj, Name name, Context nameCtx,
            Hashtable environment) throws Exception
    {
        return new PropagationContextManager();
    }
}