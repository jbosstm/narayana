/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import java.io.IOException;
import java.util.Date;

import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * @author <a href="gytis@redhat.com">Gytis Trikleris</a>
 */
public final class AssumedCompleteHeuristicServerTransaction extends RecoveredServerTransaction {

    private static final String ourTypeName = "/StateManager/BasicAction/TwoPhaseCoordinator/ArjunaTransactionImple/AssumedCompleteHeuristicServerTransaction";
    
    private Date lastActiveTime;
    
    public AssumedCompleteHeuristicServerTransaction(final Uid actionUid) {
        super(actionUid, ourTypeName);
        
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug(AssumedCompleteHeuristicServerTransaction.class.getSimpleName() + " " + get_uid() + " created");
        }
    }
    
    public static String typeName() {
        return ourTypeName;
    }
    
    public Status getOriginalStatus() {
        return Status.StatusNoTransaction;
    }
    
    public String type() {
        return AssumedCompleteHeuristicServerTransaction.typeName();
    }
    
    public String toString() {
        return AssumedCompleteHeuristicServerTransaction.class.getSimpleName() + " <" + get_uid() + ">";
    }
    
    public boolean assumeComplete() {
        return false;
    }
    
    public Date getLastActiveTime() {
        return lastActiveTime;
    }
    
    public boolean restore_state(final InputObjectState objectState, final int ot) {
        final boolean result = super.restore_state(objectState, ot);

        if (result) {
            try {
                final long oldtime = objectState.unpackLong();
                lastActiveTime = new Date(oldtime);
            } catch (java.io.IOException ex) {
                lastActiveTime = new Date();
            }
        }
        
        return result;
    }
    
    public boolean save_state(final OutputObjectState objectState, final int ot) {
        final boolean result = super.save_state(objectState, ot);

        if (result) {
            lastActiveTime = new Date();
            
            try {
                objectState.packLong(lastActiveTime.getTime());
            } catch (final IOException ex) {
            }
        }
        
        return result;
    }

}