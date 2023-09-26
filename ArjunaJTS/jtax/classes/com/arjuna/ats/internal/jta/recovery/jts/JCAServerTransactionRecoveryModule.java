/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.jts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction;

import javax.transaction.xa.XAException;
import java.io.IOException;


/**
 * This class is purely used by the recovery system to load the transactions into memory so we can be sure
 * that bottom-up recovery could find a serverControl if the EIS has not called XATerminator::recover yet
 */
public class JCAServerTransactionRecoveryModule implements RecoveryModule {

    @Override
    public void periodicWorkFirstPass() {
                /*
         * Requires going through the objectstore for the states of imported
         * transactions - this is just to make sure the server control are loaded into memory.
         *
         * The XATerminatorImple::recover() method is used for actual crash recovery
         */

        try {
            RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
            InputObjectState states = new InputObjectState();

            // only look in the JCA section of the object store

            if (recoveryStore.allObjUids(ServerTransaction.getType(), states)
                    && (states.notempty())) {
                boolean finished = false;

                do {
                    Uid uid = UidHelper.unpackFrom(states);
                    if (uid.notEquals(Uid.nullUid())) {
                        SubordinationManager
                                .getTransactionImporter().recoverTransaction(
                                uid);
                    } else {
                        finished = true;
                    }
                }
                while (!finished);
            }
        } catch (ObjectStoreException | XAException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void periodicWorkSecondPass() {

    }
}