/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.ats.jts.logging;

import org.jboss.logging.Logger;

public class jtsLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.ats.jts");
    public static final jtsI18NLogger i18NLogger = Logger.getMessageLogger(jtsI18NLogger.class, "com.arjuna.ats.jts");
}