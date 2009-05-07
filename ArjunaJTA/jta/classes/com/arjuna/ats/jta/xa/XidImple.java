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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XidImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.xa;

import com.arjuna.ats.jta.logging.jtaLogger;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.xa.XID;

import com.arjuna.ats.internal.arjuna.utils.XATxConverter;

import java.io.*;

import javax.transaction.xa.Xid;

import java.io.IOException;

/**
 * Implementation of javax.transaction.xa.Xid.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: XidImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class XidImple implements javax.transaction.xa.Xid, Serializable
{
	private static final long serialVersionUID = -8922505475867377266L;

	public XidImple ()
	{
		_theXid = null;
		hashCode = getHash(_theXid) ;
	}

	public XidImple (Xid xid)
	{
		_theXid = null;

		copy(xid);
		hashCode = getHash(_theXid) ;
	}

	public XidImple (AtomicAction c)
	{
		this(c.get_uid(), false);
	}

	public XidImple (AtomicAction c, boolean branch)
	{
		this(c.get_uid(), branch);
	}

	public XidImple (Uid id)
	{
		this(id, false);
	}

	public XidImple (Uid id, boolean branch)
	{
		try
		{
			_theXid = XATxConverter.getXid(id, branch);
		}
		catch (Exception e)
		{
			_theXid = null;

			// abort or throw exception?
		}
		hashCode = getHash(_theXid) ;
	}

	public XidImple (Uid id, Uid branch, int formatId)
	{
		try
		{
			_theXid = XATxConverter.getXid(id, branch, formatId);
		}
		catch (Exception e)
		{
			_theXid = null;

			// abort or throw exception?
		}
		hashCode = getHash(_theXid) ;
	}

	public XidImple (com.arjuna.ats.arjuna.xa.XID x)
	{
		_theXid = x;
		hashCode = getHash(_theXid) ;
	}

	public final boolean isSameTransaction (Xid xid)
	{
		if (xid == null)
			return false;

		if (xid instanceof XidImple)
		{
			return _theXid.isSameTransaction(((XidImple) xid)._theXid);
		}

		if (getFormatId() == xid.getFormatId())
		{
			byte[] gtx = xid.getGlobalTransactionId();

			if (_theXid.gtrid_length == gtx.length)
			{
				if (equals(xid))
					return true;
				else
				{
					for (int i = 0; i < _theXid.gtrid_length; i++)
					{
						if (_theXid.data[i] != gtx[i])
							return false;
					}

					return true;
				}
			}
		}

		return false;
	}

	public int getFormatId ()
	{
		if (_theXid != null)
		{
			return _theXid.formatID;
		}
		else
			return -1;
	}

	/**
	 * These operations critically rely on the fact that we unpack the array in
	 * the order we packed it!
	 */

	public byte[] getGlobalTransactionId ()
	{
		if (_theXid != null)
		{
			byte b[] = new byte[_theXid.gtrid_length];

			System.arraycopy(_theXid.data, 0, b, 0, b.length);

			return b;
		}
		else
			return null;
	}

	public byte[] getBranchQualifier ()
	{
		if (_theXid != null)
		{
			byte b[] = new byte[_theXid.bqual_length];

			System.arraycopy(_theXid.data, _theXid.gtrid_length, b, 0, b.length);

			return b;
		}
		else
			return null;
	}

	public final com.arjuna.ats.arjuna.xa.XID getXID ()
	{
		return _theXid;
	}

	public final void copy (Xid xid)
	{
		_theXid = new com.arjuna.ats.arjuna.xa.XID();

		if (xid != null)
		{
			if (xid instanceof XidImple)
				_theXid.copy(((XidImple) xid)._theXid);
			else
			{
				_theXid.formatID = xid.getFormatId();

				byte[] gtx = xid.getGlobalTransactionId();
				byte[] bql = xid.getBranchQualifier();
                final int bqlength = (bql == null ? 0 : bql.length) ;

				_theXid.gtrid_length = gtx.length;
				_theXid.bqual_length = bqlength;

				System.arraycopy(gtx, 0, _theXid.data, 0, gtx.length);
                if (bqlength > 0)
                {
                    System.arraycopy(bql, 0, _theXid.data, gtx.length, bql.length);
                }
			}
		}
	}

	public boolean equals (Xid xid)
	{
		if (xid == null)
			return false;

		if (xid == this)
			return true;
		else
		{
			if (xid instanceof XidImple)
				return ((XidImple) xid)._theXid.equals(_theXid);
			else
			{
				if (xid.getFormatId() == _theXid.formatID)
				{
					byte[] gtx = xid.getGlobalTransactionId();
					byte[] bql = xid.getBranchQualifier();
                    final int bqlength = (bql == null ? 0 : bql.length) ;

					if ((_theXid.gtrid_length == gtx.length)
							&& (_theXid.bqual_length == bqlength))
					{
						int i;

						for (i = 0; i < _theXid.gtrid_length; i++)
						{
							if (_theXid.data[i] != gtx[i])
								return false;
						}

						for (i = _theXid.gtrid_length; i < _theXid.gtrid_length
								+ _theXid.bqual_length; i++)
						{
							if (_theXid.data[i] != bql[i])
								return false;
						}

						return true;
					}
				}
			}
		}

		return false;
	}

	public final boolean packInto (OutputObjectState os)
	{
		boolean result = false;

		try
		{
			os.packInt(_theXid.formatID);
			os.packInt(_theXid.gtrid_length);
			os.packInt(_theXid.bqual_length);
			os.packBytes(_theXid.data);

			result = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			result = false;
		}

		return result;
	}

	public final boolean unpackFrom (InputObjectState os)
	{
		boolean result = false;

		try
		{
			if (_theXid == null)
				_theXid = new com.arjuna.ats.arjuna.xa.XID();

			_theXid.formatID = os.unpackInt();
			_theXid.gtrid_length = os.unpackInt();
			_theXid.bqual_length = os.unpackInt();
			_theXid.data = os.unpackBytes();

			hashCode = getHash(_theXid);

			result = true;
		}
		catch (Exception e)
		{
			result = false;
		}

		return result;
	}

	/**
	 * @message com.arjuna.ats.jta.xid.packerror Could not pack XidImple.
	 */

	public static final void pack (OutputObjectState os, Xid xid)
			throws IOException
	{
		if (xid instanceof XidImple)
		{
			XidImple x = (XidImple) xid;

			os.packBoolean(true);

			if (!x.packInto(os))
				throw new IOException(
						jtaLogger.logMesg.getString("com.arjuna.ats.jta.xid.packerror"));
		}
		else
		{
			os.packBoolean(false);

			ByteArrayOutputStream s = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(s);

			o.writeObject(xid);
			o.close();

			os.packBytes(s.toByteArray());
		}
	}

	public static final Xid unpack (InputObjectState os) throws IOException
	{
		if (os.unpackBoolean())
		{
			XidImple x = new XidImple();

			x.unpackFrom(os);

			return x;
		}
		else
		{
			try
			{
				byte[] b = os.unpackBytes();

				ByteArrayInputStream s = new ByteArrayInputStream(b);
				ObjectInputStream o = new ObjectInputStream(s);

				Xid x = (Xid) o.readObject();

				return x;
			}
			catch (Exception e)
			{
                IOException ioException = new IOException(e.toString());
                ioException.initCause(e);
                throw ioException;
			}
		}
	}

	/**
	 * @message com.arjuna.ats.jta.xa.xidunset Xid unset
	 */

	public String toString ()
	{
		if (_theXid != null)
			return _theXid.toString();
		else
			return jtaLogger.logMesg.getString("com.arjuna.ats.jta.xa.xidunset");
	}

	/**
	 * Is the specified object equal to this one?
	 * @param obj The object to test.
	 * @return true if they are equal, false otherwise.
	 */
    public boolean equals(final Object obj)
    {
        if (obj instanceof Xid)
        {
        	return equals((Xid)obj) ;
        }
        return false ;
    }

    /**
     * Return the hash code for this Xid.
     * @return the hash code.
     */
    public int hashCode()
    {
        return hashCode ;
    }

    /**
     * Generate the hash code for the xid.
     * @param xid The xid.
     * @return The hash code.
     */
    private static int getHash(final XID xid)
    {
    	if (xid == null)
    	{
    		return 0 ;
    	}
    	final int hash = generateHash(xid.formatID, xid.data, 0, xid.gtrid_length) ;
        return generateHash(hash, xid.data, xid.gtrid_length, xid.bqual_length) ;
    }

	/**
	 * Generate a hash code for the specified bytes.
	 * @param hash The initial hash.
	 * @param bytes The bytes to include in the hash.
	 * @return The new hash code.
	 */
    private static int generateHash(int hash, final byte[] bytes, final int start, final int length)
    {
        for(int count = start ; count < length ; count++)
        {
            hash = 31 * hash + bytes[count] ;
        }
        return hash ;
    }

	private XID _theXid;
    private int hashCode ;
}
