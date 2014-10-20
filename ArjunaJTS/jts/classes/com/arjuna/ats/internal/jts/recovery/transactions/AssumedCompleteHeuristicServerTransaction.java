/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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