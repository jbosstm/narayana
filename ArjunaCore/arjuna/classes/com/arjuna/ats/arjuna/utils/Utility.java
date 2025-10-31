/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;

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

   private static final byte[] HEX_DIGITS;

   static {
      // each byte contains 2 hex digits
      HEX_DIGITS = new byte[16];
      HEX_DIGITS[0] = (byte) '0';
      HEX_DIGITS[1] = (byte) '1';
      HEX_DIGITS[2] = (byte) '2';
      HEX_DIGITS[3] = (byte) '3';
      HEX_DIGITS[4] = (byte) '4';
      HEX_DIGITS[5] = (byte) '5';
      HEX_DIGITS[6] = (byte) '6';
      HEX_DIGITS[7] = (byte) '7';
      HEX_DIGITS[8] = (byte) '8';
      HEX_DIGITS[9] = (byte) '9';
      HEX_DIGITS[10] = (byte) 'a';
      HEX_DIGITS[11] = (byte) 'b';
      HEX_DIGITS[12] = (byte) 'c';
      HEX_DIGITS[13] = (byte) 'd';
      HEX_DIGITS[14] = (byte) 'e';
      HEX_DIGITS[15] = (byte) 'f';
   }

   /**
    * Determine the number of hex chars needed to represent the given int
    * value. This is at least 1.
    *
    * @param value the int value
    * @return the number of hex chars needed to represent the value
    */
   public static int hexCharsOf(int value) {
      int nonZeroBits = Integer.SIZE - Long.numberOfLeadingZeros(value);
      // each hex char represents 4 bits: align the non-zero bits to the next multiple of 4 and divide by 4
      return Math.max(((nonZeroBits + 3) >> 2), 1);
   }

   /**
    * Convert a int to hex chars. The byte array must be big enough to hold
    * the expected number of hex chars. See {@link #hexCharsOf(int)} to determine
    * the number of hex chars needed.
    *
    * @param value            the int value to convert
    * @param ascii            the byte array to hold the hex chars
    * @param offset           the offset into the byte array to start writing
    * @param expectedHexChars the expected number of hex chars to write
    */
   public static void toHexChars(int value, byte[] ascii, int offset, int expectedHexChars) {
      for (int i = expectedHexChars - 1; i >= 0; i--) {
         int digit = value & 0xF;
         ascii[offset + i] = HEX_DIGITS[digit];
         value >>>= 4;
      }
   }

   /**
    * Determine the number of hex chars needed to represent the given long
    * value. This is at least 1.
    *
    * @param value the long value
    * @return the number of hex chars needed to represent the value
    */
   public static int hexCharsOf(long value) {
      int nonZeroBits = Long.SIZE - Long.numberOfLeadingZeros(value);
      // each hex char represents 4 bits: align the non-zero bits to the next multiple of 4 and divide by 4
      return Math.max(((nonZeroBits + 3) >> 2), 1);
   }

   /**
    * Convert a long to hex chars. The byte array must be big enough to hold
    * the expected number of hex chars. See {@link #hexCharsOf(long)} to determine
    * the number of hex chars needed.
    *
    * @param value            the long value to convert
    * @param ascii            the byte array to hold the hex chars
    * @param offset           the offset into the byte array to start writing
    * @param expectedHexChars the expected number of hex chars to write
    */
   public static void toHexChars(long value, byte[] ascii, int offset, int expectedHexChars) {
      for (int i = expectedHexChars - 1; i >= 0; i--) {
         int digit = (int) (value & 0xF);
         ascii[offset + i] = HEX_DIGITS[digit];
         value >>>= 4;
      }
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
     * Convert a hex String to a long
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
     * @return Long(s) representing the ip v6 address of the local machine.
     *         Essentially the bytes of the InetAddress are shuffled into the
     *         long(s). This was once part of the Uid class but has been
     *         separated for general availability.
     * @since JTS 2.1.
     */
    public static long[] hostInetAddr() throws UnknownHostException {
        if(myAddr == null) {
            calculateHostInetAddr();
        }

        return myAddr;
    }

    private static synchronized void calculateHostInetAddr () throws UnknownHostException
    {
        /*
         * Calculate only once.
         */

        if (myAddr == null)
        {
            myAddr = new long[2];
            
            myAddr[0] = 0;
            myAddr[1] = 0;
            
            byte[] b = null;
            InetAddress addr;

            try
            { 
                addr = InetAddress.getLocalHost(); 
            }
            catch (final UnknownHostException uhe) {
                tsLogger.i18NLogger.warn_utils_Utility_2();

                addr = InetAddress.getByName(null);
            } 
             
            if (addr instanceof Inet6Address)
            {
                // 16 bytes to work with.
                
                b = addr.getAddress();
            }
            else
            {
                /*
                 * Convert ipv4 to ipv6
                 * 
                 * We only have 4 bytes here.
                 * 
                 * ::FFFF:129.144.52.38
                 */

                byte[] v4Address = addr.getAddress();
                
                if (v4Address.length > 4)
                    throw new UnknownHostException();        
                
                b = new byte[16];
                
                // high order byte in [0]
                
                for (int i = 0; i < 10; i++)
                    b[i] = 0;
                
                b[10] = b[11] = (byte) 255;
                
                System.arraycopy(v4Address, 0, b, 12, v4Address.length);
            }

            for (int i = 0; i < 8; i++)
            {
                /*
                 * Convert signed byte into unsigned.
                 */

                int l = 0x7f & b[i];

                l += (0x80 & b[i]);

                myAddr[0] = (myAddr[0] << 8) | l;
            }
            
            for (int i = 8; i < 16; i++)
            {
                /*
                 * Convert signed byte into unsigned.
                 */

                int l = 0x7f & b[i];

                l += (0x80 & b[i]);

                myAddr[1] = (myAddr[1] << 8) | l;
            }               
        }
    }

    /**
     * Convert a host name into an InetAddress object
     *
     * @param host
     *            if empty or null then the loopback address is used
     * @return an InetAddress structure corresponding the desired host name
     * @throws UnknownHostException
     *             if the hostname cannot be found
     */
    public static InetAddress hostNameToInetAddress (String host) throws UnknownHostException
    {
        return InetAddress.getByName(host);
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
    public static Uid getProcessUid ()
    {
        if (processUid == null) {
            initProcessUid();
        }

        return processUid;
    }

    private static synchronized void initProcessUid() {
        // not done from a static initializer because Uid ctor calls back into this class.
        if(processUid == null) {
            processUid = new Uid();
        }
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

    private static synchronized void initDefaultProcess ()
    {
        if(processHandle == null)
        {
            processHandle = arjPropertyManager.getCoreEnvironmentBean().getProcessImplementation();
            if(processHandle == null) {
                tsLogger.i18NLogger.warn_utils_Utility_1();
            }
        }
    }

    private static final Process getProcess ()
    {
        if (processHandle == null)
        {
            initDefaultProcess();
        }

        return processHandle;
    }

    public static void validatePortRange(int port) {
        if(port < 0 || port > MAX_PORT) {
            throw new IllegalArgumentException("port value out of range "+port);
        }
    }
    
    public static synchronized String getDefaultProcessId ()
    {
        initialise();
        
        return defaultProcessId;
    }
    
    public static synchronized boolean isAndroid ()
    {
        initialise();
        
        return _isAndroid;
    }
    
    private static void initialise ()
    {
        if (defaultProcessId == null)
        {
            String t = System.getProperty("java.vm.vendor");
            
            if (t.toLowerCase().indexOf("android") != -1)
            {
                defaultProcessId = "com.arjuna.ats.internal.arjuna.utils.AndroidProcessId";
                
                _isAndroid = true;
            }
            else
                defaultProcessId = "com.arjuna.ats.internal.arjuna.utils.SocketProcessId";
        }
    }

    private static volatile long[] myAddr = null;

    private static Uid processUid = null;

    private static volatile Process processHandle = null;

    private static final String hexStart = "0x";

    private static volatile String defaultProcessId = null;

    private static boolean _isAndroid = false;
    
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