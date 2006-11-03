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
 * Copyright (C) 1998 - 2004.
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Utility.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.common.internal.util.licence.utils;

import java.net.InetAddress;

import java.lang.NumberFormatException;
import java.net.UnknownHostException;

/**
 * Various useful functions that we wrap in a single class.
 * Some of these functions are needed simply for backwards
 * compatibility with older versions of Java.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Utility.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Utility
{

	private static int     myAddr = 0;
	private static final String hexStart = "0x";

	/**
	 * Convert integer to hex String.
	 */
	public static String intToHexString (int number)
	    throws NumberFormatException
	{
		return Integer.toString(number, 16);
	}

	/**
	 * Convert a hex String to an integer.
	 *
	 * Be careful of -1. Java IO is really bad!
	 */
	public static int hexStringToInt (String s)
	    throws NumberFormatException
	{
		boolean isNeg;
		String toUse = s;
	
		if (s.startsWith(Utility.hexStart))
			toUse = s.substring(Utility.hexStart.length());

		String lastString = toUse.substring(toUse.length()-1);

		if (toUse.substring(0, 1).equals("-")) {
			toUse = "-0" + toUse.substring(1, toUse.length() - 1);
			isNeg = true;
		} else {
			toUse = "0" + toUse.substring(0, toUse.length() - 1);
			isNeg = false;
		}

		Integer i = Integer.valueOf(toUse, 16);
		int val = i.intValue();
	
		val = val << 4;
	
		if (isNeg) {
			val -= Integer.valueOf(lastString, 16).intValue();
		} else {
			val += Integer.valueOf(lastString, 16).intValue();
		}

		return val;
	}

	/**
	 * Convert a long to a hex String.
	 */
	public static String longToHexString (long number)
	    throws NumberFormatException
	{
		return Long.toString(number, 16);
	}

	/**
	 * Convert a hex String to a long.
	 */
	public static long hexStringToLong (String s)
	    throws NumberFormatException
	{
		boolean isNeg;
		String toUse = s;
	
		if (s.startsWith(Utility.hexStart))
			toUse = s.substring(Utility.hexStart.length());

		String lastString = toUse.substring(toUse.length()-1);

		if (toUse.substring(0, 1).equals("-")) {
			toUse = "-0" + toUse.substring(1, toUse.length() - 1);
			isNeg = true;
		} else {
			toUse = "0" + toUse.substring(0, toUse.length() - 1);
			isNeg = false;
		}

		Long i = Long.valueOf(toUse, 16);
		long val = i.longValue();
	
		val = val << 4;
	
		if (isNeg) {
			val -= Long.valueOf(lastString, 16).longValue();
		} else {
			val += Long.valueOf(lastString, 16).longValue();
		}

		return val;
	}    

	public static synchronized int hostInetAddr ()
	    throws UnknownHostException
	{
		/*
		 * Calculate only once.
		 */

		if (myAddr == 0) {
			InetAddress addr = InetAddress.getLocalHost();
			byte[] b = addr.getAddress();
	
			for (int i = 0; i < b.length; i++) {
				/*
				 * Convert signed byte into unsigned.
				 */
		
				int l = 0x7f & b[i];

				l += (0x80 & b[i]);
				myAddr = (myAddr << 8) | l;
			}
		}
		return myAddr;
	}
}
