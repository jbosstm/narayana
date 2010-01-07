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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: tsmxLogger.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.tsmx.logging;

import com.arjuna.common.util.logging.*;

import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import java.util.Locale;
import java.util.ResourceBundle;

public class tsmxLogger
{

	public static LogNoi18n logger;
	public static Logi18n loggerI18N;

	private static ResourceBundle log_mesg;

	private static String language;
	private static String country;
	private static Locale currentLocale;

	static
	{
		logger = LogFactory.getLogNoi18n("com.arjuna.ats.tsmx.logging.tsmxLogger");

		language = commonPropertyManager.getLoggingEnvironmentBean().getLanguage();
		country = commonPropertyManager.getLoggingEnvironmentBean().getCountry();

		currentLocale = new Locale(language, country);
		
		try
		{
			log_mesg = ResourceBundle.getBundle("arjuna_msg", currentLocale);
		}
		catch (Throwable ex)
		{
			log_mesg = null;
		}

		if (log_mesg == null)
		{
			currentLocale = new Locale("en", "US");
			
			log_mesg = ResourceBundle.getBundle("arjuna_msg", currentLocale);
		}
		
		try
		{
			loggerI18N = LogFactory.getLogi18n("com.arjuna.ats.tsmx.logging.tsmxLoggerI18N", "tsmx_msg_"
					+ language + "_" + country);
		}
		catch (Throwable ex)
		{
			loggerI18N = null;
		}
		
		if (loggerI18N == null)
			loggerI18N = LogFactory.getLogi18n("com.arjuna.ats.tsmx.logging.tsmxLoggerI18N", "tsmx_msg_en_US");
	}

}
