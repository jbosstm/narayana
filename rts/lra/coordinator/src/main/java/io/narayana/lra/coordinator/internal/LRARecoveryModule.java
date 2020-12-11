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
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import io.narayana.lra.logging.LRALogger;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import io.narayana.lra.coordinator.domain.model.LongRunningAction;
import io.narayana.lra.coordinator.domain.service.LRAService;
import org.eclipse.microprofile.lra.annotation.LRAStatus;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class LRARecoveryModule implements RecoveryModule {
    public LRARecoveryModule() {
        service = new LRAService();

        if (_recoveryStore == null) {
            _recoveryStore = StoreManager.getRecoveryStore();
        }

        _transactionStatusConnectionMgr = new TransactionStatusConnectionManager();
        Implementations.install();
    }

    public static LRAService getService() {
        if (service == null) {
            // service is a cached static value and is only set once (to the instance variable
            // held by the LRARecoveryModule singleton).
            // This means that if two threads try to set it they will set it to the same value.
            // Also, the code called by this routine which (indirectly) creates the recovery module
            // is also reentrant. Therefore this routine is reentrant and there is no necessity for
            // synchronisation
            RecoveryManager.manager();

            List<RecoveryModule> modules = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryModules();

            for (RecoveryModule rm : modules) {
                if (rm instanceof LRARecoveryModule) {
                    return service;
                }
            }

            String errorMsg = LRALogger.i18NLogger.error_recovery_missing_module();

            LRALogger.logger.error(errorMsg);

            throw new RuntimeException(errorMsg); // misconfiguration
        }

        return service;
    }

    /**
     * This is called periodically by the RecoveryManager
     */
    public void periodicWorkFirstPass() {
        if (LRALogger.logger.isTraceEnabled()) {
            LRALogger.logger.trace("LRARecoveryModule: first pass");
        }
    }

    public void periodicWorkSecondPass() {
        if (LRALogger.logger.isTraceEnabled()) {
            LRALogger.logger.trace("LRARecoveryModule: second pass");
        }

        recoverTransactions();
    }

    private synchronized void recoverTransactions() {
         // uids per transaction type
        InputObjectState aa_uids = new InputObjectState();

        if (getUids(aa_uids)) {
            processTransactionsStatus(processTransactions(aa_uids));
        }
    }

    private void doRecoverTransaction(Uid recoverUid) {
        // Retrieve the transaction status from its original process.
        int theStatus = _transactionStatusConnectionMgr.getTransactionStatus(_transactionType, recoverUid);

        try {
            RecoveringLRA lra = new RecoveringLRA(service, recoverUid, theStatus);
            boolean inFlight = (lra.getLRAStatus() == LRAStatus.Active);

            LRAStatus lraStatus = lra.getLRAStatus();
            if (LRAStatus.FailedToCancel.equals(lraStatus) || LRAStatus.FailedToClose.equals(lraStatus)) {
                return;
            }

            if (!service.hasTransaction(lra.getId())) {
                // make sure LRAService knows about it
                service.addTransaction(lra);
            }

            if (LRALogger.logger.isDebugEnabled()) {
                LRALogger.logger.debug("LRARecoverModule: transaction type is " + _transactionType + " uid is " +
                        recoverUid.toString() + "\n Status is " + lraStatus +
                        " in flight is " + inFlight);
            }

            if (!inFlight && lra.hasPendingActions()) {
                lra.replayPhase2();

                if (!lra.isRecovering()) {
                    service.finished(lra, false);
                }
            }

        } catch (Exception e) {
            if (LRALogger.logger.isInfoEnabled()) {
                LRALogger.logger.infof(
                        "LRARecoverModule: Error '%s' while recovering LRA record %s",
                        e.getMessage(), recoverUid.fileStringForm());
            }
        }
    }

    private Collection<Uid> processTransactions(InputObjectState uids) {
        Collection<Uid> uidCollection = new ArrayList<>();

        if (LRALogger.logger.isDebugEnabled()) {
            LRALogger.logger.debugf("LRARecoverModule: processing transaction type %s", _transactionType);
        }

        Consumer<Uid> uidUnpacker = uidCollection::add;

        forEach(uids, uidUnpacker);

        return uidCollection;
    }

    private void processTransactionsStatus(Collection<Uid> uids) {
        // Process the collection of transaction Uids
        uids.forEach(uid -> {
            try {
                if (_recoveryStore.currentState(uid, _transactionType) != StateStatus.OS_UNKNOWN) {
                    doRecoverTransaction(uid);
                }
            } catch (ObjectStoreException e) {
                if (LRALogger.logger.isTraceEnabled()) {
                    LRALogger.logger.tracef(e,
                            "LRARecoverModule: Object store exception '%s' while reading the current state of LRA record %s:",
                            e.getMessage(), uid.fileStringForm());
                } else if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof(
                            "LRARecoverModule: Object store exception '%s' while reading the current state of LRA record %s",
                            e.getMessage(), uid.fileStringForm());
                }
            }
        });

    }

    public void getRecoveringLRAs(Map<URI, LongRunningAction> lras) {
        InputObjectState aa_uids = new InputObjectState();

        // for LRA call reactivate and add it to the input list
        if (getUids(aa_uids)) {
            Collection<Uid> uids = processTransactions(aa_uids);
            uids.forEach(uid -> {
                int status = _transactionStatusConnectionMgr.getTransactionStatus(_transactionType, uid);
                RecoveringLRA lra = new RecoveringLRA(service, uid, status);

                if (lra.isActivated()) {
                    lras.put(lra.getId(), lra);
                } else {
                    LRALogger.logger.infof("LRARecoverModule: failed to activate LRA record %s",
                            uid.fileStringForm());
                }
            });
        }
    }

    /**
     * remove an LRA log record
     *
     * @param lraUid LRA id that will be removed from the log record
     * @return false if record isn't in the store or there was an error removing it
     */
    public boolean removeCommitted(Uid lraUid) {
        try {
            return _recoveryStore.remove_committed(lraUid, _transactionType);
        } catch (ObjectStoreException e) {
            if (LRALogger.logger.isTraceEnabled()) {
                LRALogger.logger.tracef(e,
                        "LRARecoveryModule: Object store exception '%s' while removing LRA record %s",
                        e.getMessage(), lraUid.fileStringForm());
            } else if (LRALogger.logger.isInfoEnabled()) {
                LRALogger.logger.infof(
                        "LRARecoveryModule: Object store exception '%s' while removing LRA record %s",
                        e.getMessage(), lraUid.fileStringForm());
            }
        }

        return false;
    }

    public void recover() {
        recoverTransactions();
    }

    public void getFailedLRAs(Map<URI, LongRunningAction> lras) {
        InputObjectState aa_uids = new InputObjectState();
        Consumer<Uid> failedLRACreator = uid -> {
            LongRunningAction lra = new LongRunningAction(service, new Uid(uid));
            lra.activate();

            LRAStatus status = lra.getLRAStatus();
            if (LRAStatus.FailedToCancel.equals(status) || LRAStatus.FailedToClose.equals(status)) {
                lras.put(lra.getId(), lra);
            }
        };

        if (getUids(aa_uids)) {
            forEach(aa_uids, failedLRACreator);
        }
    }

    private boolean getUids(InputObjectState aa_uids) {
        synchronized (this) {
            try {
                return _recoveryStore.allObjUids(_transactionType, aa_uids);
            } catch (ObjectStoreException e) {
                if (LRALogger.logger.isTraceEnabled()) {
                    LRALogger.logger.tracef(e,
                            "LRARecoverModule: Object store exception %s while unpacking records of type %s",
                            e.getMessage(), _transactionType);
                } else if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof(
                            "LRARecoverModule: Object store exception %s while unpacking records of type %s",
                            e.getMessage(), _transactionType);
                }

                return false;

            }
        }
    }

    /**
     * Iterate over a collection of Uids
     *
     * @param uids the uids to iterate over
     * @param consumer the consumer that should be called for each Uid
     */
    // This method could be moved to an ArjunaCore class (such as UidHelper) if its useful,
    // in which case make the method returns a boolean:
    //     * @return false if there was an error processing the collection of uids
    private void forEach(InputObjectState uids, Consumer<Uid> consumer) {
        do {
            try {
                Uid uid = new Uid(uids.unpackBytes());

                if (uid.equals(Uid.nullUid())) {
                    return;
                }

                consumer.accept(uid);
            } catch (IOException e) {
                if (LRALogger.logger.isTraceEnabled()) {
                    LRALogger.logger.tracef(e,
                            "LRARecoverModule: Object store exception %s while unpacking a record of type %s",
                            e.getMessage(), _transactionType);
                } else if (LRALogger.logger.isInfoEnabled()) {
                    LRALogger.logger.infof(
                            "LRARecoverModule: Object store exception %s while unpacking a record of type: %s",
                            e.getMessage());
                }

                return;
            }

        } while (true);
    }

    private static LRAService service;

    // 'type' within the Object Store for LRAs.
    private final String _transactionType = LongRunningAction.getType();

    // Reference to the Object Store.
    private static RecoveryStore _recoveryStore = null;

    // This object manages the interface to all TransactionStatusManager
    // processes(JVMs) on this system/node.
    private final TransactionStatusConnectionManager _transactionStatusConnectionMgr;
}
