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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 * 
 * $Id: LoggerSetup.java 2342 2006-03-30 13:06:17Z  $
 */
package com.arjuna.ats.internal.jta.utils;

import com.arjuna.ats.jta.logging.jtaLogger;

import java.util.Locale;
import java.util.ResourceBundle;

public class LoggerSetup
{
	private static boolean _initialised = false;

	public final static void setup()
	{
		if ( !_initialised )
		{
			String language = System.getProperty("language","en");
			String country = System.getProperty("country","US");

			jtaLogger.loggerI18N.addResourceBundle("jtax_msg_"+language+"_"+country);

			_initialised = true;
		}
	}
}
