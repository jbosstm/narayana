/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.logging;

import org.jboss.logging.Logger;

public class wscfLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.ws.wscf");
    public static final wscfI18NLogger i18NLogger = Logger.getMessageLogger(wscfI18NLogger.class, "com.arjuna.ws.wscf");
}