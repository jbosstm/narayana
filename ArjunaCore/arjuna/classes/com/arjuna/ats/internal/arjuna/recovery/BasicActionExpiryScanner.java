/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.arjuna.recovery;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

/**
 * This class is a base class for an {@code ExpiryScanner} for expired
 * {@code BasicAction} items using creation time to evaluate if items are
 * considered too old. It uses
 * {@code RecoveryEnvironmentBean.expiryScanInterval} to calculate expiry time.
 * It does not need the application to run all the time to work since it only
 * needs one successful run that can be run immediately on application startup.
 * <p>
 * <strong>It is very important to know that application downtimes lasting
 * longer than expiry time will lead to immediate item handling before recovery
 * is getting into touch. That might cause corrupting the system!</strong>
 */
public abstract class BasicActionExpiryScanner<T extends BasicAction> implements ExpiryScanner {

    private static final Duration EXPIRY_TIME;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss");

    private final RecoveryStore recoveryStore;
    private final Class<T> typeClass;
    private final String typeName;
    private final String movedTypeName;

    static {
        EXPIRY_TIME = Duration.ofHours(recoveryPropertyManager.getRecoveryEnvironmentBean().getExpiryScanInterval());
    }

    public BasicActionExpiryScanner(T typeInstance, String movedSubFolder) {
        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debugf("%s - created, with expiry time of %s", getClass().getSimpleName(), EXPIRY_TIME);
        }
        this.recoveryStore = StoreManager.getRecoveryStore();
        this.typeClass = (Class<T>) typeInstance.getClass();
        this.typeName = typeInstance.type();
        if (movedSubFolder != null && !movedSubFolder.isEmpty()) {
            this.movedTypeName = this.typeName + movedSubFolder;
        } else {
            this.movedTypeName = null;
        }
    }

    private boolean isNotNullUid(Uid uid) {
        return !uid.equals(Uid.nullUid());
    }

    @Override
    public void scan() {
        Instant oldestSurviving = Instant.now().minus(EXPIRY_TIME);
        if (tsLogger.logger.isDebugEnabled()) {
            ZonedDateTime zdt = oldestSurviving.atZone(ZoneId.systemDefault());
            tsLogger.logger.debugf("%s - scanning to remove items from before %s", getClass().getSimpleName(), TIME_FORMAT.format(zdt));
        }
        try {
            InputObjectState uids = new InputObjectState();
            if (this.recoveryStore.allObjUids(this.typeName, uids)) {
                for (Uid uid = UidHelper.unpackFrom(uids); isNotNullUid(uid); uid = UidHelper.unpackFrom(uids)) {
                    T action = this.typeClass.getConstructor(Uid.class).newInstance(uid);
                    if (action.activate()) {
                        Instant creationTime = Instant.ofEpochMilli(action.getCreationTimeMillis());
                        if (creationTime.isBefore(oldestSurviving)) {
                            try {
                                if (this.movedTypeName != null) {
                                    moveEntry(uid);
                                } else {
                                    removeEntry(uid);
                                }
                            } catch (Exception ex) {
                                tsLogger.logger.warnf(ex, "%s - exception during attempted move of %s", getClass().getSimpleName(), uid);
                            }
                        }
                    }
                }
            }
        } catch (NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException ex) {
            tsLogger.logger.errorf(ex, "Action class %s could not be initiated", typeClass);
        } catch (Exception ex) {
            // end of uids!
        }
    }

    private void moveEntry(Uid uid) throws ObjectStoreException {
        InputObjectState state = this.recoveryStore.read_committed(uid, this.typeName);
        if (state != null) {
            tsLogger.logger.warnf("%s - %s is assumed complete and will be moved", getClass().getSimpleName(), uid);
            boolean moved = this.recoveryStore.write_committed(uid, this.movedTypeName, new OutputObjectState(state));
            if (moved) {
                removeEntry(uid);
            }
        }
    }

    private void removeEntry(Uid uid) throws ObjectStoreException {
        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debugf("%s - removing old transaction item %s", getClass().getSimpleName(), uid);
        }
        this.recoveryStore.remove_committed(uid, this.typeName);
    }

    @Override
    public boolean toBeUsed() {
        return !EXPIRY_TIME.isZero();
    }
}
