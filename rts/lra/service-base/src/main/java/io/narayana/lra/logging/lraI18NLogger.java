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

package io.narayana.lra.logging;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.json.JsonObject;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * i18n log messages for the lra module.
 */
@MessageLogger(projectCode = "LRA")
public interface lraI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */
    @LogMessage(level = ERROR)
    @Message(id = 25001, value = "Can't construct URL from LRA id '%s'")
    void error_urlConstructionFromStringLraId(String lraId, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25002, value = "LRA created with an unexpected status code: %d, coordinator response '%s'")
    void error_lraCreationUnexpectedStatus(int status, String response);

    @LogMessage(level = ERROR)
    @Message(id = 25004, value = "Cannot create URL from coordinator response '%s'")
    void error_cannotCreateUrlFromLCoordinatorResponse(String response, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25005, value = "Error on contacting the LRA coordinator '%s'")
    void error_cannotContactLRACoordinator(URI coordinator, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25006, value = "LRA renewal ends with an unexpected status code: %d, coordinator response '%s'")
    void error_lraRenewalUnexpectedStatus(int status, String response);

    @LogMessage(level = ERROR)
    @Message(id = 25007, value = "Leaving LRA ends with an unexpected status code: %d, coordinator response '%s'")
    void error_lraLeaveUnexpectedStatus(int status, String response);

    @LogMessage(level = WARN)
    @Message(id = 25008, value = "JAX-RS @Suspended annotation is untested")
    void warn_suspendAnnotationIsUntested();

    @LogMessage(level = WARN)
    @Message(id = 25009, value = "LRA participant class '%s' with asynchronous temination but no @Status or @Forget annotations")
    void error_asyncTerminationBeanMissStatusAndForget(Class<?> clazz);

    @Message(id = 25010, value = "LRA finished with an unexpected status code: %d, coordinator response '%s'")
    String error_lraTerminationUnexpectedStatus(int status, String response);

    @LogMessage(level = ERROR)
    @Message(id = 25011, value = "Cannot access coordinator '%s' when getting status for LRA '%s'")
    void error_cannotAccessCoordinatorWhenGettingStatus(URI coordinator, URL lra, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25012, value = "LRA coordinator '%s' returned an invalid status code '%s' for LRA '%s'")
    void error_invalidStatusCode(URI coordinator, int status, URL lra);

    @LogMessage(level = ERROR)
    @Message(id = 25013, value = "LRA coordinator '%s' returned no content on #getStatus call for LRA '%s'")
    void error_noContentOnGetStatus(URI coordinator, URL lra);

    @LogMessage(level = ERROR)
    @Message(id = 25014, value = "LRA coordinator '%s' returned an invalid status for LRA '%s'")
    void error_invalidArgumentOnStatusFromCoordinator(URI coordinator, URL lra, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25015, value = "Too late to join with the LRA '%s', coordinator response: '%s'")
    void error_tooLateToJoin(URL lra, String response);

    @LogMessage(level = ERROR)
    @Message(id = 25016, value = "Failed enlisting to LRA '%s', coordinator '%s' responded with status '%s'")
    void error_failedToEnlist(URL lra, URI coordinator, int status);

    @LogMessage(level = ERROR)
    @Message(id = 25017, value = "Trying to aquire an in use connection for coordinator '%s'")
    void error_connectionInUse(URI coordinator);

    @LogMessage(level = INFO)
    @Message(id = 25018, value = "Error parsing json LRAStatus from JSON '%s'")
    void warn_failedParsingStatusFromJson(JsonObject json, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25019, value = "Invalid query format '%s' to get lra statuses")
    void error_invalidQueryForGettingLraStatuses(String query);

    @LogMessage(level = ERROR)
    @Message(id = 25020, value = "Invalid format of coordinator url, was '%s'")
    void error_invalidCoordinatorUrl(URL coordinatorUrl, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25022, value = "Error when converting String '%s' to URL")
    void error_invalidStringFormatOfUrl(String string, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25023, value = "Error when encoding LRA id URL '%s' to String")
    void error_invalidFormatToEncodeUrl(URL url, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 25024, value = "Invalid LRA id format to create LRA record from LRA id '%s', link URI '%s'")
    void error_invalidFormatToCreateLRARecord(String lraId, String linkURI);

    @LogMessage(level = ERROR)
    @Message(id = 25025, value = "Cannot get status of nested lra '%s' as outer one '%s' is still active")
    void error_cannotGetStatusOfNestedLra(String nestedLraId, URL lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25026, value = "Invalid recovery url '%s' to join lra '%s'")
    void error_invalidRecoveryUrlToJoinLRA(String recoveryUrl, URL lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25027, value = "Cannot found compensator url '%s' for lra '%s'")
    void error_cannotFoundCompensatorUrl(String recoveryUrl, String lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25028, value = "Invalid format of lra id '%s' to replace compensator '%s'")
    void error_invalidFormatOfLraIdReplacingCompensator(String recoveryUrl, String lraId, @Cause MalformedURLException e);

    @LogMessage(level = ERROR)
    @Message(id = 25029, value = "Invalid format of request uri '%s' for lra id '%s' to replace compensator '%s'")
    void error_invalidFormatOfRequestUri(URI uri, String recoveryUrl, String lraId, @Cause MalformedURLException e);

    @Message(id = 25030, value = "Could not recreate abstract record '%s'")
    @LogMessage(level = WARN)
    void warn_coordinatorNorecordfound(String recordType, @Cause Throwable t);

    @Message(id = 25031, value = "Cannot retrieve compensator status data '%s' of lra id '%s'")
    @LogMessage(level = WARN)
    void warn_cannotGetCompensatorStatusData(String data, URL lraId, @Cause Throwable t);

    @Message(id = 25032, value = "reason '%s': container request for method '%s': lra: '%s'")
    @LogMessage(level = WARN)
    void warn_lraFilterContainerRequest(String reason, String method, String lra);

    @Message(id = 25033, value = "trying to aquire an in use connection")
    @LogMessage(level = ERROR)
    void error_cannotAquireInUseConnection();

    @Message(id = 25034, value = "LRA participant completion for asynchronous method %s#%s should return %d and not %d")
    @LogMessage(level = WARN)
    void warn_lraParticipantqForAsync(String clazz, String method, int statusCorrect, int statusWrong);

    @LogMessage(level = ERROR)
    @Message(id = 25035, value = "Cannot get status of nested lra '%s' as outer one '%s' is still active")
    void error_cannotGetStatusOfNestedLraURI(String nestedLraId, URI lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25036, value = "Invalid recovery url '%s' to join lra '%s'")
    void error_invalidRecoveryUrlToJoinLRAURI(String recoveryUrl, URI lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25037, value = "Invalid format of lra id '%s' to replace compensator '%s'")
    void error_invalidFormatOfLraIdReplacingCompensatorURI(String recoveryUrl, String lraId, @Cause URISyntaxException e);

    @LogMessage(level = WARN)
    @Message(id = 25038, value = "LRA participant `%s` returned immediate state (Compensating/Completing) from CompletionStage. LRA id: %s")
    void warn_participantReturnsImmediateStateFromCompletionStage(String participantId, String lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25039, value = "Cannot process non JAX-RS LRA participant")
    void error_cannotProcessParticipant(@Cause ReflectiveOperationException e);

    @LogMessage(level = WARN)
    @Message(id = 25040, value = "CDI cannot be detected, non JAX-RS LRA participants will not be processed")
    void warn_nonJaxRsParticipantsNotAllowed();

    @LogMessage(level = ERROR)
    @Message(id = 25041, value = "Invalid format of LRA id to be converted to LRA coordinator url, was '%s'")
    void error_invalidLraIdFormatToConvertToCoordinatorUrl(String lraId, @Cause Throwable t);

    @LogMessage(level = INFO)
    @Message(id = 25042, value = "Failed enlisting to LRA '%s', coordinator '%s' responded with status '%d (%s)'. Returning '%d (%s)'.")
    void info_failedToEnlistingLRANotFound(URL lraId, URI coordinatorUri, int coordinatorStatusCode,
            String coordinatorStatusMsg, int returnStatusCode, String returnStatusMsg);

    @Message(id = 25043, value = "Could not %s LRA '%s': coordinator '%s' responded with status '%s'")
    String get_couldNotCompleteCompensateOnReturnedStatus(String actionName, URI lraId, URI coordinatorUri, String status);

    @LogMessage(level = ERROR)
    @Message(id = 25044, value = "Error when encoding parent LRA id URL '%s' to String")
    void error_invalidFormatToEncodeParentUri(URI parentUri, @Cause Throwable t);

    @Message(id = 25145, value = "Unable to process LRA annotations: %s'")
    String warn_LRAStatusInDoubt(String reason);

    @LogMessage(level = WARN)
    @Message(id = 25146, value = "Unable to remove the failed duclicate failed LRA record (Uid: '%s') " +
            "(which is already present in the failedLRA record location type: '%s'.) from LRA Record location: '%s'")
    void warn_UnableToRemoveDuplicateFailedLRARecord(String failedUid, String failedLRAType, String lraType);

    @LogMessage(level = WARN)
    @Message(id = 25147, value = "An exception was thrown while moving failed LRA record (Uid: '%s'). " +
            "Reason: '%s'")
    void warn_move_lra_record(String failedUid, String exceptionMessage);

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in numeric sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics

          Debug and trace don't get i18n. Everything else MUST be i18n.
          By convention methods with String return type have prefix get_,
            all others are log methods and have prefix <level>_
     */

}
