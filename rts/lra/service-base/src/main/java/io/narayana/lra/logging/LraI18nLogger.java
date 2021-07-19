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
import static org.jboss.logging.Logger.Level.WARN;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * i18n log messages for the lra module.
 */
@MessageLogger(projectCode = "LRA")
public interface LraI18nLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */
    @Message(id = 25001, value = "LRA created with an unexpected status code: %d, coordinator response '%s'")
    String error_lraCreationUnexpectedStatus(int status, String response);

    @Message(id = 25002, value = "Leaving LRA: %s, ends with an unexpected status code: %d, coordinator response '%s'")
    String error_lraLeaveUnexpectedStatus(URI lra, int status, String response);

    @Message(id = 25003, value = "LRA participant class '%s' with asynchronous temination but no @Status or @Forget annotations")
    String error_asyncTerminationBeanMissStatusAndForget(Class<?> clazz);

    @Message(id = 25004, value = "LRA finished with an unexpected status code: %d, coordinator response '%s'")
    String error_lraTerminationUnexpectedStatus(int status, String response);

    @Message(id = 25005, value = "LRA coordinator '%s' returned an invalid status code '%s' for LRA '%s'")
    String error_invalidStatusCode(URI coordinator, int status, URL lra);

    @Message(id = 25006, value = "LRA coordinator '%s' returned no content on #getStatus call for LRA '%s'")
    String error_noContentOnGetStatus(URI coordinator, URL lra);

    @Message(id = 25007, value = "LRA coordinator '%s' returned an invalid status for LRA '%s'")
    String error_invalidArgumentOnStatusFromCoordinator(URI coordinator, URL lra, @Cause Throwable t);

    @Message(id = 25008, value = "Too late to join with the LRA '%s', coordinator response: '%s'")
    String error_tooLateToJoin(URL lra, String response);

    @Message(id = 25009, value = "Failed enlisting to LRA '%s', coordinator '%s' responded with status '%s'")
    String error_failedToEnlist(URL lra, URI coordinator, int status);

    @Message(id = 25010, value = "Error when converting String '%s' to URL")
    String error_invalidStringFormatOfUrl(String string, @Cause Throwable t);

    @Message(id = 25011, value = "Invalid LRA id format to create LRA record from LRA id '%s', link URI '%s'")
    String error_invalidFormatToCreateLRARecord(String lraId, String linkURI);

    @LogMessage(level = ERROR)
    @Message(id = 25012, value = "Cannot found compensator url '%s' for lra '%s'")
    void error_cannotFoundCompensatorUrl(String recoveryUrl, String lraId);

    @Message(id = 25013, value = "Could not recreate abstract record '%s'")
    @LogMessage(level = WARN)
    void warn_coordinatorNorecordfound(String recordType, @Cause Throwable t);

    @Message(id = 25014, value = "reason '%s': container request for method '%s': lra: '%s'")
    @LogMessage(level = WARN)
    void warn_lraFilterContainerRequest(String reason, String method, String lra);

    @Message(id = 25015, value = "LRA participant completion for asynchronous method %s#%s should return %d and not %d")
    @LogMessage(level = WARN)
    void warn_lraParticipantqForAsync(String clazz, String method, int statusCorrect, int statusWrong);

    @Message(id = 25016, value = "Cannot get status of nested lra '%s' as outer one '%s' is still active")
    String error_cannotGetStatusOfNestedLraURI(String nestedLraId, URI lraId);

    @Message(id = 25017, value = "Invalid recovery url '%s' to join lra '%s'")
    String error_invalidRecoveryUrlToJoinLRAURI(String recoveryUrl, URI lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25018, value = "Invalid format of lra id '%s' to replace compensator '%s'")
    void error_invalidFormatOfLraIdReplacingCompensatorURI(String recoveryUrl, String lraId, @Cause URISyntaxException e);

    @LogMessage(level = WARN)
    @Message(id = 25019, value = "LRA participant `%s` returned immediate state (Compensating/Completing) from CompletionStage. LRA id: %s")
    void warn_participantReturnsImmediateStateFromCompletionStage(String participantId, String lraId);

    @LogMessage(level = ERROR)
    @Message(id = 25020, value = "Cannot process non JAX-RS LRA participant")
    void error_cannotProcessParticipant(@Cause ReflectiveOperationException e);

    @Message(id = 25021, value = "Invalid format of LRA id to be converted to LRA coordinator url, was '%s'")
    String error_invalidLraIdFormatToConvertToCoordinatorUrl(String lraId, @Cause Throwable t);

    @Message(id = 25022, value = "Failed enlisting to LRA '%s', coordinator '%s' responded with status '%d (%s)'. Returning '%d (%s)'.")
    String info_failedToEnlistingLRANotFound(URL lraId, URI coordinatorUri, int coordinatorStatusCode,
            String coordinatorStatusMsg, int returnStatusCode, String returnStatusMsg);

    @Message(id = 25023, value = "Could not %s LRA '%s': coordinator '%s' responded with status '%s'")
    String get_couldNotCompleteCompensateOnReturnedStatus(String actionName, URI lraId, URI coordinatorUri, String status);

    @Message(id = 25024, value = "Error when encoding parent LRA id URL '%s' to String")
    String error_invalidFormatToEncodeParentUri(URI parentUri, @Cause Throwable t);

    @Message(id = 25025, value = "Unable to process LRA annotations: %s'")
    String warn_LRAStatusInDoubt(String reason);

    @LogMessage(level = WARN)
    @Message(id = 25026, value = "Unable to remove the failed duclicate failed LRA record (Uid: '%s') " +
            "(which is already present in the failedLRA record location type: '%s'.) from LRA Record location: '%s'")
    void warn_UnableToRemoveDuplicateFailedLRARecord(String failedUid, String failedLRAType, String lraType);

    @LogMessage(level = WARN)
    @Message(id = 25027, value = "An exception was thrown while moving failed LRA record (Uid: '%s'). " +
            "Reason: '%s'")
    void warn_move_lra_record(String failedUid, String exceptionMessage);

    @Message(id = 25028, value = "Demanded API version '%s' is not in the list of the supported versions '%s'. " +
            "Please, provide the right version for the API.")
    String get_wrongAPIVersionDemanded(String demandedApiVersion, String supportedVersions);

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
