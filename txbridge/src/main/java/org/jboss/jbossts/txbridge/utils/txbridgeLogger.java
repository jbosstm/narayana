/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.utils;

import org.jboss.logging.Logger;

/**
 * Module logger for the txbridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-07
 */
public class txbridgeLogger
{
    public static final Logger logger = Logger.getLogger("org.jboss.jbossts.txbridge");
    public static final txbridgeI18NLogger i18NLogger = Logger.getMessageLogger(txbridgeI18NLogger.class, "org.jboss.jbossts.txbridge");
}