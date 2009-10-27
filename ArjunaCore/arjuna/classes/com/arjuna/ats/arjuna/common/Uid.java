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
 * $Id: Uid.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.common;

import com.arjuna.ats.arjuna.utils.Utility;

import java.lang.Cloneable;
import java.io.Serializable;
import java.io.PrintStream;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import java.io.IOException;
import java.net.UnknownHostException;
import java.lang.StringIndexOutOfBoundsException;
import java.lang.NumberFormatException;
import java.lang.CloneNotSupportedException;

import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Implements a unique identity class. Since 4.9 each instance is immutable.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Uid.java 2342 2006-03-30 13:06:17Z  $
 * @since 1.0.
 *
 * @message com.arjuna.ats.arjuna.common.Uid_1
 *          [com.arjuna.ats.arjuna.common.Uid_1] - cannot get local host.
 * @message com.arjuna.ats.arjuna.common.Uid_2
 *          [com.arjuna.ats.arjuna.common.Uid_2] - Uid.Uid string constructor
 *          could not create nullUid
 * @message com.arjuna.ats.arjuna.common.Uid_3
 *          [com.arjuna.ats.arjuna.common.Uid_3] - Uid general parsing error:
 *          {0}
 * @message com.arjuna.ats.arjuna.common.Uid_4
 *          [com.arjuna.ats.arjuna.common.Uid_4] - Uid.Uid string constructor
 *          could not create nullUid for incorrect string: {0}
 * @message com.arjuna.ats.arjuna.common.Uid_5
 *          [com.arjuna.ats.arjuna.common.Uid_5] - Uid.Uid string constructor
 *          incorrect: {0}
 * @message com.arjuna.ats.arjuna.common.Uid_6
 *          [com.arjuna.ats.arjuna.common.Uid_6] - Uid.generateHash called for
 *          invalid Uid. Will ignore.
 * @message com.arjuna.ats.arjuna.common.Uid_7
 *          [com.arjuna.ats.arjuna.common.Uid_7] - nullUid error for
 * @message com.arjuna.ats.arjuna.common.Uid_8
 *          [com.arjuna.ats.arjuna.common.Uid_8] - Invalid string:
 * @message com.arjuna.ats.arjuna.common.Uid_9
 *          [com.arjuna.ats.arjuna.common.Uid_9] - Invalid Uid object.
 * @message com.arjuna.ats.arjuna.common.Uid_10
 *          [com.arjuna.ats.arjuna.common.Uid_10] - Cannot unpack into nullUid!
 * @message com.arjuna.ats.arjuna.common.Uid_11
 *          [com.arjuna.ats.arjuna.common.Uid_11] - Uid.Uid recreate constructor
 *          could not recreate Uid!
 */

public class Uid implements Cloneable, Serializable
{
	private static final long serialVersionUID = 7808395904206530189L;

	/**
	 * Create a new instance.
	 */

	public Uid ()
	{
		hostAddr = null;
		process = 0;
		sec = 0;
		other = 0;
		_hashValue = -1;
		_valid = false;
		_stringForm = null;

		try
		{
			hostAddr = Utility.hostInetAddr(); /* calculated only once */
			process = Utility.getpid();

			if (Uid.initTime == 0)
				Uid.initTime = (int) (System.currentTimeMillis() / 1000);

			sec = Uid.initTime;

			other = Uid.getValue();
			
			_valid = true;
			
			generateHash();
		}
		catch (UnknownHostException e)
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Uid_1");
			_valid = false;
		}
	}

	/**
	 * Create a copy of the specified identifier.
	 */

	public Uid (Uid copyFrom)
	{
		copy(copyFrom);
	}

	/**
	 * Create Uid from string representation. If the string does not represent a
	 * valid Uid then the instance will be set to nullUid.
	 */

	public Uid (String uidString)
	{
		this(uidString, true);
	}

	/**
	 * Create Uid from string representation. boolean arg says whether to give
	 * up if an error is detected or to simply replace with nullUid.
	 */

	public Uid (String uidString, boolean errsOk)
	{
		char theBreakChar = Uid.getBreakChar(uidString);

		hostAddr = new long[2];
		process = 0;
		sec = 0;
		other = 0;
		_hashValue = -1;
		_valid = false;
		_stringForm = null;

		if (uidString.length() > 0)
		{
			int startIndex = 0;
			int endIndex = 0;
			String s = null;

			try
			{
				while (uidString.charAt(endIndex) != theBreakChar)
					endIndex++;

				s = uidString.substring(startIndex, endIndex);
				hostAddr[0] = Utility.hexStringToLong(s);

				startIndex = endIndex + 1;
				endIndex++;
				while (uidString.charAt(endIndex) != theBreakChar)
                                    endIndex++;

				s = uidString.substring(startIndex, endIndex);
                                hostAddr[1] = Utility.hexStringToLong(s);

                                startIndex = endIndex + 1;
                                endIndex++;
 
				while (uidString.charAt(endIndex) != theBreakChar)
					endIndex++;

				s = uidString.substring(startIndex, endIndex);
				process = (int) Utility.hexStringToLong(s);

				startIndex = endIndex + 1;
				endIndex++;

				while (uidString.charAt(endIndex) != theBreakChar)
					endIndex++;

				s = uidString.substring(startIndex, endIndex);
				sec = (int) Utility.hexStringToLong(s);

				s = uidString.substring(endIndex + 1, uidString.length());
				other = (int) Utility.hexStringToLong(s);

				_valid = true;
				
				generateHash();
			}
			catch (NumberFormatException e)
			{
				if (!errsOk)
				{
					if (tsLogger.arjLoggerI18N.isWarnEnabled())
					{
						tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Uid_3", new Object[]
						{ uidString }, e);
					}
				}

				_valid = false;
			}
			catch (StringIndexOutOfBoundsException e)
			{
                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                {
                    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Uid_3", new Object[]
                            { uidString }, e);
                }
			    
				_valid = false;
			}
		}
		else
		{
			this.copy(Uid.nullUid());
		}

		if (!_valid)
		{
			if (errsOk)
			{
				try
				{
					this.copy(Uid.nullUid());
				}
				catch (Exception e)
				{
					if (tsLogger.arjLoggerI18N.isFatalEnabled())
					{
						tsLogger.arjLoggerI18N.fatal("com.arjuna.ats.arjuna.common.Uid_4", new Object[]
						{ uidString });
					}

					throw new FatalError(
							tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.common.Uid_2"), e);
				}
			}
			else
			{
				if (tsLogger.arjLoggerI18N.isFatalEnabled())
				{
					tsLogger.arjLoggerI18N.fatal("com.arjuna.ats.arjuna.common.Uid_5", new Object[]
					{ uidString });
				}

				throw new FatalError(
						tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.common.Uid_3")
								+ uidString);
			}
		}
	}
	
	public Uid (long[] addr, int processId, int time, int incr)
        {
            try
            {
                    hostAddr = new long[2];
                    hostAddr[0] = addr[0];
                    hostAddr[1] = addr[1];
                    
                    process = processId;
                    sec = time;
                    other = incr;
                    
                    _valid = true;
                    
                    generateHash();
            }
            catch (Throwable ex)
            {
                _valid = false;
                
                throw new FatalError(
                        tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.common.Uid_11")
                                        + ex);
            }       
        }

	/**
	 * Override Object.hashCode. We always return a positive value.
	 */

	/*
	 * generateHash should have been called by now.
	 */

	public int hashCode ()
	{
		return _hashValue;
	}

	/**
	 * Print a human-readable form of the Uid.
	 */

	public void print (PrintStream strm)
	{
		strm.print("<Uid:" + this.toString() + ">");
	}

	public String stringForm ()
	{
	    // no need to synchronize since object is immutable
	    
	    if (_stringForm == null)
		_stringForm = Utility.longToHexString(hostAddr[0]) + Uid.breakChar
		                + Utility.longToHexString(hostAddr[1]) + Uid.breakChar
				+ Utility.intToHexString(process) + Uid.breakChar
				+ Utility.intToHexString(sec) + Uid.breakChar
				+ Utility.intToHexString(other);
	    
	    return _stringForm;
	}

	/**
	 * @return a string representation of the Uid with all : replaced by _ so
	 *         that the name may be used as a file name.
	 */

	public String fileStringForm ()
	{
		return Utility.longToHexString(hostAddr[0]) + Uid.fileBreakChar
		                + Utility.longToHexString(hostAddr[1]) + Uid.fileBreakChar
				+ Utility.intToHexString(process) + Uid.fileBreakChar
				+ Utility.intToHexString(sec) + Uid.fileBreakChar
				+ Utility.intToHexString(other);
	}

	/**
	 * Same as stringForm()
	 */

	public String toString ()
	{
		return stringForm();
	}

    // return the process id value in hex form.
    // The internal format is Uids mostly should not be exposed, but some recovery/expiry code need this.
    public String getHexPid() {
        return Utility.intToHexString(process);
    }

	/**
	 * Create a copy of this instance.
	 */

	public Object clone () throws CloneNotSupportedException
	{
		return new Uid(this);
	}

	/**
	 * Copy the specified Uid over this instance.
	 */

	public void copy (Uid toCopy)
	{
		if (toCopy == this)
			return;

		hostAddr = toCopy.hostAddr;
		process = toCopy.process;
		sec = toCopy.sec;
		other = toCopy.other;
		_hashValue = toCopy._hashValue;
		_valid = toCopy._valid;
	}

	/**
	 * Uid comparisons.
	 */

	/**
	 * Override Object.equals
	 */

	public boolean equals (Object o)
	{
		if (o instanceof Uid)
			return this.equals((Uid) o);
		else
			return false;
	}

	public boolean equals (Uid u)
	{
		if (u == null)
			return false;

		if (u == this)
			return true;

		return ((other == u.other) && (sec == u.sec) && (process == u.process) && (hostAddr[0] == u.hostAddr[0]) && (hostAddr[1] == u.hostAddr[1]));
	}

	public boolean notEquals (Uid u)
	{
		if (u == null)
			return true;

		if (u == this)
			return false;

		return ((other != u.other) || (sec != u.sec) || (process != u.process) || (hostAddr[0] != u.hostAddr[0]) || (hostAddr[1] != u.hostAddr[1]));
	}

	public boolean lessThan (Uid u)
	{
		if (u == null)
			return false;

		if (u == this)
			return false;

		if (this.equals(u))
			return false ;

		if (LAST_RESOURCE_UID.equals(this))
			return false ;

		if (LAST_RESOURCE_UID.equals(u))
			return true ;

		if ((hostAddr[0] < u.hostAddr[0]) && (hostAddr[1] < u.hostAddr[1]))
			return true;
		else
		{
		    if ((hostAddr[0] == u.hostAddr[0]) && (hostAddr[1] == u.hostAddr[1]))
		    {
		        if (process < u.process)
		            return true;
		        else
		            if (process == u.process)
		            {
		                if (sec < u.sec)
		                    return true;
		                else
		                    if ((sec == u.sec) && (other < u.other))
		                        return true;
		            }
		    }
		}
		
		return false;
	}

	public boolean greaterThan (Uid u)
	{
		if (u == null)
			return false;

		if (u == this)
			return false;

		if (this.equals(u))
			return false ;

		if (LAST_RESOURCE_UID.equals(this))
			return true ;

		if (LAST_RESOURCE_UID.equals(u))
			return false ;

		if ((hostAddr[0] > u.hostAddr[0]) && (hostAddr[1] > u.hostAddr[1]))
			return true;
		else
		{
			if ((hostAddr[0] == u.hostAddr[0]) && (hostAddr[1] == u.hostAddr[0]))
			{
				if (process > u.process)
					return true;
				else
					if (process == u.process)
					{
						if (sec > u.sec)
							return true;
						else
							if ((sec == u.sec) && (other > u.other))
								return true;
					}
			}
		}
		
		return false;
	}

	/**
	 * Is the Uid valid?
	 */

	public final boolean valid ()
	{
		return _valid;
	}

	/**
	 * Return a null Uid (0:0:0:0:0)
	 */

	public static final Uid nullUid ()
	{
		return NIL_UID;
	}

	/**
	 * Return a last resource Uid (0:0:0:0:1)
	 */
	public static final Uid lastResourceUid ()
	{
		return LAST_RESOURCE_UID;
	}

	/**
	 * Return the maximum Uid (7fffffff:7fffffff:7fffffff:7fffffff:7fffffff)
	 */
	public static final Uid maxUid ()
	{
		return MAX_UID;
	}

	/**
	 * Return the minimum Uid (-80000000:-80000000:-80000000:-80000000:-80000000)
	 */
	public static final Uid minUid ()
	{
		return MIN_UID;
	}

	/*
	 * Serialization methods. Similar to buffer packing. If the Uid is invalid
	 * the an IOException is thrown.
	 *
	 * @since JTS 2.1.
	 */

	private void writeObject (java.io.ObjectOutputStream out)
			throws IOException
	{
		if (_valid)
		{
			out.writeLong(hostAddr[0]);
			out.writeLong(hostAddr[1]);
			out.writeInt(process);
			out.writeInt(sec);
			out.writeInt(other);
		}
		else
			throw new IOException("Invalid Uid object.");
	}

	/*
	 * Serialization methods. Similar to buffer unpacking. If the
	 * unserialization fails then an IOException is thrown.
	 *
	 * @since JTS 2.1.
	 */

	private void readObject (java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException
	{
		try
		{
			hostAddr[0] = in.readLong();
			hostAddr[1] = in.readLong();
			process = in.readInt();
			sec = in.readInt();
			other = in.readInt();	
	                     
                        _valid = true; // should not be able to pack an invalid Uid.
                        
			generateHash();
		}
		catch (IOException e)
		{
			_valid = false;

			throw e;
		}
	}

	private static final synchronized int getValue ()
	{
		if ((Uid.uidsCreated & 0xf0000000) > 0)
		{
			Uid.uidsCreated = 0;
			Uid.initTime = (int) (System.currentTimeMillis() / 1000);
		}

		return Uid.uidsCreated++;
	}

	/*
	 * Is an idempotent operation, so can be called more than once without
	 * adverse effect.
	 */

	private final void generateHash ()
	{
		if (_valid)
		{
			if (true)
				_hashValue = (int) hostAddr[0] ^ (int) hostAddr[1] ^ process ^ sec ^ other;
			else
			{
				int g = 0;
				String p = toString();
				int len = p.length();
				int index = 0;

				if (len > 0)
				{
					while (len-- > 0)
					{
						_hashValue = (_hashValue << 4)
								+ (int) (p.charAt(index));
						g = _hashValue & 0xf0000000;

						if (g > 0)
						{
							_hashValue = _hashValue ^ (g >> 24);
							_hashValue = _hashValue ^ g;
						}

						index++;
					}
				}
			}

			if (_hashValue < 0)
				_hashValue = -_hashValue;
		}
		else
		{
			if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.common.Uid_6");
		}
	}

	/*
	 * Since we may be given a Uid from the file system (which uses '_' to
	 * separate fields, we need to be able to convert.
	 */

	private static final char getBreakChar (String uidString)
	{
		if (uidString == null)
			return Uid.breakChar;

		if (uidString.indexOf(fileBreakChar) != -1)
			return Uid.fileBreakChar;
		else
			return Uid.breakChar;
	}

	protected long[] hostAddr;  // representation of ipv6 address (and ipv4)
	protected int process;
	protected int sec;
	protected int other;

	private int _hashValue;

	private boolean _valid;
	
	private String _stringForm;

	private static int uidsCreated ;

	private static int initTime ;

	private static char breakChar = ':';

	private static char fileBreakChar = '_';

	private static Uid NIL_UID = new Uid("0:0:0:0:0") ;

	private static Uid LAST_RESOURCE_UID = new Uid("0:0:0:0:1") ;

	private static Uid MAX_UID = new Uid("7fffffff:7fffffff:7fffffff:7fffffff:7fffffff") ;

	private static Uid MIN_UID = new Uid("-80000000:-80000000:-80000000:-80000000:-80000000") ;
}
