package com.arjuna.mw.wscf.model.sagas.participants;

import com.arjuna.mwlabs.wscf.model.sagas.arjunacore.ACCoordinator;

/**
 * extension of the Participant API implemented by BA Participant Stubs which allows the coordinator
 * to establish a back channel from the participant engine to the corodinator during recovery processing.
 * the back channel is necessary so that remote participant requests can flow via the protocol engine to
 * to the coordinator. the back channel is normally established using the activivty mechanism but this
 * is not possible when recreating participant stubs and their engines during recovery as there is no thread
 * association in place.
 */
public interface RecoverableParticipant extends Participant
{
    public void setCoordinator(ACCoordinator coordinator);
}
