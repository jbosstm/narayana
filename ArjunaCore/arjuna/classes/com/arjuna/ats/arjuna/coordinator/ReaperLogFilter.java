/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.logging.ArjunaLoggerInterceptor;
import org.jboss.logmanager.Level;

import java.util.logging.Filter;
import java.util.logging.LogRecord;

record ReaperLogFilter(ArjunaLoggerInterceptor interceptor) implements Filter {

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record.getLevel() == Level.INFO) {
            return interceptor.info(record.getInstant(), record.getMessage());
        } else if (record.getLevel() == Level.WARN) {
            return interceptor.warn(record.getInstant(), record.getMessage());
        } else if (record.getLevel() == Level.ERROR) {
            return interceptor.error(record.getInstant(), record.getMessage());
        }

        return true;
    }
}
