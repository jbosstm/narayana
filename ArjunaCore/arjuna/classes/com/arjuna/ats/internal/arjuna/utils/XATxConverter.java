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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: XATxConverter.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.utils;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.xa.XID;
import com.arjuna.ats.arjuna.coordinator.TxControl;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: XATxConverter.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class XATxConverter
{

    public static final int FORMAT_ID = 131075; // different from JTS ones.
    public static final char NODE_SEPARATOR = '-';

    public static XID getXid (Uid uid, boolean branch) throws IllegalStateException
    {
	return getXid(uid, branch, FORMAT_ID);
    }

    public static XID getXid (Uid uid, boolean branch, int formatId) throws IllegalStateException
    {
	if (branch)
	    return getXid(uid, new Uid(), formatId);
	else
	    return getXid(uid, Uid.nullUid(), formatId);
    }

    public static XID getXid (Uid uid, Uid branch, int formatId) throws IllegalStateException
    {
	if (uid == null)
	    throw new IllegalStateException();
	
	byte[] nodeName = TxControl.getXANodeName();
	String s = uid.stringForm();
	int uidLen = s.length()+nodeName.length+1;
		
	if (uidLen > com.arjuna.ats.arjuna.xa.XID.MAXGTRIDSIZE)  // Uid is too long!!!!
	{
	    throw new IllegalStateException();
	}
	else
	{
	    try
	    {
		XID xid = new XID();
		    
		xid.formatID = formatId;
		xid.gtrid_length = uidLen;

		/*
		 * Copy in the XA node identifier first.
		 */

		System.arraycopy(nodeName, 0, xid.data, 0, nodeName.length);
		
		xid.data[nodeName.length] = NODE_SEPARATOR;
		
		byte[] b = s.getBytes();

		/*
		 * Which way round do we fill in the structure?
		 */

		System.arraycopy(b, 0, xid.data, nodeName.length+1, b.length);
		    
		if (branch.notEquals(Uid.nullUid()))
		{
		    String bs = branch.stringForm();
		    int bsLen = bs.length();
		    
		    b = bs.getBytes();

		    if (bsLen > XID.MAXBQUALSIZE) // Uid is too long!!!!
			throw new IllegalStateException();
		    else
		    {
			xid.bqual_length = bsLen;
		    
			System.arraycopy (b, 0, xid.data, xid.gtrid_length, bsLen);
		    }
		
		    bs = null;
		}
		else
		{
		    /*
		     * Note: for some dbs we seem to be able to get
		     * away with setting the size field to the size
		     * of the actual branch. However, for Oracle,
		     * it appears as though it must always be 64.
		     * (At least for zero branches.)
		     */

		    xid.data[xid.gtrid_length] = (byte) 0;
		    xid.bqual_length = 64;
		}
		    
		b = null;
		s = null;

		return xid;
	    }
	    catch (Exception e)
	    {
		e.printStackTrace();
		
		throw new IllegalStateException(e.toString());
	    }
	}
    }

    public static Uid getUid (com.arjuna.ats.arjuna.xa.XID xid)
    {
	if (xid == null)
	    return Uid.nullUid();
	
	if ((xid.formatID == -1) || (xid.gtrid_length <= 0)) // means null XID
	    return Uid.nullUid();
	else
	{
	    int nodeNameIndex = 0;
	    
	    for (int i = 0; i < xid.gtrid_length; i++)
	    {
		if (xid.data[i] == NODE_SEPARATOR)
		{
		    nodeNameIndex = i+1;
		    break;
		}
	    }
	    
	    byte[] buff = new byte[xid.gtrid_length-nodeNameIndex];

	    System.arraycopy(xid.data, nodeNameIndex, buff, 0, buff.length);

	    Uid tx = new Uid(new String(buff), true);

	    buff = null;

	    return tx;
	}
    }

    public static Uid getBranch (com.arjuna.ats.arjuna.xa.XID xid)
    {
	return null;
    }
    
}
