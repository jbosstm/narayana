/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.logging;

import org.jboss.logging.Logger;

public class tsLogger
{
	public static final Logger logger = Logger.getLogger("com.arjuna.ats.arjuna");
    public static final arjunaI18NLogger i18NLogger = Logger.getMessageLogger(arjunaI18NLogger.class, "com.arjuna.ats.arjuna");
}