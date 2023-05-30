/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.logging;

import org.jboss.logging.Logger;

public final class LRALogger {
    private LRALogger() {
    }

    public static final Logger logger = Logger.getLogger("io.narayana.lra");
    public static final LraI18nLogger i18nLogger = Logger.getMessageLogger(LraI18nLogger.class, "io.narayana.lra");
}