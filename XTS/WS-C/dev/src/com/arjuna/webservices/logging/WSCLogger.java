/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: WSCLogger.java,v 1.1.2.1 2005/11/22 10:32:48 kconner Exp $
 */

package com.arjuna.webservices.logging;

import java.util.Locale;
import java.util.ResourceBundle;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.common.util.logging.LogFactory;
import com.arjuna.common.util.logging.LogNoi18n;
import com.arjuna.common.util.logging.Logi18n;

/**
 * WS-C logger instances.
 */
public class WSCLogger
{
    /**
     * The non I18N logger.
     */
    public static LogNoi18n      arjLogger;
    /**
     * The I18N logger.
     */
    public static Logi18n        arjLoggerI18N;
    /**
     * The message bundle.
     */
    public static ResourceBundle log_mesg;

    static
    {
        /** Ensure the properties are loaded before initialising the logger **/
        arjPropertyManager.getPropertyManager();
            
    	arjLogger = LogFactory.getLogNoi18n("com.arjuna.wsc.logging.WSCLogger");
    
        final String language = commonPropertyManager.propertyManager.getProperty("language","en");
        final String country  = commonPropertyManager.propertyManager.getProperty("country","US");
    
    	final Locale currentLocale = new Locale(language, country);
    	log_mesg = ResourceBundle.getBundle("wsc_msg",currentLocale);
    	
    	arjLoggerI18N = LogFactory.getLogi18n("com.arjuna.wsc.logging.WSCLoggerI18N",
    					     "wsc_msg_"+language+"_"+country);
    }
}
