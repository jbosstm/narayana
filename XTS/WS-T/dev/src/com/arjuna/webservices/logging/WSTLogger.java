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

import java.util.Locale;
import java.util.ResourceBundle;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.util.logging.LogNoi18n;
import com.arjuna.common.util.logging.Logi18n;

public class WSTLogger
{
    public static LogNoi18n      arjLogger;
    public static Logi18n        arjLoggerI18N;
    public static ResourceBundle log_mesg;

    static
    {
        /** Ensure the properties are loaded before initialising the logger **/
        arjPropertyManager.init();
            
    	arjLogger = LogFactory.getLogNoi18n("com.arjuna.webservices.logging.WSTLogger");
    
        final String language = commonPropertyManager.getPropertyManager().getProperty("language","en");
        final String country  = commonPropertyManager.getPropertyManager().getProperty("country","US");
    
    	final Locale currentLocale = new Locale(language, country);
    	log_mesg = ResourceBundle.getBundle("wst_msg",currentLocale);
    	
    	arjLoggerI18N = LogFactory.getLogi18n("com.arjuna.webservices.logging.WSTLoggerI18N",
    					     "wst_msg_"+language+"_"+country);
    }
}
