package org.jboss.narayana.rest.integration.api;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public interface ParticipantsManager {

    /**
     * Returns the URL of the deployment which contains the ParticipantsManager.
     *
     * @return the base Url
     */
    String getBaseUrl();

    /**
     * Set the URL of the deployment which contains the ParticipantsManager.
     *
     * NOTE: not needed if application is deployed to the WildFly and uses RTS subsystem.
     *
     * @param baseUrl
     */
    void setBaseUrl(String baseUrl);

    /**
     * Enlist participant to REST-AT transaction.
     *
     * @param applicationId Application ID unique in the container scope.
     * @param participantEnlistmentURL Participant enlistment URL returned by the transaction manager after creating
     *                                 REST-AT transaction.
     * @param participant Participant to be enlisted.
     * @return Participant ID which can be later used to report heuristic decision.
     */
    String enlist(String applicationId, String participantEnlistmentURL, Participant participant);

    /**
     * Enlist volatile participant to REST-AT transaction.
     *
     * @param volatileParticipantEnlistmentURL VolatileParticipant enlistment URL returned by the transaction manager
     *                                         after creating REST-AT transaction.
     * @param volatileParticipant VolatileParticipant to be enlisted.
     */
    void enlistVolatileParticipant(String volatileParticipantEnlistmentURL, VolatileParticipant volatileParticipant);

    /**
     * Register ParticipantDeserializer instance which can be used during recovery to recreate participant instances.
     *
     * @param applicationId Application ID unique in the container scope.
     * @param deserializer Instance of ParticipantDeserializer.
     */
    void registerDeserializer(String applicationId, ParticipantDeserializer deserializer);

    /**
     * Report heuristic decision.
     *
     * @param participantId Participant ID received after enlisting participant to the transaction.
     * @param heuristicType Type of the heuristic.
     */
    void reportHeuristic(String participantId, HeuristicType heuristicType);

}
