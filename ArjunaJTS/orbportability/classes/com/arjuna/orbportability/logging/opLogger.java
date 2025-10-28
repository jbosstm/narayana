/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.orbportability.logging;

import org.jboss.logging.Logger;

public class opLogger
{
	public static final Logger logger = Logger.getLogger("com.arjuna.orbportability");
    public static final orbportabilityI18NLogger i18NLogger = Logger.getMessageLogger(orbportabilityI18NLogger.class, "com.arjuna.orbportability");
}