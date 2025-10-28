/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.logging;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.Logger;

public class wsasLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.mw.wsas");
    public static final wsasI18NLogger i18NLogger = Logger.getMessageLogger(MethodHandles.lookup(),
            wsasI18NLogger.class, "com.arjuna.mw.wsas");
}