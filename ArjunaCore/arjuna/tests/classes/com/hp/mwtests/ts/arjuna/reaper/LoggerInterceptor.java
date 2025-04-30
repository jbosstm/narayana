/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.arjuna.reaper;

import com.arjuna.ats.arjuna.logging.ArjunaLoggerInterceptor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

public class LoggerInterceptor implements ArjunaLoggerInterceptor
{
    record LogMessage(Instant instant, String message) {}
    private final Collection<LogMessage> errorMessages = new ArrayList<>();
    private final Collection<LogMessage> warnMessages = new ArrayList<>();
    private final Collection<LogMessage> infoMessages = new ArrayList<>();

    public synchronized boolean error(Instant instant, String message) {
        errorMessages.add(new LogMessage(instant, message));
        return true; // pass the message on to the next java.util.logging.Filter
    }

    public synchronized boolean warn(Instant instant, String message) {
        warnMessages.add(new LogMessage(instant, message));
        return false; // don't pass it on so it should not appear in the logs
    }

    public synchronized boolean info(Instant instant, String message) {
        infoMessages.add(new LogMessage(instant, message));
        return false; // don't pass it on so it should not appear in the logs
    }

    public Collection<LogMessage> getErrorMessages() {
        return errorMessages;
    }

    public Collection<LogMessage> getWarnMessages() {
        return warnMessages;
    }

    public Collection<LogMessage> getInfoMessages() {
        return infoMessages;
    }
}
