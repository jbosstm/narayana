/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.txoj.logging;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.Logger;

public class txojLogger
{
	public static final Logger logger = Logger.getLogger("com.arjuna.ats.txoj");
    public static final txojI18NLogger i18NLogger = Logger.getMessageLogger(MethodHandles.lookup(),
            txojI18NLogger.class, "com.arjuna.ats.txoj");
}