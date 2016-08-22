/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.internal.recovery.local;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.mw.wscf.model.sagas.exceptions.CompensateFailedException;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.BACoordinator;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.CompensationsRecoveryException;
import org.jboss.narayana.compensations.internal.utils.RecoveryHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LocalParticipantRecoveryModule implements RecoveryModule {

    private static final Logger LOGGER = Logger.getLogger(LocalParticipantRecoveryModule.class);

    private final Set<Uid> firstPassUids = new HashSet<>();

    private final RecoveryHelper localParticipantRecoveryHelper = new RecoveryHelper(StoreManager.getRecoveryStore(),
            LocalParticipantRecord.getType());

    private final RecoveryHelper baCoordinatorRecoveryHelper = new RecoveryHelper(StoreManager.getRecoveryStore(),
            new BACoordinator().type());

    @Override
    public void periodicWorkFirstPass() {
        LOGGER.tracef("Periodic work first pass");
        firstPassUids.clear();
        firstPassUids.addAll(localParticipantRecoveryHelper.getAllUids(e -> LOGGER.warnf(e, "Failed to get uids")));
        LOGGER.tracef("First pass found uids to recover: '%s'", firstPassUids);
    }

    @Override
    public void periodicWorkSecondPass() {
        LOGGER.tracef("Periodic work second pass");
        localParticipantRecoveryHelper.getAllUids(e -> LOGGER.warnf(e, "Failed to get uids")).stream()
                .filter(firstPassUids::contains).forEach(this::recoverParticipant);
    }

    /**
     * Participant with the provided uid is compensated unless BACoordinator record with the transaction id of this participant
     * exists.
     * 
     * @param uid
     */
    private void recoverParticipant(Uid uid) {
        localParticipantRecoveryHelper.getRecord(uid, e -> LOGGER.warnf(e, "Failed to recover participant"))
                .ifPresent(state -> {
                    try {
                        LocalParticipantRecord participantRecord = LocalParticipantRecord.getInstance(state);
                        if (shouldCompensate(participantRecord)) {
                            LOGGER.tracef("Compensating participant: '%s'", participantRecord);
                            participantRecord.compensate();
                        }
                    } catch (CompensationsRecoveryException e) {
                        LOGGER.warnf(e, "Failed to recover participant");
                    } catch (CompensateFailedException e) {
                        LOGGER.warnf(e, "Failed to compensate participant");
                    }
                });
    }

    /**
     * Method checks if the provided participant should be compensated. I looks for the BACoordinator record with the
     * coordinator id from the participant. If such record exists, recovery should be done by the BACoordinatorRecoveryModule.
     * 
     * @param participantRecord
     * @return
     */
    private boolean shouldCompensate(LocalParticipantRecord participantRecord) {
        Uid coordinatorId = new Uid(participantRecord.getParticipant().getCoordinatorId());
        try {
            return !baCoordinatorRecoveryHelper.getAllUidsWithException(e -> new CompensationsRecoveryException("Ignore"))
                    .contains(coordinatorId);
        } catch (CompensationsRecoveryException e) {
            return false;
        }
    }

}
