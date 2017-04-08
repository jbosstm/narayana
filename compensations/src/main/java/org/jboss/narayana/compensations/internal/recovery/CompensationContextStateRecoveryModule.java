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

package org.jboss.narayana.compensations.internal.recovery;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.internal.context.CompensationContextState;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.local.LocalParticipantRecoveryModule;
import org.jboss.narayana.compensations.internal.utils.RecoveryHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * Recovery module to remove unmanaged compensation context state records.
 * 
 * Normally these records are removed when transaction is completed. However, in case of a system crash and participants being
 * completed by recovery manager, these records must be removed by this module.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CompensationContextStateRecoveryModule implements RecoveryModule {

    private static final Logger LOGGER = Logger.getLogger(LocalParticipantRecoveryModule.class);

    private final Set<Uid> firstPassUids = new HashSet<>();

    private final RecoveryHelper recoveryHelper = new RecoveryHelper(StoreManager.getRecoveryStore(),
            CompensationContextState.getRecordType());

    private final DeserializerHelper deserializerHelper = new DeserializerHelper();

    private final CompensationContextStateManager compensationContextStateManager = CompensationContextStateManager
            .getInstance();

    @Override
    public void periodicWorkFirstPass() {
        LOGGER.tracef("Periodic work first pass");
        firstPassUids.clear();
        firstPassUids.addAll(recoveryHelper.getAllUids(e -> LOGGER.warnf(e, "Failed to get uids")));
        LOGGER.tracef("First pass found uids to recover: '%s'", firstPassUids);
    }

    @Override
    public void periodicWorkSecondPass() {
        LOGGER.tracef("Periodic work second pass");
        recoveryHelper.getAllUids(e -> LOGGER.warnf(e, "Failed to get uids")).stream().filter(firstPassUids::contains)
                .forEach(this::removeState);
    }

    /**
     * Compensation context state without attached participants should be removed.
     *
     * @param uid record uid.
     */
    private void removeState(Uid uid) {
        recoveryHelper.getRecord(uid, e -> LOGGER.warnf(e, "Failed to get record")).ifPresent(record -> {
            CompensationContextState state = new CompensationContextState(deserializerHelper);
            if (!state.restore(record)) {
                LOGGER.warnf("Failed to restore compensation context state");
            } else if (!state.hasAttachedParticipants()) {
                LOGGER.tracef("removing compensation context state for transaction '%", state.getTransactionId());
                compensationContextStateManager.remove(state.getTransactionId());
            }
        });
    }

}
