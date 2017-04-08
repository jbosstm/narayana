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
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.mw.wscf.model.sagas.exceptions.CompensateFailedException;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.api.CompensationsRecoveryException;
import org.jboss.narayana.compensations.internal.local.LocalParticipant;
import org.jboss.narayana.compensations.internal.utils.RecoveryHelper;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class LocalParticipantRecord {

    private static final String TYPE = "/Compensations/Local/LocalParticipantRecord";

    private static final Logger LOGGER = Logger.getLogger(LocalParticipantRecord.class);

    private final RecoveryHelper recoveryHelper = new RecoveryHelper(StoreManager.getRecoveryStore(), TYPE);

    private final LocalParticipant participant;

    private LocalParticipantRecord(LocalParticipant participant) {
        this.participant = participant;
    }

    public static LocalParticipantRecord getInstance(InputObjectState state) throws CompensationsRecoveryException {
        LocalParticipant participant = new LocalParticipant();

        if (!participant.restore_state(state)) {
            LOGGER.warnf("Failed to restore participant");
            throw new CompensationsRecoveryException("Failed to restore participant");
        }

        return new LocalParticipantRecord(participant);
    }

    public static LocalParticipantRecord getInstance(LocalParticipant participant) {
        return new LocalParticipantRecord(participant);
    }

    public static String getType() {
        return TYPE;
    }

    public LocalParticipant getParticipant() {
        return participant;
    }

    public boolean persist() {
        Uid uid = new Uid(participant.id());
        OutputObjectState state = new OutputObjectState(uid, TYPE);

        if (!participant.save_state(state)) {
            LOGGER.warnf("Failed to serialize participant state");
            return false;
        }

        return recoveryHelper.writeRecord(state, e -> LOGGER.warnf(e, "Failed to persist participant state"));
    }

    public void remove() throws CompensationsRecoveryException {
        recoveryHelper.removeRecordWithException(new Uid(participant.id()),
                e -> new CompensationsRecoveryException("Failed to remove participant record", e));
    }

    public void compensate() throws CompensateFailedException {
        participant.compensate();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{participant=" + participant + "}";
    }

}
