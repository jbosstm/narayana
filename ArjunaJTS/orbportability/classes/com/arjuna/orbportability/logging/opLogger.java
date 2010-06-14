/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */

package com.arjuna.orbportability.logging;

import com.arjuna.common.util.logging.*;

public class opLogger
{
	public static LogNoi18n logger;
	public static Logi18n loggerI18N;
    public static orbportabilityI18NLogger i18NLogger;

    public static void initialize(LogNoi18n noi18n, Logi18n i18n)
    {
        logger = noi18n;
        loggerI18N = i18n;
        i18NLogger = new orbportabilityI18NLoggerImpl(i18n);
    }

    static
    {
        LogFactory.initializeModuleLogger(opLogger.class, "orbportability_msg", "com.arjuna.orbportability");
    }
}
