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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Utility.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.utils;

import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

import java.net.InetAddress;

import java.net.UnknownHostException;
import java.lang.NumberFormatException;

/**
 * Various useful functions that we wrap in a single class. Some of these
 * functions are needed simply for backwards compatibility with older versions
 * of Java.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Utility.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class Utility
{

    /**
     * Convert integer to hex String.
     */

    public static String intToHexString (int number)
            throws NumberFormatException
    {
        return Integer.toString(number, 16);
    }

    /**
     * Convert a hex String to an integer. Be careful of -1. Java IO is really
     * bad!
     */

    public static int hexStringToInt (String s) throws NumberFormatException
    {
        boolean isNeg;
        String toUse = s;

        if (s.startsWith(Utility.hexStart))
            toUse = s.substring(Utility.hexStart.length());

        String lastString = toUse.substring(toUse.length() - 1);

        if (toUse.substring(0, 1).equals("-"))
        {
            toUse = "-0" + toUse.substring(1, toUse.length() - 1);
            isNeg = true;
        }
        else
        {
            toUse = "0" + toUse.substring(0, toUse.length() - 1);
            isNeg = false;
        }

        Integer i = Integer.valueOf(toUse, 16);

        int val = i.intValue();

        val = val << 4;

        if (isNeg)
        {
            val -= Integer.valueOf(lastString, 16).intValue();
        }
        else
        {
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

    public static long hexStringToLong (String s) throws NumberFormatException
    {
        boolean isNeg;
        String toUse = s;

        if (s.startsWith(Utility.hexStart))
            toUse = s.substring(Utility.hexStart.length());

        String lastString = toUse.substring(toUse.length() - 1);

        if (toUse.substring(0, 1).equals("-"))
        {
            toUse = "-0" + toUse.substring(1, toUse.length() - 1);
            isNeg = true;
        }
        else
        {
            toUse = "0" + toUse.substring(0, toUse.length() - 1);
            isNeg = false;
        }

        Long i = Long.valueOf(toUse, 16);

        long val = i.longValue();

        val = val << 4;

        if (isNeg)
        {
            val -= Long.valueOf(lastString, 16).longValue();
        }
        else
        {
            val += Long.valueOf(lastString, 16).longValue();
        }

        return val;
    }

    /**
     * @return an integer representing the ip address of the local machine.
     *         Essentially the bytes of the InetAddress are shuffled into the
     *         integer. This was once part of the Uid class but has been
     *         separated for general availability.
     * @since JTS 2.1.
     */

    public static synchronized int hostInetAddr () throws UnknownHostException
    {
        /*
         * Calculate only once.
         */

        if (myAddr == 0)
        {
            InetAddress addr = InetAddress.getLocalHost();
            byte[] b = addr.getAddress();

            for (int i = 0; i < b.length; i++)
            {
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

    /**
     * Convert a host name into an InetAddress object
     *
     * @param host
     *            if empty or null then the loopback address is used
     * @param messageKey
     *            message key to a report warning if host is unknown
     * @return an InetAddress structure corresponding the desired host name
     * @throws UnknownHostException
     *             if the hostname cannot be found
     */
    public static InetAddress hostNameToInetAddress (String host,
            String messageKey) throws UnknownHostException
    {
        try
        {
            return InetAddress.getByName(host);
        }
        catch (UnknownHostException ex)
        {
            /*
             * The hostname is unknown
             */
            if (tsLogger.arjLoggerI18N.isWarnEnabled() && messageKey != null)
                tsLogger.arjLoggerI18N.warn(messageKey, ex);

            throw ex;
        }
    }

    /**
     * Lookup and valid a port number.
     *
     * @param intProperty
     *            property name of an integer valued property
     * @param defValue
     *            a value to return if intProperty is invalid. If a null value
     *            is used and intProperty is invalid then
     *            @see com.arjuna.ats.arjuna.exceptions.FatalError is thrown
     * @param warnMsgKey
     *            message key to report a warning if property values is invalid
     * @param minValue
     *            minimum value for the integer propertry
     * @param maxValue
     *            maximum value for the integer propertry
     * @return the integer value of property or the default value if the
     *         property does not represent an integer
     *
     * @deprecated properties should come from environment beans now
     */
    @Deprecated
    public static Integer lookupBoundedIntegerProperty (PropertyManager pm,
            String intProperty, Integer defValue, String warnMsgKey,
            int minValue, int maxValue)
    {
        String intStr = pm.getProperty(intProperty);

        // if the propery is not found or empty just return the default
        if (intStr == null || intStr.length() == 0)
        {
            return defValue;
        }

        try
        {
            int i = Integer.parseInt(intStr);

            if (i < minValue || i > maxValue)
            {
                // the value is an invalid number
                if (warnMsgKey != null
                        && tsLogger.arjLoggerI18N.isWarnEnabled())
                {
                    tsLogger.arjLoggerI18N.warn(warnMsgKey, new Object[]
                    { intStr });
                }
            }
            else
            {
                return i;
            }
        }
        catch (Exception ex)
        {
            // the value is not a number
            if (warnMsgKey != null && tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N.warn(warnMsgKey, ex);
        }

        return defValue;
    }

    /**
     * @return the process id. This had better be unique between processes on
     *         the same machine. If not we're in trouble!
     * @since JTS 2.1.
     */

    public static final int getpid ()
    {
        Process handle = getProcess();

        return ((handle == null) ? -1 : handle.getpid());
    }

    /**
     * @return a Uid representing this process.
     * @since JTS 2.1.
     */

    public static final synchronized Uid getProcessUid ()
    {
        if (processUid == null)
            processUid = new Uid();

        return processUid;
    }

    public static final boolean isWindows ()
    {
        String os = System.getProperty("os.name");

        if (("WIN32".equals(os)) || (os.indexOf("Windows") != -1))
            return true;
        else
            return false;
    }

    public static final void setProcess (Process p)
    {
        processHandle = p;
    }

    /**
     * @message com.arjuna.ats.arjuna.utils.Utility_1
     *          [com.arjuna.ats.arjuna.utils.Utility_1] -
     *          Utility.getDefaultProcess - failed with
     */
    public static final Process getDefaultProcess ()
    {
        try
        {
            Class c = Thread.currentThread().getContextClassLoader().loadClass( arjPropertyManager.getCoreEnvironmentBean().getProcessImplementation());

            return (Process) c.newInstance();
        }
        catch (Exception e)
        {
            tsLogger.arjLoggerI18N.warn("Utility_1", e);

            return null;
        }
    }

    private static final Process getProcess ()
    {
        if (processHandle == null)
        {
            processHandle = getDefaultProcess();
        }

        return processHandle;
    }

    public static void validatePortRange(int port) {
        if(port < 0 || port > MAX_PORT) {
            throw new IllegalArgumentException("port value out of range "+port);
        }
    }

    private static int myAddr = 0;

    private static Uid processUid = null;

    private static Process processHandle = null;

    private static final String hexStart = "0x";

    public static final String defaultProcessId = "com.arjuna.ats.internal.arjuna.utils.SocketProcessId";

    /**
     * The maximum queue length for incoming connection indications (a request
     * to connect)
     */
    public static final int BACKLOG = 50;

    /**
     * Maximum value for a socket port
     */
    public static final int MAX_PORT = 65535;
}
