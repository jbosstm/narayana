package org.jboss.jbossts.xts.recovery.participant.ba;

import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.PersistableParticipant;
import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.subordinate.SubordinateBACoordinator;
import com.arjuna.ats.arjuna.state.InputObjectState;

import java.io.ObjectInputStream;

/**
 * A recovery module which recovers durable participants registered by subordinate coordinators
 */

public class XTSBASubordinateRecoveryModule implements XTSBARecoveryModule
{
    public BusinessAgreementWithParticipantCompletionParticipant deserializeParticipantCompletionParticipant(String id, ObjectInputStream stream) throws Exception {
        if (id.startsWith(SubordinateBACoordinator.PARTICIPANT_PREFIX)) {
            // throw an exception because we don't expect these participants to use serialization
            throw new Exception("XTSBASubordinateRecoveryModule : invalid request to deserialize() subordinate WS-BA coordinator participant completion participant " + id);
        }
        return null;
    }

    public BusinessAgreementWithParticipantCompletionParticipant recreateParticipantCompletionParticipant(String id, byte[] recoveryState) throws Exception {
        if (id.startsWith(SubordinateBACoordinator.PARTICIPANT_PREFIX)) {
            // throw an exception because we don't expect participant completion participants at present
            throw new Exception("XTSBASubordinateRecoveryModule : invalid request to deserialize() subordinate WS-BA coordinator participant completion participant " + id);
        }
        return null;
    }

    public BusinessAgreementWithCoordinatorCompletionParticipant deserializeCoordinatorCompletionParticipant(String id, ObjectInputStream stream) throws Exception {
        if (id.startsWith(SubordinateBACoordinator.PARTICIPANT_PREFIX)) {
            // throw an exception because we don't expect these participants to use serialization
            throw new Exception("XTSBASubordinateRecoveryModule : invalid request to deserialize() subordinate WS-BA coordinator coordinator completion participant " + id);
        }
        return null;
    }

    public BusinessAgreementWithCoordinatorCompletionParticipant recreateCoordinatorCompletionParticipant(String id, byte[] recoveryState) throws Exception {
        if (id.startsWith(SubordinateBACoordinator.PARTICIPANT_PREFIX)) {
            if (!id.endsWith("_CCP")) {
                // throw an exception because we don't expect participant completion participants at present
                throw new Exception("XTSBASubordinateRecoveryModule : invalid name for subordinate WS-BA coordinator coordinator completion participant participant " + id);
            }
            // ok, try to recreate the participant
            InputObjectState ios = new InputObjectState();
            ios.setBuffer(recoveryState);
            String className = ios.unpackString();
            Class participantClass =  this.getClass().getClassLoader().loadClass(className);
            BusinessAgreementWithCoordinatorCompletionParticipant participant = (BusinessAgreementWithCoordinatorCompletionParticipant)participantClass.newInstance();
            ((PersistableParticipant)participant).restoreState(ios);
            return participant;
        }
        return null;
    }

    /**
     * we don't need to use this because the BA recovery manager tracks whether a recovery scan has happened
     */
    public void endScan()
    {
        // do nothing
    }
}