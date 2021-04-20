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
package io.narayana.sra.client;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public interface SRAClientAPI {

    /**
     *
     * @param parentSRA the parent SRA if this new SRA is nested
     * @param clientID Each client is expected to have a unique identity (which can be a URL). (optional)
     * @param timeout Specifies the maximum time in seconds that the SRA will exist for. If the SRA is
     *                terminated because of a timeout, the SRA URL is deleted. All further invocations
     *                on the URL will return 404. The invoker can assume this was equivalent to a compensate
     *                operation. (optional, default to 0)
     * @param unit
     * @return the id of the new SRA
     * @throws GenericSRAException
     */
    URL startSRA(URL parentSRA, String clientID, Long timeout, TimeUnit unit) throws GenericSRAException;

    /**
     * Attempt to cancel an SRA
     *
     * Trigger the compensation of the SRA. All compensators will be triggered by the coordinator
     * (ie the compensate message will be sent to each compensators). Upon termination, the URL is
     * implicitly deleted. The invoker cannot know for sure whether the lra completed or compensated
     * without enlisting a io.narayana.sra.
     *
     * @param lraId The unique identifier of the SRA (required)
     * @return compensator sepcific data provided by the application
     *         for nested SRA the API implementation will combine compensator data into a
     *         JSON encoded array. This means that compensators MUST not return any data
     *         that starts with the JSON array start token (ie a '[' character)
     * @throws GenericSRAException Comms error
     */
    String cancelSRA(URL lraId) throws GenericSRAException;

    /**
     * Attempt to close an SRA
     *
     * Trigger the successful completion of the SRA. All compensators will be dropped by the coordinator.
     * The complete message will be sent to the compensators. Upon termination, the URL is implicitly
     * deleted. The invoker cannot know for sure whether the lra completed or compensated without enlisting
     * a io.narayana.sra.
     *
     * @param lraId The unique identifier of the SRA (required)
     * @return compensator sepcific data provided by the application
     *         for nested SRA the API implementation will combine compensator data into a
     *         JSON encoded array. This means that compensators MUST not return any data
     *         that starts with the JSON array start token (ie a '[' character)
     * @throws GenericSRAException Comms error
     */
    String commitSRA(URL lraId) throws GenericSRAException;

    /**
     * Lookup active SRAs
     *
     * @throws GenericSRAException Comms error
     */
    List<SRAInfo> getActiveSRAs() throws GenericSRAException;

    /**
     * Returns all SRAs
     *
     * Gets both active and recovering SRAs
     *
     * @return List<SRA>
     * @throws GenericSRAException Comms error
     */
    List<SRAInfo> getAllSRAs() throws GenericSRAException;

    /**
     * List recovering Long Running Actions
     *
     * Returns SRAs that are recovering (ie some compensators still need to be ran)
     *
     * @throws GenericSRAException Comms error
     */
    List<SRAInfo> getRecoveringSRAs() throws GenericSRAException;

    /**
     * Indicates whether an SRA is active
     *
     * @param lraId The unique identifier of the SRA (required)
     * @throws GenericSRAException Comms error
     */
    Boolean isActiveSRA(URL lraId) throws GenericSRAException;

    /**
     * Indicates whether an SRA was compensated
     *
     * @param lraId The unique identifier of the SRA (required)
     * @throws GenericSRAException Comms error
     */
    Boolean isCompensatedSRA(URL lraId) throws GenericSRAException;

    /**
     * Indicates whether an SRA is complete
     *
     * @param lraId The unique identifier of the SRA (required)
     * @throws GenericSRAException Comms error
     */
    Boolean isCompletedSRA(URL lraId) throws GenericSRAException;

    /**
     * A SRAParticipant can join with the SRA at any time prior to the completion of an activity
     *
     * @param lraId   The unique identifier of the SRA (required)
     * @param timelimit The time limit (in seconds) that the SRAParticipant can guarantee that it
     *                can compensate the work performed by the io.narayana.sra.demo.service. After this time period has elapsed,
     *                it may no longer be possible to undo the work within the scope of this (or any enclosing)
     *                SRA. It may therefore be necessary for the application or io.narayana.sra.demo.service to start other
     *                activities to explicitly try to compensate this work. The application or coordinator may
     *                use this information to control the lifecycle of a SRA. (required)
     * @param body    The resource path that the SRA coordinator will use to drive the compensator.
     *                Performing a GET on the compensator URL will return the current status of the compensator,
     *                or 404 if the compensator is no longer present.  The following types must be returned by
     *                Compensators to indicate their current status:
     *                -  Compensating: the SRAParticipant is currently compensating for the SRA;
     *                -  Compensated: the SRAParticipant has successfully compensated for
     *                the SRA.
     *                -  FailedToCompensate: the SRAParticipant was not able to compensate for the SRA.
     *                   It must maintain information about the work it was to compensate until the
     *                   coordinator sends it a forget message.
     *                -  Completing: the SRAParticipant is tidying up after being told to complete.
     *                -  Completed: the coordinator/io.narayana.sra has confirmed.
     *                -  FailedToComplete: the SRAParticipant was unable to tidy-up.
     *                     Performing a POST on URL/compensate will cause the compensator to compensate
     *                     the work that was done within the scope of the SRA.
     *                     Performing a POST on URL/complete will cause the compensator to tidy up and
     *                  it can forget this SRA.  (optional)
     * @throws GenericSRAException Comms error
     */
    void joinSRA(URL lraId, Long timelimit, String body) throws GenericSRAException;

    /**
     * Similar to {@link SRAClientAPI#joinSRA(URL, Long, String)} but the various compensator urls
     * are passed in explicitly
     *
     * @param lraId The unique identifier of the SRA (required)
     * @param timelimit The time limit (in seconds) that the SRAParticipant can guarantee that it
     *                can compensate the work performed by the io.narayana.sra.demo.service
     * @param compensateUrl Performing a POST onthis URL will cause the io.narayana.sra to compensate the work that
     *                      was done within the scope of the SRA.
     * @param completeUrl Performing a POST on this URL  will cause the io.narayana.sra to tidy up and it can forget this transaction.
     * @param leaveUrl Performing a PUT on this URL with cause the compensator to leave the SRA
     * @param statusUrl Performing a GET on this URL will return the status of the compensator {@see joinSRA}
     * @throws GenericSRAException
     */
    String joinSRA(URL lraId, Long timelimit, String compensateUrl, String completeUrl, String leaveUrl, String statusUrl) throws GenericSRAException;

    /**
     * checks whether there is an SRA associated with the calling thread
     * @return
     */
    URL getCurrent();

    List<String> getResponseData(URL lraId);
}
