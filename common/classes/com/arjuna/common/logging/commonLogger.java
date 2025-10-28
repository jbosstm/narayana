/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.common.logging;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.Logger;

public class commonLogger {
	public static final Logger logger = Logger.getLogger("com.arjuna.ats.common");
    public static final commonI18NLogger i18NLogger = Logger.getMessageLogger(MethodHandles.lookup(),
            commonI18NLogger.class,
            "com.arjuna.ats.common");
}