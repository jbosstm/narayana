/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package io.narayana.lra.proxy.logging;

import static org.jboss.logging.Logger.Level.ERROR;

import java.util.concurrent.ExecutionException;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * i18n log messages for the lra module.
 */
@MessageLogger(projectCode = "LRAPROXY")
public interface lraI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */
    @Message(id = 25001, value = "Participant '%s' serialization problem")
    @LogMessage(level = ERROR)
    void error_cannotSerializeParticipant(String participantToString, @Cause Throwable e);

    @Message(id = 25002, value = "Participant '%s' exception during completion")
    @LogMessage(level = ERROR)
    void error_participantExceptionOnCompletion(String name, @Cause ExecutionException e);

    @Message(id = 25003, value = "Cannot get status of participant '%s' of lra id '%s'")
    String error_gettingParticipantStatus(String participant, String lraId, @Cause Throwable e);


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