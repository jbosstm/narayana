/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jdbc.logging;

import org.jboss.logging.Logger;

public class jdbcLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.ats.jdbc");
    public static final jdbcI18NLogger i18NLogger = Logger.getMessageLogger(jdbcI18NLogger.class, "com.arjuna.ats.jdbc");
}