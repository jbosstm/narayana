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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: tsLogger.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.logging;

import com.arjuna.common.util.logging.*;

import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.*;

public class tsLogger
{

	public static LogNoi18n arjLogger;
	public static Logi18n arjLoggerI18N;
	public static ResourceBundle log_mesg;
	
	private static String language;
	private static String country;
	private static Locale currentLocale;
	private static String dirLocale;

	static
	{
		/** Ensure the properties are loaded before initialising the logger * */
		arjPropertyManager.getPropertyManager();

		arjLogger = LogFactory.getLogNoi18n("com.arjuna.ats.arjuna.logging.arjLogger");

		language = commonPropertyManager.propertyManager.getProperty("language", "en");
		country = commonPropertyManager.propertyManager.getProperty("country", "US");

		currentLocale = new Locale(language, country);
		
		try
		{
			log_mesg = ResourceBundle.getBundle("arjuna_msg", currentLocale);
		}
		catch (Throwable ex)
		{
			log_mesg = null;
		}

		// the default
		
		if (log_mesg == null)
		{
			currentLocale = new Locale("en", "US");

			log_mesg = ResourceBundle.getBundle("arjuna_msg", currentLocale);
		}
		
		try
		{
			arjLoggerI18N = LogFactory.getLogi18n("com.arjuna.ats.arjuna.logging.arjLoggerI18N", "arjuna_msg_"
					+ language + "_" + country);
		}
		catch (Throwable ex)
		{
			arjLoggerI18N = null;
		}

		// the default
		
		if (arjLoggerI18N == null)
			arjLoggerI18N = LogFactory.getLogi18n("com.arjuna.ats.arjuna.logging.arjLoggerI18N", "arjuna_msg_en_US");
	}

}
