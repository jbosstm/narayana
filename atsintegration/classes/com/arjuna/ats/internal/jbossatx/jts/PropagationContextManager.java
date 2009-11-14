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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PropagationContextManager.java,v 1.5 2004/10/04 09:48:19 nmcl Exp $
 */

package com.arjuna.ats.internal.jbossatx.jts;

import org.jboss.tm.TransactionPropagationContextFactory;
import org.jboss.tm.TransactionPropagationContextImporter;

import org.omg.CosTransactions.*;

import javax.transaction.Transaction;
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
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;
import com.arjuna.common.util.logging.FacilityCode;

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
        if (jbossatxLogger.logger.isDebugEnabled())
        {
            jbossatxLogger.logger
                    .debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                            FacilityCode.FAC_ALL,
                            "PropagationContextManager.getTransactionPropagationContext - called");
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

            if (jbossatxLogger.logger.isDebugEnabled())
            {
                jbossatxLogger.logger
                        .debug(
                                DebugLevel.FUNCTIONS,
                                VisibilityLevel.VIS_PUBLIC,
                                FacilityCode.FAC_ALL,
                                "PropagationContextManager.getTransactionPropagationContext() - returned tpc = "
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
        if (jbossatxLogger.logger.isDebugEnabled())
        {
            jbossatxLogger.logger
                    .debug(
                            DebugLevel.FUNCTIONS,
                            VisibilityLevel.VIS_PUBLIC,
                            FacilityCode.FAC_ALL,
                            "PropagationContextManager.getTransactionPropagationContext(Transaction) - called tx = "
                                    + tx);
        }

        Transaction oldTx = null;
        Object tpc = null;
        javax.transaction.TransactionManager tm = TransactionManager
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

        if (jbossatxLogger.logger.isDebugEnabled())
        {
            jbossatxLogger.logger
                    .debug(
                            DebugLevel.FUNCTIONS,
                            VisibilityLevel.VIS_PUBLIC,
                            FacilityCode.FAC_ALL,
                            "PropagationContextManager.getTransactionPropagationContext(Transaction) - returned tpc = "
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
     * 
     * @message 
     *          com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager.exception
     *          [com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager.
     *          exception] Unexpected exception occurred
     * @message com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager.
     *          unknownctx
     *          [com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager
     *          .unknownctx] jboss-atx: unknown Tx PropagationContext
     */

    public Transaction importTransactionPropagationContext (Object tpc)
    {
        if (jbossatxLogger.logger.isDebugEnabled())
        {
            jbossatxLogger.logger
                    .debug(
                            DebugLevel.FUNCTIONS,
                            VisibilityLevel.VIS_PUBLIC,
                            FacilityCode.FAC_ALL,
                            "PropagationContextManager.importTransactionPropagationContext(Object) - called tpc = "
                                    + tpc);
        }

        javax.transaction.TransactionManager tm = TransactionManager
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

                if (jbossatxLogger.logger.isDebugEnabled())
                {
                    jbossatxLogger.logger
                            .debug(
                                    DebugLevel.FUNCTIONS,
                                    VisibilityLevel.VIS_PUBLIC,
                                    FacilityCode.FAC_ALL,
                                    "PropagationContextManager.importTransactionPropagationContext(Object) - transaction = "
                                            + newTx);
                }

                ei.unregisterTransaction();

                return newTx;
            }
            catch (Exception e)
            {
                jbossatxLogger.loggerI18N
                        .error(
                                "com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager.exception",
                                e);

                return null;
            }
        }
        else
        {
            jbossatxLogger.loggerI18N
                    .error("com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager.unknownctx");

            return null;
        }
    }

    public Object getObjectInstance (Object obj, Name name, Context nameCtx,
            Hashtable environment) throws Exception
    {
        return new PropagationContextManager();
    }
}
