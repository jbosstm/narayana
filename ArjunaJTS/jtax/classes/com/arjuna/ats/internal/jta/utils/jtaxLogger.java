/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.utils;

import java.lang.invoke.MethodHandles;

import org.jboss.logging.Logger;

/**
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-01
 */
public class jtaxLogger
{
    public static final Logger logger = Logger.getLogger("com.arjuna.ats.jtax");
    public static final jtaxI18NLogger i18NLogger = Logger.getMessageLogger(MethodHandles.lookup(),
            jtaxI18NLogger.class, "com.arjuna.ats.jtax");
}