package org.jboss.narayana.rts.lra.client.compensator;

public interface LRAManagement {
    /**
     * join an LRA
     * @param compensator an instance of a {@link Compensator} that will be notified when the target LRA ends
     * @param deserializer a mechanism for recreating compensators during recovery
     * @param lraId the LRA to join
     */
    void joinLRA(Compensator compensator, CompensatorDeserializer deserializer, String lraId);
}
