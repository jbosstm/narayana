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

import io.narayana.lra.annotation.CompensatorStatus;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface LRAClientAPI {

    /**
     * Start a new LRA
     *
     * @param parentLRA The parent of the LRA that is about to start. If null then the new LRA will
     *                  be top level
     * @param clientID The client may provide a (preferably) unique identity which will be reported
     *                back when the LRA is queried.
     * @param timeout Specifies the maximum time that the LRA will exist for. If the LRA is
     *                terminated because of a timeout it will be cancelled.
     * @param unit Specifies the unit that the timeout is measured in
     *
     * @throws GenericLRAException a new LRA could not be started. The specific reason
     *                is available in {@link GenericLRAException#getStatusCode()}
     */
    URL startLRA(URL parentLRA, String clientID, Long timeout, TimeUnit unit) throws GenericLRAException;

    /**
     * Attempt to cancel an LRA
     *
     * Trigger compensation of all participants enlisted with the LRA (ie the compensate message will be
     * sent to each participant).
     *
     * @param lraId The unique identifier of the LRA (required)
     * @return the response MAY contain the final status of the LRA as reported by
     * {@link CompensatorStatus#name()}. If the final status is not returned the client can still discover
     * the final state using the {@link LRAClientAPI#getStatus(URL)} method
     * @throws GenericLRAException Communication error (the reason is availalbe via the
     * {@link GenericLRAException#getStatusCode()} method
     */
    String cancelLRA(URL lraId) throws GenericLRAException;

    /**
     * Attempt to close an LRA
     *
     * Tells the LRA to close normally. All participants will be triggered by the coordinator
     * (ie the complete message will be sent to each participant).
     *
     * @param lraId The unique identifier of the LRA (required)
     *
     * @return the response MAY contain the final status of the LRA as reported by
     * {@link CompensatorStatus#name()}. If the final status is not returned the client can still discover
     * the final state using the {@link LRAClientAPI#getStatus(URL)} method
     * @throws GenericLRAException Communication error (the reason is availalbe via the
     * {@link GenericLRAException#getStatusCode()} method
     */
    String closeLRA(URL lraId) throws GenericLRAException;

    /**
     * Lookup active LRAs
     * 
     * @throws GenericLRAException on error
     */
    List<LRAStatus> getActiveLRAs() throws GenericLRAException;

    /**
     * Returns all LRAs
     *
     * Gets both active and recovering LRAs
     *
     * @return List<LRA>
     * @throws GenericLRAException on error
     */
    List<LRAStatus> getAllLRAs() throws GenericLRAException;

    /**
     * List recovering Long Running Actions
     *
     * Returns LRAs that are recovering (ie the participant is still
     * attempting to complete or compensate
     *
     *
     * @throws GenericLRAException on error
     */
    List<LRAStatus> getRecoveringLRAs() throws GenericLRAException;

    /**
     * Lookup the status of an LRA
     *
     * @param lraId the LRA whose status is being requested
     * @return the status or null if the the LRA is still active (ie has not yet been closed or cancelled)
     * @throws GenericLRAException if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.
     */
    Optional<CompensatorStatus> getStatus(URL lraId) throws GenericLRAException;

    /**
     * Indicates whether an LRA is active. The same information can be obtained via a call to
     * {@link LRAClientAPI#getStatus(URL)}.
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @throws GenericLRAException if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.
     */
    Boolean isActiveLRA(URL lraId) throws GenericLRAException;

    /**
     * Indicates whether an LRA was compensated. The same information can be obtained via a call to
     * {@link LRAClientAPI#getStatus(URL)}.
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @throws GenericLRAException if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.
     */
    Boolean isCompensatedLRA(URL lraId) throws GenericLRAException;

    /**
     * Indicates whether an LRA is complete. The same information can be obtained via a call to
     * {@link LRAClientAPI#getStatus(URL)}.
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @throws GenericLRAException if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.     */
    Boolean isCompletedLRA(URL lraId) throws GenericLRAException;

    /**
     * A participant can join with the LRA at any time prior to the completion of an activity.
     * The participant provides end points on which it will listen for LRA related events.
     * 
     * @param lraId   The unique identifier of the LRA (required) to enlist with
     * @param timelimit The time limit (in seconds) that the participant can guarantee that it
     *                can compensate the work performed while the LRA is active.
     * @param body   The resource path or participant URL that the LRA coordinator will use
     *               to drive the participant. The coordinator uses the URL as follows:
     *
     *               - `{participant URL}/complete` is the `completion URL`,
     *               - `{participant URL}/compensate` is the `compensatation URL` and
     *               - `{participant URL}` serves as both the `status` and `forget` URLs.
     *
     * @param compensatorData data that will be stored with the coordinator and passed back to
     *                        the participant when the LRA is closed or cancelled
     * @return a recovery URL for this enlistment
     *
     * @throws GenericLRAException  if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.
     */
    String joinLRA(URL lraId, Long timelimit, String body, String compensatorData) throws GenericLRAException;

    /**
     * Similar to {@link LRAClientAPI#joinLRA(URL, Long, String, String)} except that the various
     * participant URLs are passed in explicitly.
     */
    String joinLRA(URL lraId, Long timelimit,
                   URL compensateUrl, URL completeUrl, URL forgetUrl, URL leaveUrl, URL statusUrl,
                   String compensatorData) throws GenericLRAException;

    /**
     * Join an LRA passing in a class that will act as the participant.
     * Similar to {@link LRAClientAPI#joinLRA(URL, Long, String, String)} but the various participant URLs
     * are expressed as CDI annotations on the passed in resource class.
     *
     * @param lraId The unique identifier of the LRA (required)
     * @param resourceClass An annotated class for the participant methods: {@link io.narayana.lra.annotation.Compensate},
     *                      etc.
     * @param baseUri Base uri for the participant endpoints
     * @param compensatorData Compensator specific data that the coordinator will pass to the participant when the LRA
     *                        is closed or cancelled
     * @return a recovery URL for this enlistment
     * @throws GenericLRAException if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.
     */
    String joinLRA(URL lraId, Class<?> resourceClass, URI baseUri, String compensatorData) throws GenericLRAException;

    /**
     * Change the endpoints that a participant can be contacted on.
     *
     * @param recoveryUrl the recovery URL returned from a participant join request
     * @param compensateUrl the URL to invoke when the LRA is cancelled
     * @param completeUrl the URL to invoke when the LRA is closed
     * @param statusUrl if a participant cannot finish immediately then it provides
     *                  this URL that the coordinator uses to monitor the progress
     * @param forgetUrl used to inform the participant that can forget about this LRA
     * @param compensatorData opaque data that returned to the participant when the LRA
     *                        is closed or cancelled
     * @return an updated recovery URL for this participant
     * @throws GenericLRAException if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.
     */
    URL updateCompensator(URL recoveryUrl,URL compensateUrl, URL completeUrl, URL forgetUrl, URL statusUrl,
                           String compensatorData) throws GenericLRAException;

    /**
     * A Compensator can resign from the LRA at any time prior to the completion of an activity
     * 
     * @param lraId The unique identifier of the LRA (required)
     * @param body  (optional)
     * @throws GenericLRAException if the request to the coordinator failed.
     * {@link GenericLRAException#getCause()} and/or {@link GenericLRAException#getStatusCode()}
     * may provide a more specific reason.
     */
    void leaveLRA(URL lraId, String body) throws GenericLRAException;

    /**
     * LRAs can be created with timeouts after which they are cancelled. Use this method to update the timeout.
     *
     * @param lraId the id of the lra to update
     * @param limit the new timeout period
     * @param unit the time unit for limit
     */
    void renewTimeLimit(URL lraId, long limit, TimeUnit unit);

    /**
     * checks whether there is an LRA associated with the calling thread
     *
     * @return the current LRA (can be null)
     */
    URL getCurrent();

    /**
     * Update the clients notion of the current coordinator.
     *
     * @param lraId the id of the LRA (can be null)
     */
    void setCurrentLRA(URL lraId);
}
