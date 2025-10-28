/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.logging;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.Logger;

public class jtaLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.ats.jta");

    public static final jtaI18NLogger i18NLogger = Logger.getMessageLogger(MethodHandles.lookup(), jtaI18NLogger.class,
            "com.arjuna.ats.jta");

    private jtaLogger() {
    }
}