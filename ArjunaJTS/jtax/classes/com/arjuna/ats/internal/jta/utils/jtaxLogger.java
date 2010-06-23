/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.jta.utils;

import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.util.logging.LogNoi18n;
import com.arjuna.common.util.logging.Logi18n;
import org.jboss.logging.Logger;

/**
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-01
 */
public class jtaxLogger
{
    public static LogNoi18n logger;
    public static jtaxI18NLogger i18NLogger;

    public static void initialize(LogNoi18n noi18n, Logi18n i18n)
    {
        logger = noi18n;
        i18NLogger = new jtaxI18NLoggerImpl(Logger.getLogger("com.arjuna.ats.jtax"));
    }

    static
    {
        LogFactory.initializeModuleLogger(jtaxLogger.class, "jtax_msg", "com.arjuna.ats.jtax");
    }
}
