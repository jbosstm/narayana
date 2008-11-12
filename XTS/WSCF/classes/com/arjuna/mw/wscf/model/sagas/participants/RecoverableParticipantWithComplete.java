package com.arjuna.mw.wscf.model.sagas.participants;

/**
 * extension of the ParticipantWithComplete API implemented by BA Participant Records which allows the coordinator
 * to establish a back channel form the participant engine to the corodinator during recovery processing.
 * the back channel is necessary so that remote participant requests can flow via the protocol engine to
 * to the coordinator. the back channel is normally established using the activivty mechanism but this
 * is not possible when recreating participants and their engines during recovery as there is no thread
 * association in place.
 */
public interface RecoverableParticipantWithComplete extends RecoverableParticipant, ParticipantWithComplete
{
}
