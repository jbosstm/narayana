/*
 * SPDX short identifier: Apache-2.0
 */



package com.arjuna.ats.jbossatx.logging;

import org.jboss.logging.Logger;

public class jbossatxLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.ats.jbossatx");
    public static final jbossatxI18NLogger i18NLogger = Logger.getMessageLogger(jbossatxI18NLogger.class, "com.arjuna.ats.jbossatx");
}