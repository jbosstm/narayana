/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.xts.recovery.logging;

import org.jboss.logging.Logger;

public class RecoveryLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.wsrecovery");
    public static final recoveryI18NLogger i18NLogger  = Logger.getMessageLogger(recoveryI18NLogger.class, "com.arjuna.wsrecovery");
}