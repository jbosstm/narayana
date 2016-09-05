/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2016,
 * @author JBoss Inc.
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
