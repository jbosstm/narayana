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

package org.jboss.narayana.compensations.internal.recovery.remote;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryModule;
import org.jboss.logging.Logger;
import org.jboss.narayana.compensations.internal.ParticipantImpl;
import org.jboss.narayana.compensations.internal.context.CompensationContextStateManager;
import org.jboss.narayana.compensations.internal.recovery.DeserializerHelper;
import org.jboss.narayana.compensations.internal.remote.RemoteParticipant;

import java.io.ObjectInputStream;

/**
 * XTS business activity recovery module to recreate {@link RemoteParticipant} objects.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RemoteParticipantRecoveryModule implements XTSBARecoveryModule {

    private static final Logger LOGGER = Logger.getLogger(RemoteParticipantRecoveryModule.class);

    /**
     * Only this recreation method is implemented because RemoteParticipant is not serializable and is participant completion
     * participant.
     * 
     * @param id the id used when the participant was created
     * @param recoveryState a byte array returned form the original participant via a call to method getRecoveryState of
     *        interface PersistableBAParticipant
     * @return
     * @throws Exception
     */
    @Override
    public BusinessAgreementWithParticipantCompletionParticipant recreateParticipantCompletionParticipant(String id,
            byte[] recoveryState) throws Exception {
        LOGGER.tracef("Recreating participant id='%s'", id);

        InputObjectState state = new InputObjectState();
        state.setBuffer(recoveryState);

        if (!RemoteParticipant.class.getSimpleName().equals(state.unpackString())) {
            LOGGER.tracef("This is a state of a different participant type");
            return null;
        }

        ParticipantImpl participant = new ParticipantImpl(CompensationContextStateManager.getInstance(),
                new DeserializerHelper());
        if (!participant.restoreState(state)) {
            LOGGER.tracef("Failed to restore participant");
            return null;
        }

        RemoteParticipant remoteParticipant = new RemoteParticipant(participant);
        LOGGER.tracef("Restored participant='%s'", remoteParticipant);
        return remoteParticipant;
    }

    /**
     * Not supported.
     */
    @Override
    public BusinessAgreementWithCoordinatorCompletionParticipant recreateCoordinatorCompletionParticipant(String id,
            byte[] recoveryState) throws Exception {
        return null;
    }

    /**
     * Not supported.
     */
    @Override
    public BusinessAgreementWithParticipantCompletionParticipant deserializeParticipantCompletionParticipant(String id,
            ObjectInputStream stream) throws Exception {
        return null;
    }

    /**
     * Not supported.
     */
    @Override
    public BusinessAgreementWithCoordinatorCompletionParticipant deserializeCoordinatorCompletionParticipant(String id,
            ObjectInputStream stream) throws Exception {
        return null;
    }

    @Override
    public void endScan() {

    }

}
