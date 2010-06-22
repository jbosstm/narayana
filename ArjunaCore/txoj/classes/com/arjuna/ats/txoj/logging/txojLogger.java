/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */

package com.arjuna.ats.txoj.logging;

import com.arjuna.common.util.logging.*;
import org.jboss.logging.Logger;

public class txojLogger
{
	public static LogNoi18n aitLogger;
    public static txojI18NLogger i18NLogger;

    public static void initialize(LogNoi18n noi18n, Logi18n i18n)
    {
        aitLogger = noi18n;
        i18NLogger = new txojI18NLoggerImpl(Logger.getLogger("com.arjuna.ats.txoj"));
    }

    static
    {
        LogFactory.initializeModuleLogger(txojLogger.class, "txoj_msg", "com.arjuna.ats.txoj");
    }
}
