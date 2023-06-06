/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.arjuna.ats.jta.logging;

import org.jboss.logging.Logger;

public class jtaLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.ats.jta");

    public static final jtaI18NLogger i18NLogger = Logger.getMessageLogger(jtaI18NLogger.class, "com.arjuna.ats.jta");

    private jtaLogger() {
    }
}