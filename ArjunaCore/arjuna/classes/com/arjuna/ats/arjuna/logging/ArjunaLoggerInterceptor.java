/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.logging;

import java.time.Instant;

public interface ArjunaLoggerInterceptor
{
    /**
     * notification that an error is about to be logged
     * <p>
     * @param instant the instant at which the message was logged
     * @param message the text of the message
     * @return true if the message should be passed on to the next logger
     */
    boolean error(Instant instant, String message);

    /**
     * notification that a warning is about to be logged
     * <p>
     * @param instant the instant at which the message was logged
     * @param message the text of the message
     * @return true if the message should be passed on to the next logger
     */
    boolean warn(Instant instant, String message);

    /**
     * notification that an info message about to be logged
     * <p>
     * @param instant the instant at which the message was logged
     * @param message the text of the message
     * @return true if the message should be passed on to the next logger
     */
    boolean info(Instant instant, String message);

    /**
     * Given a message return, if present, its Arjuna message id
     * @see arjunaI18NLogger
     * <p>
     * @param message the message whose id should be returned
     * @return the Arjuna message id or null if it does not correspond to a localised message
     */
    default String getMessageId(String message) {
        if (message.contains(":")) { // use contains to avoid issues with character sets
            return message.split(":")[0]; // the first word is the arjuna message id;
        }

        return null;
    }
}
