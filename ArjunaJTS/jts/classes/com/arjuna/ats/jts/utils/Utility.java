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
import java.nio.charset.StandardCharsets;
import com.arjuna.ats.jts.logging.jtsLogger;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Vote;
import org.omg.CosTransactions.otid_t;

import com.arjuna.ArjunaOTS.UidCoordinator;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.internal.jts.utils.Helper;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;

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
    
    public static String exceptionCode (int code)
    {
        switch (code)
        {
        case ExceptionCodes.ADD_FAILED:
            return "ExceptionCodes.ADD_FAILED";
        case ExceptionCodes.ALREADY_BEGUN:
            return "ExceptionCodes.ALREADY_BEGUN";
        case ExceptionCodes.ALREADY_ROLLEDBACK:
            return "ExceptionCodes.ALREADY_ROLLEDBACK";
        case ExceptionCodes.ALREADY_TERMINATED:
            return "ExceptionCodes.ALREADY_TERMINATED";
        case ExceptionCodes.ATOMICACTION_COMMIT:
            return "ExceptionCodes.ATOMICACTION_COMMIT";
        case ExceptionCodes.ATOMICACTION_ROLLBACK:
            return "ExceptionCodes.ATOMICACTION_ROLLBACK";
        case ExceptionCodes.BAD_TRANSACTION_CONTEXT:
            return "ExceptionCodes.BAD_TRANSACTION_CONTEXT";
        case ExceptionCodes.FAILED_TO_COMMIT:
            return "ExceptionCodes.FAILED_TO_COMMIT";
        case ExceptionCodes.GETTIMEOUT_FAILED:
            return "ExceptionCodes.GETTIMEOUT_FAILED";
        case ExceptionCodes.HEURISTIC_COMMIT:
            return "ExceptionCodes.HEURISTIC_COMMIT";
        case ExceptionCodes.INACTIVE_TRANSACTION:
            return "ExceptionCodes.INACTIVE_TRANSACTION";
        case ExceptionCodes.INVALID_ACTION:
            return "ExceptionCodes.INVALID_ACTION";
        case ExceptionCodes.INVALID_TIMEOUT:
            return "ExceptionCodes.INVALID_TIMEOUT";
        case ExceptionCodes.INVALID_TRANSACTION_BASE:
            return "ExceptionCodes.INVALID_TRANSACTION_BASE";
        case ExceptionCodes.MARKED_ROLLEDBACK:
            return "ExceptionCodes.MARKED_ROLLEDBACK";
        case ExceptionCodes.NO_TRANSACTION:
            return "ExceptionCodes.NO_TRANSACTION";
        case ExceptionCodes.NO_TXCONTEXT:
            return "ExceptionCodes.NO_TXCONTEXT";
        case ExceptionCodes.NOT_CURRENT_TRANSACTION:
            return "ExceptionCodes.NOT_CURRENT_TRANSACTION";
        case ExceptionCodes.OTS_BAD_OPERATION_BASE:
            return "ExceptionCodes.OTS_BAD_OPERATION_BASE";
        case ExceptionCodes.OTS_BAD_PARAM_BASE:
            return "ExceptionCodes.OTS_BAD_PARAM_BASE";
        case ExceptionCodes.OTS_GENERAL_BASE:
            return "ExceptionCodes.OTS_GENERAL_BASE";
        case ExceptionCodes.OTS_UNKNOWN_BASE:
            return "ExceptionCodes.OTS_UNKNOWN_BASE";
        case ExceptionCodes.SERVERAA_COMMIT:
            return "ExceptionCodes.SERVERAA_COMMIT";
        case ExceptionCodes.SERVERAA_NO_CONTROL:
            return "ExceptionCodes.SERVERAA_NO_CONTROL";
        case ExceptionCodes.SERVERAA_PREPARE:
            return "ExceptionCodes.SERVERAA_PREPARE";
        case ExceptionCodes.SYNCHRONIZATION_EXCEPTION:
            return "ExceptionCodes.SYNCHRONIZATION_EXCEPTION";
        case ExceptionCodes.TRANSACTION_REQUIRED_BASE:
            return "ExceptionCodes.TRANSACTION_REQUIRED_BASE";
        case ExceptionCodes.TRANSACTION_ROLLEDBACK_BASE:
            return "ExceptionCodes.TRANSACTION_ROLLEDBACK_BASE";
        case ExceptionCodes.UNAVAILABLE_COORDINATOR:
            return "ExceptionCodes.UNAVAILABLE_COORDINATOR";
        case ExceptionCodes.UNAVAILABLE_TRANSACTION:
            return "ExceptionCodes.UNAVAILABLE_TRANSACTION";
        case ExceptionCodes.UNEXPECTED_SYSTEMEXCEPTION:
            return "ExceptionCodes.UNEXPECTED_SYSTEMEXCEPTION";
        case ExceptionCodes.UNKNOWN_EXCEPTION:
            return "ExceptionCodes.UNKNOWN_EXCEPTION";
        case ExceptionCodes.UNKNOWN_INVALID:
            return "ExceptionCodes.UNKNOWN_INVALID";
        case ExceptionCodes.WRONG_TRANSACTION_BASE:
            return "ExceptionCodes.WRONG_TRANSACTION_BASE";
        default:
                return "Unknown";
        }
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
	byte[] b = theUid.getBytes(StandardCharsets.UTF_8);

	if (TxControl.getXANodeName() == null) {
		throw new IllegalStateException(jtsLogger.i18NLogger.get_nodename_null());
	}

	byte[] nodeName = TxControl.getXANodeName().getBytes(StandardCharsets.UTF_8);

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
	    
	    Uid u = new Uid(new String(theUid, StandardCharsets.UTF_8), true);  // errors in string give NIL_UID
	    
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
