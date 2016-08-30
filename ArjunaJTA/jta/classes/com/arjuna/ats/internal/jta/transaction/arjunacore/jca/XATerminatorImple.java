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
 * $Id: XATerminatorImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import javax.transaction.HeuristicCommitException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.spi.XATerminatorExtensions;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

/**
 * The XATerminator implementation.
 * 
 * @author mcl
 */

public class XATerminatorImple implements javax.resource.spi.XATerminator, XATerminatorExtensions
{
    /**
     * Commit the transaction identified and hence any inflow-associated work.
     * 
     * @param xid
     *            the transaction to commit
     * @param onePhase
     *            whether or not this is a one-phase commit (should only be
     *            <code>true</code> if there is a single resource associated
     *            with the transaction).
     * @exception XAException
     *                thrown if there are any errors, including if the
     *                transaction cannot commit and has to roll back.
     */

    public void commit (Xid xid, boolean onePhase) throws XAException
    {
        try
        {
            SubordinateTransaction tx = SubordinationManager
                    .getTransactionImporter().getImportedTransaction(xid);

            if (tx == null)
                throw new XAException(XAException.XAER_INVAL);

            if (tx.activated())
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
        catch (final HeuristicCommitException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURCOM);
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

    /**
     * If the transaction subordinate generated a heuristic, then this operation
     * will be called later once that heuristic has been resolved.
     * 
     * @param xid
     *            the transaction.
     * @throws XAException
     *             if any error happens.
     */

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

    /**
     * Prepare the imported transaction.
     * 
     * @param xid
     *            the transaction to prepare.
     * @throws XAException
     *             thrown if any error occurs, including if the transaction has
     *             rolled back.
     * @return either XAResource.XA_OK if the transaction prepared, or
     *         XAResource.XA_RDONLY if it was a read-only transaction (and in
     *         which case, a second phase message is not expected/required.)
     */

    public int prepare (Xid xid) throws XAException
    {

    	// JBTM-927 this can happen if the transaction has been rolled back by the TransactionReaper
		SubordinateTransaction tx = null;
		try {
			tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
		} catch (XAException xae) {
			if (xae.errorCode == XAException.XA_RBROLLBACK) {
				SubordinationManager.getTransactionImporter().removeImportedTransaction(xid);
			}
			throw xae;
		}
        try
        {

            if (tx == null)
                throw new XAException(XAException.XAER_INVAL);

            switch (tx.doPrepare())
            {
            case TwoPhaseOutcome.PREPARE_READONLY:
                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);
                return XAResource.XA_RDONLY;
            case TwoPhaseOutcome.PREPARE_NOTOK:
                // the JCA API spec limits what we can do in terms of reporting
                // problems.
                // try to use the exception code and cause to provide info
                // whilst
                // remaining API compliant. JBTM-427.
                Exception initCause = null;
                int xaExceptionCode = XAException.XA_RBROLLBACK;
                try
                {
                    tx.doRollback();
                }
                catch (HeuristicCommitException e)
                {
                    initCause = e;
                    xaExceptionCode = XAException.XAER_RMERR;
                }
                catch (HeuristicMixedException e)
                {
                    initCause = e;
                    xaExceptionCode = XAException.XAER_RMERR;
                }
                catch (SystemException e)
                {
                    initCause = e;
                    xaExceptionCode = XAException.XAER_RMERR;
                }
                catch (final HeuristicRollbackException e)
                {
                    initCause = e;
                    xaExceptionCode = XAException.XAER_RMERR;
                }

                SubordinationManager.getTransactionImporter()
                        .removeImportedTransaction(xid);
                XAException xaException = new XAException(xaExceptionCode);
                if (initCause != null)
                {
                    xaException.initCause(initCause);
                }
                throw xaException;
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

    /**
     * Return a list of indoubt transactions. This may include those
     * transactions that are currently in-flight and running 2PC and do not need
     * recovery invoked on them.
     * 
     * @param flag
     *            either XAResource.TMSTARTRSCAN to indicate the start of a
     *            recovery scan, or XAResource.TMENDRSCAN to indicate the end of
     *            the recovery scan.
     * @throws XAException
     *             thrown if any error occurs.
     * @return a list of potentially indoubt transactions or <code>null</code>.
     */

    public Xid[] recover (int flag) throws XAException
    {
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
        return doRecover(null, null);
    }
    
    /**
     * Return a list of indoubt transactions. This may include those
     * transactions that are currently in-flight and running 2PC and do not need
     * recovery invoked on them.
     * 
     * @param nodeName
     * 				Only recover transactions for this node (unless set to NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES)
     * @throws XAException
     *             thrown if any error occurs.
     * @return a list of potentially indoubt transactions or <code>null</code>.
     */

    public Xid[] doRecover (Xid xid, String parentNodeName) throws XAException
    {
        /*
         * Requires going through the objectstore for the states of imported
         * transactions. Our own crash recovery takes care of transactions
         * imported via CORBA, Web Services etc.
         */

        Xid[] indoubt = null;

        try
        {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            InputObjectState states = new InputObjectState();

            // only look in the JCA section of the object store

            if (recoveryStore.allObjUids(SubordinateAtomicAction.getType(), states)
                    && (states.notempty()))
            {
                Stack<Xid> values = new Stack<Xid>();
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
                        ex.printStackTrace();

                        finished = true;
                    }

                    if (uid.notEquals(Uid.nullUid()))
                    {
						if (parentNodeName != null) {
							SubordinateAtomicAction saa = new SubordinateAtomicAction(uid, true);
							XidImple loadedXid = (XidImple) saa.getXid();
							if (loadedXid != null && loadedXid.getFormatId() == XATxConverter.FORMAT_ID) {
								String loadedXidSubordinateNodeName = XATxConverter.getSubordinateNodeName(loadedXid.getXID());
                                if ((loadedXidSubordinateNodeName == null && loadedXidSubordinateNodeName == TxControl.getXANodeName())
                                        || loadedXidSubordinateNodeName.equals(TxControl.getXANodeName())) {

									if (parentNodeName.equals(saa.getParentNodeName())) {
										if (jtaLogger.logger.isDebugEnabled()) {
											jtaLogger.logger.debug("Found record for " + saa);
										}
//										TransactionImple tx = (TransactionImple) SubordinationManager.getTransactionImporter().recoverTransaction(uid);

										values.push(loadedXid);
									}
								}
							}

						} else if (xid == null) {
							TransactionImple tx = (TransactionImple) SubordinationManager.getTransactionImporter().recoverTransaction(uid);

							if (tx != null)
								values.push(tx.baseXid());
						} else {
							SubordinateAtomicAction saa = new SubordinateAtomicAction(uid, true);
							XidImple loadedXid = (XidImple) saa.getXid();
							if (loadedXid != null && loadedXid.getFormatId() == XATxConverter.FORMAT_ID) {
								String loadedXidSubordinateNodeName = XATxConverter.getSubordinateNodeName(loadedXid.getXID());
								if (XATxConverter.getSubordinateNodeName(new XidImple(xid).getXID()).equals(loadedXidSubordinateNodeName)) {
									if (Arrays.equals(loadedXid.getGlobalTransactionId(), xid.getGlobalTransactionId())) {
										if (jtaLogger.logger.isDebugEnabled()) {
											jtaLogger.logger.debug("Found record for " + saa);
										}
										TransactionImple tx = (TransactionImple) SubordinationManager.getTransactionImporter().recoverTransaction(uid);

										values.push(loadedXid);
									}
								}
							}
						}

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
                        indoubt[index] = values.pop();
                        index++;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return indoubt;
    }

    /**
     * Rollback the imported transaction subordinate.
     * 
     * @param xid
     *            the transaction to roll back.
     * @throws XAException
     *             thrown if there are any errors.
     */

    public void rollback (Xid xid) throws XAException
    {
		// JBTM-927 this can happen if the transaction has been rolled back by
		// the TransactionReaper
		SubordinateTransaction tx = null;
		try {
			tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
		} catch (XAException xae) {
			if (xae.errorCode == XAException.XA_RBROLLBACK) {
				// do nothing as already rolled back
				return;
			}
			throw xae;
		}
        try
        {

            if (tx == null)
                throw new XAException(XAException.XAER_INVAL);

            if (tx.activated())
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
        catch (final HeuristicRollbackException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURRB);
            xaException.initCause(ex);
            throw xaException;
        }
        catch (HeuristicCommitException ex)
        {
            XAException xaException = new XAException(XAException.XA_HEURCOM);
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

            throw new XAException(XAException.XAER_RMERR);
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

}
