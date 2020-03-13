/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.coordinator.internal;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import io.narayana.lra.logging.LRALogger;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import io.narayana.lra.coordinator.domain.model.Transaction;
import io.narayana.lra.coordinator.domain.service.LRAService;

import java.net.URI;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

public class LRARecoveryModule implements RecoveryModule {
    public LRARecoveryModule(LRAService lraService) {
        this.lraService = lraService;

        if (_recoveryStore == null) {
            _recoveryStore = StoreManager.getRecoveryStore();
        }

        _transactionStatusConnectionMgr = new TransactionStatusConnectionManager();

        RecordTypeManager.manager().add(new RecordTypeMap() {
            @Override
            public Class<? extends AbstractRecord> getRecordClass() {
                return null;
            }

            @Override
            public int getType() {
                return 0;
            }
        });
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void periodicWorkFirstPass() {
        // uids per transaction type
        InputObjectState aa_uids = new InputObjectState();

        try {
            if (_recoveryStore.allObjUids(_transactionType, aa_uids)) {
                _transactionUidVector = processTransactions(aa_uids);
            }
        } catch (ObjectStoreException e) {
            if (LRALogger.logger.isInfoEnabled()) {
                LRALogger.logger.infof("LRARecoverModule: Object store exception: %s", e.getMessage());
            }
        }
    }

    public void periodicWorkSecondPass() {
        if (LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debug("LRARecoveryModule second pass");
        }

        processTransactionsStatus();
    }

    private void doRecoverTransaction(Uid recoverUid) {
        // Retrieve the transaction status from its original process.
        int theStatus = _transactionStatusConnectionMgr.getTransactionStatus(_transactionType, recoverUid);

//        boolean inFlight = false; // TODO figure out how to determine isTransactionInMidFlight(theStatus);

            try {
                RecoveringLRA lra = new RecoveringLRA(lraService, recoverUid, theStatus);
                String Status = ActionStatus.stringForm(theStatus);
                boolean inFlight = lra.isActive();

                if (inFlight) {
                    // make sure LRAService knows about it
                    lraService.addTransaction(lra);
                }

                if (LRALogger.logger.isDebugEnabled()) {
                    LRALogger.logger.debug("transaction type is " + _transactionType + " uid is " +
                            recoverUid.toString() + "\n ActionStatus is " + Status +
                            " in flight is " + inFlight);
                }

                if (!inFlight && lra.hasPendingActions()) {
                    lra.replayPhase2();

                    if (!lra.isRecovering() && lraService != null) {
                        lraService.finished(lra, false);
                    }
                }

            } catch (Exception ex) {
                if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof("failed to recover Transaction %s: %s", recoverUid, ex.getMessage());
                }
            }
    }

    private Vector<Uid> processTransactions(InputObjectState uids) {
        Vector<Uid> uidVector = new Vector<>();

        if (LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debugf("processing transaction type %s", _transactionType);
        }

        boolean moreUids = true;

        while (moreUids) {
            try {
                Uid uid = UidHelper.unpackFrom(uids);

                if (uid.equals(Uid.nullUid())) {
                    moreUids = false;
                } else {
                    Uid newUid = new Uid(uid);

                    if (LRALogger.logger.isDebugEnabled()) {
                        LRALogger.logger.debug("found transaction " + newUid);
                    }

                    uidVector.addElement(newUid);
                }
            } catch (Exception ex) {
                moreUids = false;
            }
        }

        return uidVector;
    }

    private void processTransactionsStatus() {
        // JBTM-2016 If the volatile object store is used we would not be able
        // to recover anything but if this module is still configured it would
        // get an NPE
        if (_transactionUidVector != null) {
            // Process the Vector of transaction Uids
            Enumeration<?> transactionUidEnum = _transactionUidVector.elements();

            while (transactionUidEnum.hasMoreElements()) {
                Uid currentUid = (Uid) transactionUidEnum.nextElement();

                try {
                    if (_recoveryStore.currentState(currentUid, _transactionType) != StateStatus.OS_UNKNOWN) {
                        doRecoverTransaction(currentUid);
                    }
                } catch (ObjectStoreException e) {
                    if (LRALogger.logger.isInfoEnabled()) {
                        LRALogger.logger.infof("failed to access transaction store %s: %s",
                                currentUid, e.getMessage());
                    }
                }
            }
        }
    }

    public void getRecoveringLRAs(Map<URI, Transaction> lras) {

        periodicWorkFirstPass();

        if (_transactionUidVector != null) {
            Enumeration transactionUidEnum = _transactionUidVector.elements();

            while (transactionUidEnum.hasMoreElements()) {
                Uid currentUid = (Uid) transactionUidEnum.nextElement();
                int status = _transactionStatusConnectionMgr.getTransactionStatus(_transactionType, currentUid);
                RecoveringLRA lra = new RecoveringLRA(lraService, currentUid, status);

                if (lra.isActivated()) {
                    lras.put(lra.getId(), lra);
                } else {
                    LRALogger.logger.infof("failed to activate LRA %s", currentUid);
                }
            }
        }
    }

    LRAService lraService;

    // 'type' within the Object Store for LRAs.
    private String _transactionType = io.narayana.lra.coordinator.domain.model.Transaction.getType();

    // Array of transactions found in the object store of the type LRA
    private Vector _transactionUidVector = null;

    // Reference to the Object Store.
    private static RecoveryStore _recoveryStore = null;

    // This object manages the interface to all TransactionStatusManager
    // processes(JVMs) on this system/node.
    private TransactionStatusConnectionManager _transactionStatusConnectionMgr;
}
