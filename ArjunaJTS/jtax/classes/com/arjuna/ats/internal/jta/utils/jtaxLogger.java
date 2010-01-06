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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2010-01
 */
public class jtaxLogger
{

    public static LogNoi18n logger;
    public static Logi18n loggerI18N;

    private static ResourceBundle logMesg;

    private static String _language;
    private static String _country;
    private static Locale _currentLocale;

    static
    {
        logger = LogFactory.getLogNoi18n("com.arjuna.ats.jtax.logging.logger");

        _language = System.getProperty("language","en");
        _country = System.getProperty("country","US");

        _currentLocale = new Locale(_language, _country);

		try
		{
			logMesg = ResourceBundle.getBundle("jtax_msg", _currentLocale);
		}
		catch (Throwable ex)
		{
			logMesg = null;
		}

		if (logMesg == null)
		{
			_currentLocale = new Locale("en", "US");

			logMesg = ResourceBundle.getBundle("jtax_msg", _currentLocale);
		}

		try
		{
			loggerI18N = LogFactory.getLogi18n("com.arjuna.ats.jtax.logging.loggerI18N", "jtax_msg_"+_language+"_"+_country);
		}
		catch (Throwable ex)
		{
			loggerI18N = null;
		}

		if (loggerI18N == null)
			loggerI18N = LogFactory.getLogi18n("com.arjuna.ats.jtax.logging.loggerI18N", "jtax_msg_en_US");
    }
}
