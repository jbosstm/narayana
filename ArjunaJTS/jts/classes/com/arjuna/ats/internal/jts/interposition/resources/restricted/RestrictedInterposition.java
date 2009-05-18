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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RestrictedInterposition.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition.resources.restricted;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.utils.Utility;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted.*;
import com.arjuna.ats.internal.jts.interposition.*;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class RestrictedInterposition extends Interposition
{

public RestrictedInterposition ()
    {
    }

public static ControlImple create (PropagationContext context) throws SystemException
    {
	if (__list != null)
	    return __list.setupHierarchy(context);
	else
	    return null;
    }

    /*
     * Assume that all actions in the imported hierarchy are of the same
     * type, i.e., all JBoss transactions.
     *
     * Because of the way garbage collection works in the ORB we have to
     * run an explicit garbage collection phase for finished hierarchies.
     */

public synchronized ControlImple setupHierarchy (PropagationContext context) throws SystemException
    {
	ControlImple controlPtr = null;
	Uid theUid = null;
	ServerTopLevelAction proxyAction = null;

	if (context.parents.length == 0)
	    theUid = Utility.otidToUid(context.current.otid);
	else
	    theUid = Utility.otidToUid(context.parents[context.parents.length-1].otid);

	proxyAction = super.present(theUid);

	if (proxyAction == null)
	{
	    /*
	     * Create a new proxyAction element and return the
	     * "current" transaction.
	     */

	    controlPtr = createHierarchy(context, theUid);
	}
	else
	{
	    /*
	     * Check hierarchy of existing element.
	     */

	    controlPtr = checkHierarchy(proxyAction, context);
	}

	return controlPtr;
    }

protected synchronized ControlImple createHierarchy (PropagationContext ctx, Uid tlUid) throws SystemException
    {
	/*
	 * Start at the parent and work our way down to "current". The current
	 * transaction is not in the IDL sequence, but sent as separate field
	 * of the propagation context. This tends to make the code more
	 * complex than it would be if the entire hierarchy was represented in
	 * one place.
	 */

	int depth = ctx.parents.length;
	ServerTopLevelAction tlAction = null;
	Coordinator tmpCoord = null;
	Terminator tmpTerm = null;

	/*
	 * First deal with top-level transaction, which may be
	 * the current transaction.
	 */

	if (depth == 0)
	{
	    tmpCoord = ctx.current.coord;
	    tmpTerm = ctx.current.term;
	}
	else
	{
	    tmpCoord = ctx.parents[depth-1].coord;
	    tmpTerm = ctx.parents[depth-1].term;
	}

	ServerControl control = ServerFactory.create_transaction(tlUid, null, null,
									 tmpCoord, tmpTerm, ctx.timeout);

	tlAction = new ServerRestrictedTopLevelAction(control);

	if (!tlAction.valid())
	{
	    /*
	     * Just deal with current transaction. Others must have been
	     * registered successfully, and will be deal with automatically
	     * when the parent transaction terminates.
	     */

	    try
	    {
		tlAction.rollback();
		tlAction = null;
	    }
	    catch (Exception e)
	    {
	    }

	    throw new TRANSACTION_ROLLEDBACK();
	}

	ServerTopLevelAction newElement = tlAction;

	super._head.add(newElement);

	if (depth > 0) // current is a nested transaction
	{
	    /*
	     * Now deal with any nested transactions.
	     * As we create, register with the original transactions.
	     */

	    ServerRestrictedNestedAction nestedAction = null;

	    for (int i = depth -2; i >= 0; i--)
	    {
		tmpCoord = ctx.parents[i].coord;
		tmpTerm = ctx.parents[i].term;

		control = ServerFactory.create_subtransaction(Utility.otidToUid(ctx.parents[i].otid),
								  tmpCoord, tmpTerm, control);

		nestedAction = new ServerRestrictedNestedAction(control);

		if (!nestedAction.valid())
		{
		    /*
		     * Just deal with current transaction. Others must have been
		     * registered successfully, and will be deal with automatically
		     * when the parent transaction terminates.
		     */

		    try
		    {
			nestedAction.rollback();
			nestedAction = null;
		    }
		    catch (Exception e)
		    {
		    }

		    throw new TRANSACTION_ROLLEDBACK();
		}

		/*
		 * Add transaction resource to list.
		 */

		tlAction.addChild(nestedAction);
	    }

	    /*
	     * Now deal with current transaction. If there is only one transaction we do nothing.
	     */

	    tmpCoord = ctx.current.coord;
	    tmpTerm = ctx.current.term;

	    control = ServerFactory.create_subtransaction(Utility.otidToUid(ctx.current.otid),
							      tmpCoord, tmpTerm, control);
	    nestedAction = new ServerRestrictedNestedAction(control);

	    if (!nestedAction.valid())
	    {
		/*
		 * Just deal with current transaction. Others must have been
		 * registered successfully, and will be deal with automatically
		 * when the parent transaction terminates.
		 */

		try
		{
		    nestedAction.rollback();
		    nestedAction = null;
		}
		catch (Exception e)
		{
		}

		throw new TRANSACTION_ROLLEDBACK();
	    }

	    tlAction.addChild(nestedAction);
	}

	return control;
    }

    /*
     * Work our way down the hierarchy, aborting any actions which are no
     * longer valid, and creating any new ones. These new actions must be
     * nested actions.
     */

protected synchronized ControlImple checkHierarchy (ServerTopLevelAction hier,
						   PropagationContext context) throws SystemException
    {
	ServerRestrictedTopLevelAction tlAction = (ServerRestrictedTopLevelAction) hier;
	ServerControl control = tlAction.control();  // top-level's control
	int depth = context.parents.length;
	int differenceIndex = -1;  // index of the new transactions in the hierarchy
	ServerRestrictedNestedAction nestedAction = tlAction.child();  // top-level's nested action

	/*
	 * Abort any old actions before we check whether we need to create
	 * additional ones.
	 *
	 * To get here we have already checked the id of the parent
	 * transaction, i.e., depth -1.
	 *
	 * Remember: the context hierarchy *does not* include the current
	 * transaction!
	 */

	if (depth == 0)
	{
	    /*
	     * If there are no transactions in the sent hierarchy then
	     * current is the only nested transaction. So, abort the
	     * rest of our local hierarchy.
	     */

	    if (nestedAction != null)
	    {
		tlAction.abortChild(nestedAction);  // automatically removed from resource list
		nestedAction = null;
		control = tlAction.deepestControl();
	    }
	}
	else
	{
	    /*
	     * Start at -2 and work our way down the hierarchy. We
	     * use -2 since the length gives us the number of elements,
	     * which is 0 to n-1, and the n-1th element is the top-level
	     * transaction, which we have dealt with to get this far.
	     */

	    for (int i = depth -2; (i >= 0) && (nestedAction != null); i--)
	    {
		if (nestedAction.get_uid().equals(Utility.otidToUid(context.parents[i].otid)))
		{
		    /*
		     * nestedAction *always* points to the next transaction
		     * in the hierarchy when we leave this loop.
		     */

		    nestedAction = nestedAction.child();

		    if ((nestedAction == null) && (i > 0))
		    {
			differenceIndex = i -1;
			control = tlAction.deepestControl();
		    }
		}
		else
		{
		    /*
		     * Uids not equal, so abort. No need to continue down the
		     * hierarchy, as aborting from this point will implicitly
		     * abort out children.
		     */

		    differenceIndex = i;    // remember for later so that we can add new actions.

		    tlAction.abortChild(nestedAction);
		    nestedAction = null;
		    control = tlAction.deepestControl();

		    break;
		}
	    }

	    /*
	     * Do we have anything left in the sent hierarchy (other than
	     * current)? If so, add it now.
	     */

	    if (differenceIndex != -1)
	    {
		Coordinator tmpCoord;
		Terminator tmpTerm;

		for (int j = differenceIndex; j >= 0; j--)
		{
		    tmpCoord = context.parents[j].coord;
		    tmpTerm = context.parents[j].term;

		    control = ServerFactory.create_subtransaction(Utility.otidToUid(context.parents[j].otid),
								      tmpCoord, tmpTerm, control);
		    nestedAction = new ServerRestrictedNestedAction(control);

		    if (!nestedAction.valid())
		    {
			/*
			 * Just deal with current transaction. Others must have been
			 * registered successfully, and will be deal with automatically
			 * when the parent transaction terminates.
			 */

			try
			{
			    nestedAction.rollback();  // does dispose as well!
			    nestedAction = null;
			}
			catch (Exception e)
			{
			}

			throw new TRANSACTION_ROLLEDBACK();
		    }

		    tlAction.addChild(nestedAction);
		}

		nestedAction = null;
	    }
	    else
	    {
		if (nestedAction != null)
		{
		    /*
		     * If current transaction has a child then we should
		     * abort it, since it does not exist in the hierarchy
		     * we have just received.
		     */

		    nestedAction = nestedAction.child();

		    if (nestedAction != null)
		    {
			tlAction.abortChild(nestedAction);
			nestedAction = null;
			control = tlAction.deepestControl();
		    }
		}
	    }
	}

	boolean newCurrent = false;
	Uid sentCurrent = Utility.otidToUid(context.current.otid);

	/*
	 * If differentIndex is not -1 then we already found a difference
	 * between the sent hierarchy and the one we already had, so we
	 * must have a new current.
	 */

	if (differenceIndex == -1)
	{
	    /*
	     * Now determine whether we have to create any new nested actions.
	     */

	    Uid currentUid = null;

	    /*
	     * Get hold of our local notion of current.
	     */

	    if (nestedAction == null)
	    {
		nestedAction = tlAction.child();

		if (nestedAction != null)
		{
		    while (nestedAction.child() != null)
			nestedAction = nestedAction.child();

		    currentUid = nestedAction.get_uid();
		}
		else
		    currentUid = tlAction.get_uid();
	    }
	    else
		currentUid = nestedAction.get_uid();

	    /*
	     * Is our notion of the current transaction the same as
	     * that sent?
	     */

	    if (currentUid.notEquals(sentCurrent))
	    {
		newCurrent = true;
	    }
	}
	else
	    newCurrent = true;

	if (newCurrent)
	{
	    if (depth == 1)
	    {
		/*
		 * Old current is gone.
		 */

		nestedAction = tlAction.child();

		if (nestedAction != null)
		{
		    tlAction.abortChild(nestedAction);
		    nestedAction = null;
		}

		control = (ServerControl) tlAction.control();
	    }
	    else
		control = tlAction.deepestControl();

	    TransIdentity currentID = context.current;

	    control = ServerFactory.create_subtransaction(sentCurrent,
							      currentID.coord, currentID.term, control);
	    nestedAction = new ServerRestrictedNestedAction(control);

	    if (!nestedAction.valid())
	    {
		/*
		 * Just deal with current transaction. Others must have been
		 * registered successfully, and will be deal with automatically
		 * when the parent transaction terminates.
		 */

		try
		{
		    nestedAction.rollback();  // does dispose as well!
		    nestedAction = null;
		}
		catch (Exception e)
		{
		}

		throw new TRANSACTION_ROLLEDBACK();
	    }

	    tlAction.addChild(nestedAction);

	    nestedAction = null;
	}

	return control;
    }

private static RestrictedInterposition __list = new RestrictedInterposition();

}
