package org.jboss.jbossts.xts.recovery.participant.at;

import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.PersistableParticipant;
import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.subordinate.SubordinateCoordinator;
import com.arjuna.ats.arjuna.state.InputObjectState;

import java.io.ObjectInputStream;

/**
 * A recovery module which recovers durable participants registered by subordinate coordinators
 */

public class XTSATSubordinateRecoveryModule implements XTSATRecoveryModule
{
    public Durable2PCParticipant deserialize(String id, ObjectInputStream stream) throws Exception {
        if (id.startsWith(SubordinateCoordinator.PARTICIPANT_PREFIX)) {
            // throw an exception because we don't expect these participants to use serialization
            throw new Exception("XTSATSubordinateRecoveryModule : invalid request to deserialize() subordinate WS-AT coordinator durable participant " + id);
        }
        return null;
    }

    public Durable2PCParticipant recreate(String id, byte[] recoveryState) throws Exception {
        if (id.startsWith(SubordinateCoordinator.PARTICIPANT_PREFIX)) {
            InputObjectState ios = new InputObjectState();
            ios.setBuffer(recoveryState);
            String className = ios.unpackString();
            Class participantClass =  this.getClass().getClassLoader().loadClass(className);
            Durable2PCParticipant participant = (Durable2PCParticipant)participantClass.newInstance();
            ((PersistableParticipant)participant).restoreState(ios);
            return participant;
        }
        return null;
    }
}
