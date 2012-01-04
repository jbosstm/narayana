/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * $Id: Utility.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts.utils;

import java.io.PrintWriter;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Vote;
import org.omg.CosTransactions.otid_t;

import com.arjuna.ArjunaOTS.UidCoordinator;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jts.utils.Helper;

/**
 * Some useful utility functions for the OTS. Use with care!
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: Utility.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class Utility
{

    public static String getHierarchy (PropagationContext ctx)
    {
	int depth = ((ctx.parents != null) ? ctx.parents.length : 0);
	String hier = "PropagationContext:";

	for (int i = depth -1; i >= 0; i--)
	{
	    if (ctx.parents[i] != null)
		hier += "\n"+Utility.otidToUid(ctx.parents[i].otid);
	}

	hier += "\n"+Utility.otidToUid(ctx.current.otid);

	return hier;
    }

    /**
     * @since JTS 2.1.2.
     */

    public static String stringVote (org.omg.CosTransactions.Vote v)
    {
	switch (v.value())
	{
	case Vote._VoteCommit:
	    return "CosTransactions::VoteCommit";
	case Vote._VoteRollback:
	    return "CosTransactions::VoteRollback";
	case Vote._VoteReadOnly:
	    return "CosTransactions::VoteReadOnly";
	default:
	    return "Unknown";
	}
    }

    public static PrintWriter printStatus (PrintWriter strm, org.omg.CosTransactions.Status res)
    {
	strm.print(stringStatus(res));

	return strm;
    }

    /**
     * @since JTS 2.1.2.
     */

    public static String stringStatus (org.omg.CosTransactions.Status res)
    {
	switch (res.value())
	{
	case Status._StatusActive:
	    return "CosTransactions::StatusActive";
	case Status._StatusMarkedRollback:
	    return "CosTransactions::StatusMarkedRollback";
	case Status._StatusPrepared:
	    return "CosTransactions::StatusPrepared";
	case Status._StatusCommitted:
	    return "CosTransactions::StatusCommitted";
	case Status._StatusRolledBack:
	    return "CosTransactions::StatusRolledBack";
	case Status._StatusUnknown:
	    return "CosTransactions::StatusUnknown";
	case Status._StatusPreparing:
	    return "CosTransactions::StatusPreparing";
	case Status._StatusCommitting:
	    return "CosTransactions::StatusCommitting";
	case Status._StatusRollingBack:
	    return "CosTransactions::StatusRollingBack";
	case Status._StatusNoTransaction:
	    return "CosTransactions::StatusNoTransaction";
	default:
	    return "Unknown";
	}
    }

    /*
     * Any need for the inverse operation?
     * Could easily do it for JBoss transactions only.
     */

    /**
     * If this control refers to an JBoss transaction then return its native
     * Uid representation. Otherwise return Uid.nullUid().
     */

    public static final Uid getUid (org.omg.CosTransactions.Control cont)
    {
	try
	{
	    UidCoordinator coord = Helper.getUidCoordinator(cont);

	    if (coord == null)
		throw new BAD_PARAM();
	    else
		return Helper.getUid(coord);
	}
	catch (BAD_PARAM ex)
	{
	    return Uid.nullUid();
	}
    }

    /**
     * @since JTS 2.2.
     */

    public static final org.omg.CosTransactions.otid_t uidToOtid (Uid theUid)
    {
	return ((theUid != null) ? uidToOtid(theUid.stringForm()) : null);
    }

    public static final org.omg.CosTransactions.otid_t uidToOtid (String theUid)
    {
	if (theUid == null)
	    return null;

	otid_t otid = new otid_t();
	byte[] b = theUid.getBytes();
	byte[] nodeName = TxControl.getXANodeName().getBytes();

	otid.formatID = 0;
	otid.tid = new byte[b.length+nodeName.length];
	otid.bqual_length = nodeName.length;
	
	/*
	 * gtrid must be first then immediately followed by bqual.
	 * bqual must be between 1 and 64 bytes if for XA.
	 */

	System.arraycopy(b, 0, otid.tid, 0, b.length);
	System.arraycopy(nodeName, 0, otid.tid, b.length, nodeName.length);

	b = null;

	return otid;
    }

    /*
     * If we cannot deal with the otid then we could simply generate
     * a new Uid. We would need to keep the original otid in case
     * the transaction either retransmits it or the application asks
     * for it (e.g., via the PropagationContext).
     */

    public static final Uid otidToUid (org.omg.CosTransactions.otid_t otid)
    {
	if (otid.bqual_length > 0)
	{
	    int uidLength = otid.tid.length - otid.bqual_length;
	    byte[] theUid = new byte[uidLength];  // don't need null terminating character
	    
	    System.arraycopy(otid.tid, 0, theUid, 0, uidLength);
	    
	    Uid u = new Uid(new String(theUid), true);  // errors in string give NIL_UID
	    
	    /*
	     * Currently we ignore bqual. THIS WILL BE AN ISSUE FOR INTEROPERABILITY!!
	     */
	    
	    theUid = null;
	    
	    return u;
	}
	else
	    return Uid.nullUid();  // error, deal with in caller!
    }

}
