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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveredTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.jts.utils.Utility;

import com.arjuna.ats.internal.arjuna.Header;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.recovery.contact.StatusChecker;
import com.arjuna.ats.arjuna.exceptions.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.jts.logging.jtsLogger;

import org.omg.CosTransactions.*;
import java.io.IOException;
import java.util.Date;

import org.omg.CORBA.SystemException;

/**
 * Transaction type only instantiated at recovery time. This is used to
 * re-activate the state of a root (non-interposed) transaction that did not
 * terminate correctly due to failure.
 * <P>
 * 
 * @author Dave Ingham (dave@arjuna.com)
 * @version $Id: RecoveredTransaction.java 2342 2006-03-30 13:06:17Z $
 */

public class RecoveredTransaction extends ArjunaTransactionImple implements
        RecoveringTransaction
{
    public RecoveredTransaction(Uid actionUid)
    {
        this(actionUid, "");
    }

    public RecoveredTransaction(Uid actionUid, String changedTypeName)
    {
        super(actionUid);

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("RecoveredTransaction "+get_uid()+" created");
        }

        // Don't bother trying to activate a transaction that isn't in
        // the store. This saves an error message.
        _recoveryStatus = RecoveryStatus.ACTIVATE_FAILED;

        String effectiveTypeName = typeName();

        if (changedTypeName.length() < 1)
        {
            _typeName = null;
        }
        else
        {
            _typeName = changedTypeName;
            effectiveTypeName = changedTypeName;
        }

        _originalProcessUid = new Uid(Uid.nullUid());

        try
        {
            if ((store().currentState(actionUid, effectiveTypeName) != StateStatus.OS_UNKNOWN))
            {
                if (activate())
                    _recoveryStatus = RecoveryStatus.ACTIVATED;
                else {
                    jtsLogger.i18NLogger.warn_recovery_transactions_RecoveredTransaction_2(actionUid);
                }
            }
        }
        catch (Exception e)
        {
            jtsLogger.i18NLogger.warn_recovery_transactions_RecoveredTransaction_3(actionUid, e);
        }

        _txStatus = Status.StatusUnknown;
    }

    /**
     * Get the status of the transaction. If we successfully activated the
     * transaction then we return whatever the transaction reports otherwise we
     * return RolledBack as we're using presumed abort.
     */
    public synchronized Status get_status () throws SystemException
    {
        if (_txStatus != Status.StatusUnknown)
            return _txStatus;

        Status theStatus = Status.StatusUnknown;

        if (_recoveryStatus == RecoveryStatus.ACTIVATE_FAILED)
            theStatus = Status.StatusRolledBack;
        else
            theStatus = super.get_status();

        return theStatus;
    }

    /**
 *
 */
    public Status getOriginalStatus ()
    {
        if (_recoveryStatus != RecoveryStatus.ACTIVATE_FAILED)
        {
            try
            {
                return StatusChecker.get_status(get_uid(), _originalProcessUid);
            }
            catch (Inactive ex)
            {
                // shouldn't happen!

                return Status.StatusUnknown;
            }
        }
        else
        {
            // if it can't be activated, we can't get the process uid
            return Status.StatusUnknown;
        }

    }

    /**
     * Allows a new Resource to be added to the transaction. Typically this is
     * used to replace a Resource that has failed and cannot be recovered on
     * it's original IOR.
     */

    public void addResourceRecord (Uid rcUid, Resource r)
    {
        Coordinator coord = null;
        AbstractRecord corbaRec = createOTSRecord(true, r, coord, rcUid);

        addRecord(corbaRec);
    }

    /**
     * Causes phase 2 of the commit protocol to be replayed.
     */

    public void replayPhase2 ()
    {
        _recoveryStatus = RecoveryStatus.REPLAYING;

        Status theStatus = get_status();

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("RecoveredTransaction.replayPhase2 ("+get_uid()+") - status = "+Utility.stringStatus(theStatus));
        }

        if ((theStatus == Status.StatusPrepared)
                || (theStatus == Status.StatusCommitting)
                || (theStatus == Status.StatusCommitted))
        {
            phase2Commit(_reportHeuristics);

            _recoveryStatus = RecoveryStatus.REPLAYED;

            _txStatus = Status.StatusCommitted;
        }
        else if ((theStatus == Status.StatusRolledBack)
                || (theStatus == Status.StatusRollingBack)
                || (theStatus == Status.StatusMarkedRollback))
        {
            phase2Abort(_reportHeuristics);

            _recoveryStatus = RecoveryStatus.REPLAYED;

            _txStatus = Status.StatusRolledBack;
        }
        else {
            jtsLogger.i18NLogger.warn_recovery_transactions_RecoveredTransaction_6(Utility.stringStatus(theStatus));
            _recoveryStatus = RecoveryStatus.REPLAY_FAILED;
        }

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("RecoveredTransaction.replayPhase2 ("+get_uid()+") - status = "+Utility.stringStatus(theStatus));
        }
    }

    /**
     * Get the status of recovery for this transaction
     */
    public int getRecoveryStatus ()
    {
        return _recoveryStatus;
    }

    // hmmm, isn't this a memory leak, since we'll never purge the cache?!

    /*
     * What we need is to have allCompleted return REPLAYED as it should, but
     * for entries to remain in the cache for a period of time to catch any
     * timing issues, such as when an upcall recovery passes a downcall
     * recovery.
     */

    public boolean allCompleted ()
    {
        synchronized (this)
        {
            if ((super.preparedList != null) && (super.preparedList.size() > 0))
                return false;

            if ((super.failedList != null) && (super.failedList.size() > 0))
                return false;

            if ((super.pendingList != null) && (super.pendingList.size() > 0))
                return false;

            if ((super.heuristicList != null)
                    && (super.heuristicList.size() > 0))
                return false;

            return true;
        }
    }

    public String type ()
    {
        if (_typeName == null)
        {
            return super.type();
        }
        else
        {
            return _typeName;
        }
    }

    public void removeOldStoreEntry ()
    {
        try
        {
            store().remove_committed(get_uid(), super.type());
        }
        catch (ObjectStoreException ex) {
            jtsLogger.i18NLogger.warn_recovery_transactions_RecoveredTransaction_8(ex);
        }
    }

    public boolean assumeComplete ()
    {
        _typeName = AssumedCompleteTransaction.typeName();

        return true;
    }

    /**
     * Override StateManager packHeader so it gets the original processUid, not
     * this process's
     * 
     * @since JTS 2.1.
     */

    protected void packHeader (OutputObjectState os, Header hdr)
            throws IOException
    {
        /*
         * If there is a transaction present than pack the process Uid of this
         * JVM and the tx id. Otherwise pack a null Uid.
         */

        super.packHeader(os, new Header(hdr.getTxId(), _originalProcessUid));
    }

    /**
     * Override StateManager's unpackHeader to save the processUid of the
     * original process
     * 
     * @since JTS 2.1.
     */

    protected void unpackHeader (InputObjectState os, Header hdr)
            throws IOException
    {
        super.unpackHeader(os, hdr);
        
        _originalProcessUid = hdr.getProcessId();
    }

    public boolean save_state (OutputObjectState objectState, int ot)
    {
        // do the other stuff
        boolean result = super.save_state(objectState, ot);

        // iff assumed complete, include the time (this should happen only once)
        if (_typeName != null && result)
        {
            Date lastActiveTime = new Date();
            try
            {
                objectState.packLong(lastActiveTime.getTime());
            }
            catch (java.io.IOException ex)
            {
            }
        }
        return result;
    }

    /** do not admit to being inactive */
    public Date getLastActiveTime ()
    {
        return null;
    }

    private String _typeName;

    private boolean _reportHeuristics = false;

    private int _recoveryStatus = RecoveryStatus.NEW;

    protected Uid _originalProcessUid;

    private org.omg.CosTransactions.Status _txStatus;

}
