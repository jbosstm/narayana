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
 * $Id: XID.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.xa;

import java.io.Serializable;

/**
 * An X/Open XID implementation.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: XID.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class XID implements Serializable
{

	public static final int XIDDATASIZE = 128; /* size in bytes */

	public static final int MAXGTRIDSIZE = 64; /*
												 * maximum size in bytes of
												 * gtrid
												 */

	public static final int MAXBQUALSIZE = 64; /*
												 * maximum size in bytes of
												 * bqual
												 */

	public static final int NULL_XID = -1;

	public XID ()
	{
		formatID = NULL_XID;
		gtrid_length = 0;
		bqual_length = 0;
	}

	/**
	 * Check for equality, then check transaction id only.
	 */

	public final boolean isSameTransaction (XID xid)
	{
		if (formatID == xid.formatID)
		{
			if (gtrid_length == xid.gtrid_length)
			{
				if (equals(xid))
					return true;
				else
				{
					for (int i = 0; i < gtrid_length; i++)
					{
						if (data[i] != xid.data[i])
							return false;
					}

					return true;
				}
			}
		}

		return false;
	}

	public void copy (XID toCopy)
	{
		if ((toCopy == null) || (toCopy.formatID == NULL_XID))
		{
			formatID = NULL_XID;
			gtrid_length = 0;
			bqual_length = 0;
		}
		else
		{
			formatID = toCopy.formatID;
			gtrid_length = toCopy.gtrid_length;
			bqual_length = toCopy.bqual_length;

			System.arraycopy(toCopy.data, 0, data, 0, toCopy.data.length);
		}
	}

	public boolean equals (XID other)
	{
		if (other == null)
			return false;

		if (other == this)
			return true;
		else
		{
			if ((formatID == other.formatID)
					&& (gtrid_length == other.gtrid_length)
					&& (bqual_length == other.bqual_length))
			{
				for (int i = 0; i < (gtrid_length + bqual_length); i++)
				{
					if (data[i] != other.data[i])
						return false;
				}

				return true;
			}
			else
				return false;
		}
	}

	public String toString ()
	{
		return new String("< " + formatID + ", " + gtrid_length + ", "
				+ bqual_length + ", " + new String(data) + " >");
	}

	public int formatID; /* format identifier (0 for OSI) */
	public int gtrid_length; /* value not to exceed 64 */
	public int bqual_length; /* value not to exceed 64 */
	public byte[] data = new byte[XIDDATASIZE];

}
