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

import com.arjuna.common.internal.util.logging.commonPropertyManager;
import com.arjuna.orbportability.common.opPropertyManager;

import java.util.Locale;
import java.util.ResourceBundle;

public class opLogger
{

	public static LogNoi18n logger;
	public static Logi18n loggerI18N;

	private static ResourceBundle logMesg;

	private static String _language;
	private static String _country;
	private static Locale _currentLocale;

	static
	{
		logger = LogFactory.getLogNoi18n("com.arjuna.orbportability.logging.logger");

		_language = commonPropertyManager.getLoggingEnvironmentBean().getLanguage();

		_country = commonPropertyManager.getLoggingEnvironmentBean().getCountry();

		_currentLocale = new Locale(_language, _country);
		
		try
		{
			logMesg = ResourceBundle.getBundle("orbportability_msg", _currentLocale);
		}
		catch (Throwable ex)
		{
			logMesg = null;
		}
		
		if (logMesg == null)
		{
			_currentLocale = new Locale("en", "US");
			
			logMesg = ResourceBundle.getBundle("orbportability_msg", _currentLocale);
		}

		try
		{
			loggerI18N = LogFactory.getLogi18n("com.arjuna.orbportability.logging.loggerI18N", "orbportability_msg_"
					+ _language + "_" + _country);
		}
		catch (Throwable ex)
		{
			loggerI18N = null;
		}
		
		if (loggerI18N == null)
			loggerI18N = LogFactory.getLogi18n("com.arjuna.orbportability.logging.loggerI18N", "orbportability_msg_en_US");
	}

}
