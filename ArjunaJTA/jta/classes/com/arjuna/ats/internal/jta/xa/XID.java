/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.xa;

import java.io.Serializable;

import com.arjuna.ats.jta.xa.XATxConverter;

/**
 * An X/Open XID implementation.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: XID.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class XID implements Serializable
{
    private static final long serialVersionUID = 1L;

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
        // controversial and not too robust. see JBTM-297 before messing with this.

        if(formatID == XATxConverter.FORMAT_ID) {
            // it's one of ours, we know how to inspect it:
            return XATxConverter.getXIDString(this);
        }

        // it's a foreign id format, use a general algorithm:

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("< ");
        stringBuilder.append(formatID);
        stringBuilder.append(", ");
        stringBuilder.append(gtrid_length);
        stringBuilder.append(", ");
        stringBuilder.append(bqual_length);
        stringBuilder.append(", ");

        for (int i = 0; i < gtrid_length; i++) {
            stringBuilder.append(data[i]);
        }
        stringBuilder.append(", ");
        for (int i = 0; i < bqual_length; i++) {
            stringBuilder.append(data[gtrid_length+i]);
        }

        stringBuilder.append(" >");
        return stringBuilder.toString();
	}

	public int formatID; /* format identifier (0 for OSI) */
	public int gtrid_length; /* value not to exceed 64 */
	public int bqual_length; /* value not to exceed 64 */
	public byte[] data = new byte[XIDDATASIZE];

}