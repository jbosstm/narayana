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
 * Copyright (C) 2000, 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveredTransactionalObject.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj.recovery;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.txoj.logging.txojLogger;

import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.internal.arjuna.Header;

/**
 * TransactionalObject shell instantiated at recovery time.
 * <p>
 * Instantiated only for TransactionalObjects that are found (by
 * {@link TORecoveryModule}) to be in an uncommitted (indeterminate) state. The
 * status of the transaction that created the uncommitted state is determined -
 * if the transaction rolled back, the original state of the TransactionalObject
 * is reinstated. If the transaction rolled back (or is still in progress), no
 * change is made - the completion (including completion in recovery) of the
 * transaction will be applied to the transactional object (eventually).
 * <p>
 * Instantiation from the ObjectStore ignores all of the TO except for the
 * information in the header ( see
 * {@link com.arjuna.ats.arjuna.StateManager#packHeader StateManager.packHeader}
 * ), which is overridden by this class).
 * <P>
 * 
 * @author Peter Furniss (peter.furniss@arjuna.com), Mark Little
 *         (mark_little@hp.com)
 * @version $Id: RecoveredTransactionalObject.java 2342 2006-03-30 13:06:17Z $
 */

/*
 * Does not extend LockManager or StateManager because they are concerned with
 * activating the committed state, and this is only concerned with the
 * uncommitted.
 */

public class RecoveredTransactionalObject extends StateManager
{
    protected RecoveredTransactionalObject(Uid objectUid, String originalType, ObjectStore objectStore)
    {
        _ourUid = objectUid;
        _type = originalType;
        _objectStore = objectStore;
        _transactionStatusConnectionMgr = new TransactionStatusConnectionManager();

        if (txojLogger.logger.isDebugEnabled()) {
            txojLogger.logger.debug("RecoveredTransactionalObject created for "+_ourUid);
        }
    }

    protected final void replayPhase2 ()
    {
        if (findHoldingTransaction())
        {
            /*
             * There is a transaction holding this in uncommitted state find out
             * what the Status is. We have no idea what type of transaction it
             * is, so leave that to the cache.
             */

            if (txojLogger.logger.isDebugEnabled()) {
                txojLogger.logger.debug("TO held by transaction "+_owningTransactionUid);
            }

            int tranStatus = _transactionStatusConnectionMgr
                    .getTransactionStatus(_owningTransactionUid);

            if (txojLogger.logger.isDebugEnabled()) {
                txojLogger.logger.debug("RecoveredTransactionalObject - transaction status "+ActionStatus.stringForm(tranStatus));
            }

            boolean inactive = false;

            if (tranStatus == ActionStatus.INVALID) // should be
                                                    // ActionStatus.NO_ACTION
            {
                if (txojLogger.logger.isDebugEnabled())
                {
                    txojLogger.logger.debug("transaction Status from original application "+Integer.toString(tranStatus)+" and inactive: "+inactive);
                }

                inactive = true;
            }

            /*
             * Only do anything if we are sure the transaction rolledback if it
             * is still in progress in the original application, let that run
             * otherwise the transaction should recover and do the committment
             * eventually.
             */

            if ((tranStatus == ActionStatus.ABORTED) || inactive)
            {
                rollback();
            }
            else
                commit();
        }
        else
        {
            if (txojLogger.logger.isDebugEnabled()) {
                txojLogger.logger.debug("RecoveredTransactionalObject.replayPhase2 - cannot find/no holding transaction");
            }
        }
    }

    /**
     * Determine which transaction got this into uncommitted state return true
     * if there is such a transaction
     */

    private final boolean findHoldingTransaction ()
    {
        InputObjectState uncommittedState = null;

        _originalProcessUid = new Uid(Uid.nullUid());

        try
        {
            uncommittedState = _objectStore.read_uncommitted(_ourUid, _type);
        }
        catch (ObjectStoreException e)
        {
            txojLogger.i18NLogger.warn_recovery_RecoveredTransactionalObject_6(e);

            return false; // probably
        }

        /*
         * Get the transaction and original process information from the saved
         * state.
         */

        _originalProcessUid = null;
        _owningTransactionUid = null;

        try
        {
            Header hdr = new Header();
            
            unpackHeader(uncommittedState, hdr);

            _originalProcessUid = hdr.getProcessId();
            _owningTransactionUid = hdr.getTxId();
            
            if (txojLogger.logger.isDebugEnabled()) {
                txojLogger.logger.debug("RecoveredTransactionalObject::findHoldingTransaction - uid is "+_owningTransactionUid);
            }

            return _owningTransactionUid.notEquals(Uid.nullUid());
        }
        catch (Exception e)
        {
            txojLogger.i18NLogger.warn_recovery_RecoveredTransactionalObject_8(e);
        }

        return false;
    }

    private final void rollback ()
    {
        try
        {
            _objectStore.remove_uncommitted(_ourUid, _type);
        }
        catch (ObjectStoreException e)
        {
            txojLogger.i18NLogger.warn_recovery_RecoveredTransactionalObject_9(_ourUid, e);
        }
    }

    private final void commit ()
    {
        try
        {
            _objectStore.commit_state(_ourUid, _type);
        }
        catch (ObjectStoreException e)
        {
            txojLogger.i18NLogger.warn_recovery_RecoveredTransactionalObject_10(_ourUid, e);
        }
    }

    private Uid _ourUid;

    private Uid _owningTransactionUid;

    private Uid _originalProcessUid;

    private ObjectStore _objectStore;

    private String _type;

    private TransactionStatusConnectionManager _transactionStatusConnectionMgr;

}
