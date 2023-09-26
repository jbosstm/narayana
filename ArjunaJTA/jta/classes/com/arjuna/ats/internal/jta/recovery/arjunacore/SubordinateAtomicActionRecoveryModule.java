/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;
import com.arjuna.ats.jta.logging.jtaLogger;

import javax.transaction.xa.XAException;
import java.io.IOException;
import java.util.Vector;


/**
 * This class is purely used by the recovery system to load the transactions into memory so we can be sure
 * that orphan detection can find the TransactionImple if the EIS has not called XATerminator::recover yet
 *
 * At the time of writing this module is utilized by the SubordinationManagerXAResourceOrphanFilter to ensure that
 * it can check with the SubordinationManager if the transaction is in flight during orphan detection.
 */
public class SubordinateAtomicActionRecoveryModule implements RecoveryModule {

    private boolean recoveryScanCompletedWithoutError;
    private boolean validatePosition;

    @Override
    public void periodicWorkFirstPass() {
        /*
         * Requires going through the objectstore for the states of imported
         * transactions - this is just to make sure the server control are loaded into memory.
         *
         * The EIS will call XATerminator::recover() for actual crash recovery
         */

        RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
        InputObjectState states = new InputObjectState();

        // only look in the JCA section of the object store
        Uid uid = null;

        try {
            if (recoveryStore.allObjUids(SubordinateAtomicAction.getType(), states)
                    && (states.notempty())) {
                while (true) {
                    uid = UidHelper.unpackFrom(states);
                    if (uid.notEquals(Uid.nullUid())) {
                        SubordinationManager
                                .getTransactionImporter().recoverTransaction(
                                uid);
                    } else {
                        break;
                    }
                }
            }
            recoveryScanCompletedWithoutError = true;
        } catch (Exception e) {
            jtaLogger.i18NLogger.warn_could_not_recover_subordinate(uid, e);
            recoveryScanCompletedWithoutError = false;
        }

        if (!validatePosition()) {
            recoveryScanCompletedWithoutError = false;
        }
    }

    @Override
    public void periodicWorkSecondPass() {
        // No-op - recovery is performed by the EIS
    }

    /**
     * Used to ensure that the orphan detection has fully loaded the transaction state before asserting
     * a decision.
     *
     * @return Whether the last recovery scan completed without an error
     */
    public boolean isRecoveryScanCompletedWithoutError() {
        return recoveryScanCompletedWithoutError;
    }

    /**
     * It is important to verify that the SubordinateAtomicActionRecoveryModule has been loaded before the XARecoveryModule
     * as otherwise when we check for an imported transaction it may not have been loaded by the  SubordinateAtomicActionRecoveryModule yet.
     *
     * @return true if and only if SubordinateAtomicActionRecoveryModule appears before XARecoveryModule in the modules
     */
    private boolean validatePosition() {
        if (!this.validatePosition) {
            Vector<RecoveryModule> modules = RecoveryManager.manager().getModules();
            boolean foundSelf = false;
            for (RecoveryModule module : modules) {
                if (module instanceof SubordinateAtomicActionRecoveryModule) {
                    foundSelf = true;
                } else if (module instanceof XARecoveryModule) {
                    // If we have found the XARecoveryModule (which in practice must be there because the filter is running
                    // we need to check to make sure that the class responsible for loading SubordinateAtomicActions is
                    // configured to run before it
                    if (!foundSelf) {
                        return false;
                    } else {
                        this.validatePosition = true;
                        break;
                    }
                }
            }
        }
        return this.validatePosition;
    }
}