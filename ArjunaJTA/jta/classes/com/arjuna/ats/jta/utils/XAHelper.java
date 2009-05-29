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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XAHelper.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.utils;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.arjuna.xa.XID;

import javax.transaction.xa.*;

/**
 * Some useful utility routines.
 */

public class XAHelper
{

	/**
	 * Print a human-readable version of the XAException.
	 *
	 * @message com.arjuna.ats.jta.utils.nullexception Null exception!
	 * @message com.arjuna.ats.jta.utils.unknownerrorcode Unknown error code:
	 */

	public static String printXAErrorCode (XAException e)
	{
		if (e == null)
		{
			return jtaLogger.logMesg.getString("com.arjuna.ats.jta.utils.nullexception");
		}
		else
		{
			switch (e.errorCode)
			{
			case XAException.XA_RBROLLBACK:
				return "XAException.XA_RBROLLBACK";
			case XAException.XA_RBCOMMFAIL:
				return "XAException.XA_RBCOMMFAIL";
			case XAException.XA_RBDEADLOCK:
				return "XAException.XA_RBDEADLOCK";
			case XAException.XA_RBINTEGRITY:
				return "XAException.XA_RBINTEGRITY";
			case XAException.XA_RBOTHER:
				return "XAException.XA_RBOTHER";
			case XAException.XA_RBPROTO:
				return "XAException.XA_RBPROTO";
			case XAException.XA_RBTIMEOUT:
				return "XAException.XA_RBTIMEOUT";
			case XAException.XA_RBTRANSIENT:
				return "XAException.XA_RBTRANSIENT";
			case XAException.XA_NOMIGRATE:
				return "XAException.XA_NOMIGRATE";
			case XAException.XA_HEURHAZ:
				return "XAException.XA_HEURHAZ";
			case XAException.XA_HEURCOM:
				return "XAException.XA_HEURCOM";
			case XAException.XA_HEURRB:
				return "XAException.XA_HEURRB";
			case XAException.XA_HEURMIX:
				return "XAException.XA_HEURMIX";
			case XAException.XA_RDONLY:
				return "XAException.XA_RDONLY";
			case XAException.XAER_RMERR:
				return "XAException.XAER_RMERR";
			case XAException.XAER_NOTA:
				return "XAException.XAER_NOTA";
			case XAException.XAER_INVAL:
				return "XAException.XAER_INVAL";
			case XAException.XAER_PROTO:
				return "XAException.XAER_PROTO";
			case XAException.XAER_RMFAIL:
				return "XAException.XAER_RMFAIL";
			case XAException.XAER_DUPID:
				return "XAException.XAER_DUPID";
			case XAException.XAER_OUTSIDE:
				return "XAException.XAER_OUTSIDE";
			case XAException.XA_RETRY:
				return "XAException.XA_RETRY";
			default:
				return jtaLogger.logMesg.getString("com.arjuna.ats.jta.utils.unknownerrorcode")
						+ e.errorCode;
			}
		}
	}

	/**
	 * Compares two Xid instances.
	 *
	 * @param x1 first Xid
	 * @param x2 second Xid
	 *
	 * @return <code>true</code> if the two instances are the same,
	 *         <code>false</code> otherwise.
	 */

	public static boolean sameXID (Xid x1, Xid x2)
	{
		if (x1 == x2)
			return true;
		else
		{
			if (x1.getFormatId() == x2.getFormatId())
			{
				byte[] gtrid1 = x1.getGlobalTransactionId();
				byte[] gtrid2 = x2.getGlobalTransactionId();

				if (gtrid1.length == gtrid2.length)
				{
					for (int i = 0; i < gtrid1.length; i++)
					{
						if (gtrid1[i] != gtrid2[i])
							return false;
					}
				}
				else
					return false;

				byte[] bqual1 = x1.getBranchQualifier();
				final int bqual1Len = (bqual1 == null ? 0 : bqual1.length) ;
				byte[] bqual2 = x2.getBranchQualifier();
				final int bqual2Len = (bqual2 == null ? 0 : bqual2.length) ;

				if (bqual1Len == bqual2Len)
				{
					for (int i = 0; i < bqual1Len; i++)
					{
						if (bqual1[i] != bqual2[i])
							return false;
					}
				}
				else
					return false;

				return true;
			}
			else
				return false;
		}
	}

	/**
	 * Compares two Xid instances at the gtid level only.
	 *
	 * @param x1 first Xid
	 * @param x2 second Xid
	 *
	 * @return <code>true</code> if the two instances are the same,
	 *         <code>false</code> otherwise.
	 */

	public static boolean sameTransaction (Xid x1, Xid x2)
	{
		if (x1 == x2)
			return true;
		else
		{
			if (x1.getFormatId() == x2.getFormatId())
			{
				byte[] gtrid1 = x1.getGlobalTransactionId();
				byte[] gtrid2 = x2.getGlobalTransactionId();

				if (gtrid1.length == gtrid2.length)
				{
					for (int i = 0; i < gtrid1.length; i++)
					{
						if (gtrid1[i] != gtrid2[i])
							return false;
					}

					return true;
				}
				else
					return false;
			}
			else
			    return false;
		}
	}

	/**
	 * get a string representing anyones Xid similar too, but not the same as
	 * OTS_Transaction/xa/XID.toString()
	 */

	public static String xidToString (Xid xid)
	{
        if(xid instanceof XID) {
            // ensure consistent representation of our native XIDs in the log output.
            return xid.toString();
        }

	    byte[] gid = xid.getGlobalTransactionId();
	    byte[] bqual = xid.getBranchQualifier();
	    String toReturn = "< " + xid.getFormatId() + ", " + gid.length + ", "
				+ bqual.length + ", ";

	    for (int i = 0; i < gid.length; i++)
		toReturn += gid[i];

	    for (int j = 0; j < bqual.length; j++)
		toReturn += bqual[j];

	    toReturn += " >";

	    return toReturn;
	}

}
