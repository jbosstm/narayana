/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package io.narayana.lra.client;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface LRAClientAPI {

    /**
     * Start a new LRA
     *
     * The LRA model uses a presumed nothing protocol: the coordinator must communicate with Compensators
     * in order to inform them of the LRA activity. Every time a Compensator is enrolled with a LRA, the
     * coordinator must make information about it durable so that the Compensator can be contacted when
     * the LRA terminates, even in the event of subsequent failures. Compensators, clients and coordinators
     * cannot make any presumption about the state of the global transaction without consulting the
     * coordinator and all compensators, respectively.
     *
     * @param clientID Each client is expected to have a unique identity (which can be a URL). (optional)
     * @param timeout Specifies the maximum time in seconds that the LRA will exist for. If the LRA is
     *                terminated because of a timeout, the LRA URL is deleted. All further invocations
     *                on the URL will return 404. The invoker can assume this was equivalent to a compensate
     *                operation. (optional, default to 0)
     * @throws GenericLRAException Comms error
     */
    URL startLRA(String clientID, Long timeout) throws GenericLRAException;

    /**
     *
     * @param parentLRA the parent LRA if this new LRA is nested
     * @param clientID Each client is expected to have a unique identity (which can be a URL). (optional)
     * @param timeout Specifies the maximum time in seconds that the LRA will exist for. If the LRA is
     *                terminated because of a timeout, the LRA URL is deleted. All further invocations
     *                on the URL will return 404. The invoker can assume this was equivalent to a compensate
     *                operation. (optional, default to 0)
     * @param unit
     * @return the id of the new LRA
     * @throws GenericLRAException
     */
    URL startLRA(URL parentLRA, String clientID, Long timeout, TimeUnit unit) throws GenericLRAException;

    /**
     * Attempt to cancel an LRA
     *
     * Trigger the compensation of the LRA. All compensators will be triggered by the coordinator
     * (ie the compensate message will be sent to each compensators). Upon termination, the URL is
     * implicitly deleted. The invoker cannot know for sure whether the lra completed or compensated
     * without enlisting a participant.
     *
     * @param lraId The unique identifier of the LRA (required)
     * @return participant sepcific data provided by the application
     *         for nested LRA the API implementation will combine participant data into a
     *         JSON encoded array. This means that compensators MUST not return any data
     *         that starts with the JSON array start token (ie a '[' character)
     * @throws GenericLRAException Comms error
     */
    String cancelLRA(URL lraId) throws GenericLRAException;

    /**
     * Attempt to close an LRA
     *
     * Trigger the successful completion of the LRA. All compensators will be dropped by the coordinator.
     * The complete message will be sent to the compensators. Upon termination, the URL is implicitly
     * deleted. The invoker cannot know for sure whether the lra completed or compensated without enlisting
     * a participant.
     *
     * @param lraId The unique identifier of the LRA (required)
     * @return participant sepcific data provided by the application
     *         for nested LRA the API implementation will combine participant data into a
     *         JSON encoded array. This means that compensators MUST not return any data
     *         that starts with the JSON array start token (ie a '[' character)
     * @throws GenericLRAException Comms error
     */
    String closeLRA(URL lraId) throws GenericLRAException;

    /**
     * Lookup active LRAs
     * 
     * @throws GenericLRAException Comms error
     */
    List<LRAStatus> getActiveLRAs() throws GenericLRAException;

    /**
     * Returns all LRAs
     *
     * Gets both active and recovering LRAs
     *
     * @return List<LRA>
     * @throws GenericLRAException Comms error
     */
    List<LRAStatus> getAllLRAs() throws GenericLRAException;

    /**
     * List recovering Long Running Actions
     *
     * Returns LRAs that are recovering (ie some compensators still need to be ran)
     *
     * @throws GenericLRAException Comms error
     */
    List<LRAStatus> getRecoveringLRAs() throws GenericLRAException;

    /**
     * Indicates whether an LRA is active
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @throws GenericLRAException Comms error
     */
    Boolean isActiveLRA(URL lraId) throws GenericLRAException;

    /**
     * Indicates whether an LRA was compensated
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @throws GenericLRAException Comms error
     */
    Boolean isCompensatedLRA(URL lraId) throws GenericLRAException;

    /**
     * Indicates whether an LRA is complete
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @throws GenericLRAException Comms error
     */
    Boolean isCompletedLRA(URL lraId) throws GenericLRAException;

    /**
     * A Compensator can join with the LRA at any time prior to the completion of an activity
     * 
     * @param lraId   The unique identifier of the LRA (required)
     * @param timelimit The time limit (in seconds) that the Compensator can guarantee that it
     *                can compensate the work performed by the service. After this time period has elapsed,
     *                it may no longer be possible to undo the work within the scope of this (or any enclosing)
     *                LRA. It may therefore be necessary for the application or service to start other
     *                activities to explicitly try to compensate this work. The application or coordinator may
     *                use this information to control the lifecycle of a LRA. (required)
     * @param body    The resource path that the LRA coordinator will use to drive the participant.
     *                Performing a GET on the participant URL will return the current status of the participant,
     *                or 404 if the participant is no longer present.  The following types must be returned by
     *                Compensators to indicate their current status:
     *                -  Compensating: the Compensator is currently compensating for the LRA;
     *                -  Compensated: the Compensator has successfully compensated for
     *                the LRA.
     *                -  FailedToCompensate: the Compensator was not able to compensate for the LRA.
     *                   It must maintain information about the work it was to compensate until the
     *                   coordinator sends it a forget message.
     *                -  Completing: the Compensator is tidying up after being told to complete.
     *                -  Completed: the coordinator/participant has confirmed.
     *                -  FailedToComplete: the Compensator was unable to tidy-up.
     *                     Performing a PUT on URL/compensate will cause the participant to compensate
     *                     the work that was done within the scope of the LRA.
     *                     Performing a PUT on URL/complete will cause the participant to tidy up and
     *                  it can forget this LRA.  (optional)
     *
     * @param compensatorData
     * @return a recovery URL for this enlistment
     *
     * @throws GenericLRAException Comms error
     */
    String joinLRA(URL lraId, Long timelimit, String body, String compensatorData) throws GenericLRAException;

    /**
     * Similar to {@link LRAClientAPI#joinLRA(URL, Long, String, String)} but the various participant urls
     * are passed in explicitly
     *
     * @param lraId The unique identifier of the LRA (required)
     * @param timelimit The time limit (in seconds) that the Compensator can guarantee that it
     *                can compensate the work performed by the service
     * @param compensateUrl Performing a PUT onthis URL will cause the participant to compensate the work that
     *                      was done within the scope of the LRA.
     * @param completeUrl Performing a PUT on this URL  will cause the participant to tidy up and it can forget this transaction.
     * @param leaveUrl Performing a PUT on this URL with cause the participant to leave the LRA
     * @param statusUrl Performing a GET on this URL will return the status of the participant {@see joinLRA}
     *
     * @param compensatorData
     * @return a recovery URL for this enlistment
     *
     * @throws GenericLRAException
     */
    String joinLRA(URL lraId, Long timelimit,
                   URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl,
                   String compensatorData) throws GenericLRAException;

    /**
     * Join an LRA passing in a class that will act as the participant.
     *
     * @param lraId The unique identifier of the LRA (required)
     * @param resourceClass An annotated class for the participant methods
     * @param baseUri Base uri for the participant endpoints
     * @param compensatorData Compensator specific data that the coordinator will pass to the participant when the LRA
     *                        is closed or cancelled
     * @return
     * @throws GenericLRAException
     */
    String joinLRA(URL lraId, Class<?> resourceClass, URI baseUri, String compensatorData) throws GenericLRAException;

    /**
     * Change the endpoints that a participant can be contacted on.
     *
     * @param recoveryUrl
     * @param compensateUrl
     * @param completeUrl
     * @param forgetUrl
     * @param leaveUrl
     * @param statusUrl
     * @param compensatorData
     * @return an updated recovery URL for this participant
     * @throws GenericLRAException
     */
    URL updateCompensator(URL recoveryUrl,URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl,
                           String compensatorData) throws GenericLRAException;

    /**
     * A Compensator can resign from the LRA at any time prior to the completion of an activity
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @param body  (optional)
     * @throws GenericLRAException Comms error
     */
    void leaveLRA(URL lraId, String body) throws GenericLRAException;

    /**
     * checks whether there is an LRA associated with the calling thread
     * @return
     */
    URL getCurrent();

    List<String> getResponseData(URL lraId);

    /**
     * LRAs can be created with timeouts after which they are cancelled. Use this method to update the timeout.
     *  @param lraId the id of the lra to update
     * @param limit the new timeout period
     * @param unit the time unit for limit
     */
    void renewTimeLimit(URL lraId, long limit, TimeUnit unit);

    /**
     * Update the clients notion of the current coordinator. Warning all further operations will be performed
     * on the LRA manager that created the passed in coordinator.
     *
     * @param coordinatorUrl the full url of an LRA
     */
    void setCurrentLRA(URL coordinatorUrl);
}
