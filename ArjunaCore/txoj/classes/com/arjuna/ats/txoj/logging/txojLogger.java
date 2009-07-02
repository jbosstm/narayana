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

import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.ats.txoj.common.txojPropertyManager;

import java.util.*;

public class txojLogger
{

	public static LogNoi18n aitLogger;
	public static Logi18n aitLoggerI18N;
	public static ResourceBundle log_mesg;

	private static String language;
	private static String country;
	private static Locale currentLocale;

	static
	{
		/** Ensure the properties are loaded before initialising the logger * */
		txojPropertyManager.getPropertyManager();

		aitLogger = LogFactory.getLogNoi18n("com.arjuna.ats.txoj.logging.txojLogger");

		language = commonPropertyManager.getPropertyManager().getProperty("language", "en");

		country = commonPropertyManager.getPropertyManager().getProperty("country", "US");

		currentLocale = new Locale(language, country);
		
		try
		{
			log_mesg = ResourceBundle.getBundle("txoj_msg", currentLocale);
		}
		catch (Throwable ex)
		{
			log_mesg = null;
		}

		if (log_mesg == null)
		{
			currentLocale = new Locale("en", "US");
			
			log_mesg = ResourceBundle.getBundle("txoj_msg", currentLocale);
		}
		
		try
		{
			aitLoggerI18N = LogFactory.getLogi18n("com.arjuna.ats.txoj.logging.txojLoggerI18N", "txoj_msg_"
					+ language + "_" + country);
		}
		catch (Throwable ex)
		{
			aitLoggerI18N = null;
		}
		
		if (aitLoggerI18N == null)
			aitLoggerI18N = LogFactory.getLogi18n("com.arjuna.ats.txoj.logging.txojLoggerI18N", "txoj_msg_en_US");
	}

}
