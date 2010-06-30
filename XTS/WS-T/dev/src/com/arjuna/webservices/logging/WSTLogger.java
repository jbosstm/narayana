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
/*
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: WSTLogger.java,v 1.1.2.1 2005/11/22 10:35:00 kconner Exp $
 */

package com.arjuna.webservices.logging;

import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.util.logging.LogNoi18n;
import com.arjuna.common.util.logging.Logi18n;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;

public class WSTLogger
{
    public static LogNoi18n      arjLogger;

    public static BasicLogger logger;
    public static wstI18NLogger i18NLogger;

    //public static Logi18n        arjLoggerI18N;

    public static void initialize(LogNoi18n noi18n, Logi18n i18n)
    {
        arjLogger = noi18n;
        //arjLoggerI18N = i18n;
        logger = Logger.getLogger("com.arjuna.wst");
        i18NLogger = new wstI18NLoggerImpl((Logger)logger);
    }

    static
    {
        LogFactory.initializeModuleLogger(WSTLogger.class, "wst_msg", "com.arjuna.wst");
    }
}
