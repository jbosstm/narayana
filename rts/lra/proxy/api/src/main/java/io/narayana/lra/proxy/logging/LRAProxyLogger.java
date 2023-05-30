/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.proxy.logging;

import org.jboss.logging.Logger;

public final class LRAProxyLogger {
    private LRAProxyLogger() {
    }

    public static final Logger logger = Logger.getLogger("io.narayana.lra.proxy");
    public static final lraI18NLogger i18NLogger = Logger.getMessageLogger(lraI18NLogger.class, "io.narayana.lra.proxy");
}