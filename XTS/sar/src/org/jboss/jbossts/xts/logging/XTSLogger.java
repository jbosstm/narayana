/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.xts.logging;
import org.jboss.logging.Logger;

/**
 * Class used to do logging in the XTS Service code
 */
public class XTSLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.xtsservice");
    public static final xtsI18NLogger i18NLogger  = Logger.getMessageLogger(xtsI18NLogger.class, "com.arjuna.xtsservice");
}