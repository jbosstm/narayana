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
 * $Id: OSIInterposition.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition.resources.osi;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.TransIdentity;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.interposition.ServerFactory;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.Interposition;
import com.arjuna.ats.internal.jts.interposition.resources.arjuna.ServerResource;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerTopLevelAction;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.osi.ServerOSINestedAction;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.osi.ServerOSITopLevelAction;
import com.arjuna.ats.jts.logging.jtsLogger;

public class OSIInterposition extends Interposition
{

public static ControlImple create (PropagationContext context) throws SystemException
    {
	if (__list != null)
	    return __list.setupHierarchy(context);
	else
	    return null;
    }

    /*
     * Currently assume that formatID and otid are not a unique combination,
     * i.e., if we see the same otid but with a different formatID it's
     * really the same transaction.
     */

public synchronized ControlImple setupHierarchy (PropagationContext context) throws SystemException
    {
	/*
	 * Map otid to a Uid.
	 */

	Uid theUid = OTIDMap.find(context.current.otid);
	ServerTopLevelAction proxyAction = present(theUid);
	ControlImple controlPtr = null;

	if (proxyAction == null)
	{
	    /*
	     * Create a new proxyAction element and return the "current" transaction.
	     */

	    controlPtr = createHierarchy(context, theUid);
	}
	else
	{
	    controlPtr = checkHierarchy(proxyAction, context);
	}

	return controlPtr;
    }

protected OSIInterposition ()
    {
    }

protected synchronized ControlImple createHierarchy (PropagationContext ctx,
						    Uid currentUid) throws SystemException
    {
	/*
	 * Start at the parent and work our way down to "current". The current
	 * transaction is not in the IDL sequence, but sent as separate field
	 * of the propagation context. This tends to make the code more
	 * complex than it would be if the entire hierarchy was represented in
	 * one place.
	 */

	/*
	 * We only ever register the current transaction with its parent, and
	 * as each transaction commits, it registers its parent with the
	 * "real" parent.
	 */

	int depth = ctx.parents.length;
	ServerResource action = null;
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

	if (tmpCoord == null)  // terminator may correctly be NULL
	{
	    return null;
	}

	ServerControl control = ServerFactory.create_transaction(currentUid, null,
									 null, tmpCoord, tmpTerm,
								       ctx.timeout);

	action = new ServerOSITopLevelAction(control, ((depth == 0) ? true : false));

	if (!action.valid())
	{
	    try
	    {
		((ServerOSITopLevelAction) action).rollback();  // does dispose as well!
		action = null;
	    }
	    catch (Exception e)
	    {
	    }

	    throw new TRANSACTION_ROLLEDBACK();
	}

	ServerTopLevelAction newElement = (ServerOSITopLevelAction)action;

	_head.add(newElement);

	if (depth > 0)  // current is a nested transaction
	{
	    /*
	     * Now deal with any nested transactions.
	     * As we create, register with the original transactions.
	     */

	    ServerResource nestedAction = null;

	    for (int i = depth -2; i >= 0; i--)
	    {
		tmpCoord = ctx.parents[i].coord;
		tmpTerm = ctx.parents[i].term;

		control = ServerFactory.create_subtransaction(OTIDMap.find(ctx.parents[i].otid),
								  tmpCoord, tmpTerm, control);

		nestedAction = new ServerOSINestedAction(control, false);  // not current, so don't register

		if (!nestedAction.valid())
		{
		    /*
		     * Just deal with current transaction. Others must have been
		     * registered successfully, and will be deal with automatically
		     * when the parent transaction terminates.
		     */

		    try
		    {
			((ServerOSINestedAction) nestedAction).rollback_subtransaction();  // does dispose as well!
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

		action.addChild((ServerOSINestedAction) nestedAction);
		action = nestedAction;
	    }

	    /*
	     * Now deal with current transaction. If there is
	     * only one transaction we do nothing.
	     */

	    tmpCoord = ctx.current.coord;
	    tmpTerm = ctx.current.term;

	    control = ServerFactory.create_subtransaction(OTIDMap.find(ctx.current.otid),
							      tmpCoord, tmpTerm, control);

	    nestedAction = new ServerOSINestedAction(control, true);  // current, so register

	    if (!nestedAction.valid())
	    {
		/*
		 * Just deal with current transaction. Others must have been
		 * registered successfully, and will be deal with automatically
		 * when the parent transaction terminates.
		 */

		try
		{
		    ((ServerOSINestedAction) nestedAction).rollback_subtransaction();  // does dispose as well!
		    nestedAction = null;
		}
		catch (Exception e)
		{
		}

		throw new TRANSACTION_ROLLEDBACK();
	    }

	    action.addChild((ServerOSINestedAction) nestedAction);
	}

	/*
	 * Always return reference to 'current' transaction.
	 */

	if (jtsLogger.logger.isTraceEnabled())
	    compareHierarchies(ctx, newElement);

	return control;
    }

    /*
     * In a single threaded environment we could walk down the hierarchy,
     * aborting any actions which are no longer valid, and creating any new
     * ones. However, in a multi-threaded environment, a thread can make a
     * call from any point in the client's hierarchy, and multiple client
     * threads can invoke the same server object. So, in general we cannot do
     * this optimisation. We must maintain the entire tree until portions of
     * it have explicitly been termined.
     *
     * Once we find the point in the new hierarchy which deviates from our
     * current representation, we begin to assemble a new subtree in much the
     * same way as we did for creating a completely new hierarchy.
     */

    /*
     * Also we would like to just register one resource to represent the entire
     * hierarchy, but this has problems: since threads can invoke operations at
     * any point in a hierarchy, we end up with multiple resources at top-level
     * for the same transaction. Each will try to commit the top-level
     * transaction!
     * In this implementation, the first to do so will garbage collect the root
     * of the hierarchy, and probably cause the subsequent ones to fail! There
     * is also the problem with many cross-process calls for the *same*
     * transaction.
     * So, we register *one* resource for each level of the hierarchy *when it
     * is required*, i.e., as the previous transaction terminates, and remove
     * terminated transaction resources when they occur. This means that at
     * top-level we only have a single resource to commit. There are the same
     * number of cross-process invocations. However, we also maintain the
     * entire hierarchy at the server, so if it makes subsequent invocations,
     * the right hierarchy gets sent out!
     */

protected synchronized ControlImple checkHierarchy (ServerTopLevelAction hier,
						   PropagationContext context)
    {
	ServerControl control = null;
	ServerResource currentAction = hier;  // top-level transaction
	int depth = context.parents.length;
	int differenceIndex = -1;  // index of the new transactions in the hierarchy

	/*
	 * Find the point at which our notion of the hierarchy deviates from
	 * the one we have just received.
	 *
	 * To get here we have already checked the id of the parent
	 * transaction, i.e., depth -1.
	 *
	 * Remember: the context hierarchy sequence *does not* include the
	 * current transaction!
	 */

	if (depth == 0)
	{
	    /*
	     * There are no transactions in the context other than the current
	     * transaction, which must therefore be top-level. We already have
	     * the control to return. However, make sure it has registered
	     * itself with the "real" transaction.
	     */

	    ServerOSITopLevelAction tx = (ServerOSITopLevelAction) hier;

	    tx.interposeResource();

	    control = tx.control();  // top-level transaction's control
	}
	else
	{
	    ServerResource nestedAction = null;

	    /*
	     * Start at -2 and work our way down the hierarchy. We
	     * use -2 since the length gives us the *number* of elements,
	     * which is 0 to n-1, and the n-1th element is the top-level
	     * transaction, which we must deal with first!
	     */

	    for (int i = depth -2; i >= 0; i--)  // don't check depth-1 as it is current action!
	    {
		nestedAction = currentAction.getChild(OTIDMap.find(context.parents[i].otid));

		if (nestedAction == null)  // point of difference, so stop trawling hierarchy
		{
		    differenceIndex = i;   // remember for later so that we can add new actions.
		    break;
		}
		else
		{
		    /*
		     * currentAction *always* points to the last known
		     * good transaction in our hierarchy.
		     */

		    currentAction = nestedAction;
		}
	    }

	    /*
	     * Do we have anything left in the sent hierarchy (other than
	     * current)? If so, add it now.
	     */

	    if (differenceIndex != -1)
	    {
		control = currentAction.control();

		Coordinator tmpCoord = null;
		Terminator tmpTerm = null;

		for (int j = differenceIndex; j >= 0; j--)
		{
		    tmpCoord = context.parents[j].coord;
		    tmpTerm = context.parents[j].term;

		    control = ServerFactory.create_subtransaction(OTIDMap.find(context.parents[j].otid),
								      tmpCoord, tmpTerm, control);

		    nestedAction = new ServerOSINestedAction(control, false);

		    if (!nestedAction.valid())
		    {
			/*
			 * Just deal with current transaction. Others must have been
			 * registered successfully, and will be deal with automatically
			 * when the parent transaction terminates.
			 */

			try
			{
			    ((ServerOSINestedAction) nestedAction).rollback();  // does dispose as well!
			    nestedAction = null;
			}
			catch (Exception e)
			{
			}

			throw new TRANSACTION_ROLLEDBACK();
		    }

		    currentAction.addChild((ServerOSINestedAction) nestedAction);
		    currentAction = nestedAction;
		}
	    }
	    else
	    {
		/*
		 * Hierarchies may be identical.
		 * Remember to check!
		 */
	    }

	    Uid currentUid = OTIDMap.find(context.current.otid);

	    /*
	     * currentAction points to the parent of the 'current'
	     * transaction, i.e., the last element in the TransIdentity
	     * structure. So, ask it if the sent hierarchy's child is
	     * one of its children.
	     */

	    nestedAction = currentAction.getChild(currentUid);

	    if (nestedAction == null)
	    {
		/*
		 * Different notion of current in sent hierarchy.
		 * So, add it to the hierarchy here.
		 */

		control = currentAction.control();

		/*
		 * Now deal with the current transaction.
		 */

		TransIdentity currentID = context.current;

		control = ServerFactory.create_subtransaction(currentUid, currentID.coord, currentID.term, control);

		nestedAction = new ServerOSINestedAction(control, true);

		if (!nestedAction.valid())
		{
		    /*
		     * Just deal with current transaction. Others must have been
		     * registered successfully, and will be deal with automatically
		     * when the parent transaction terminates.
		     */

		    try
		    {
			((ServerOSINestedAction) nestedAction).rollback();  // does dispose as well!
			nestedAction = null;
		    }
		    catch (Exception e)
		    {
		    }

		    throw new TRANSACTION_ROLLEDBACK();
		}

		currentAction.addChild((ServerOSINestedAction) nestedAction);
	    }
	    else
	    {
		/*
		 * Same current, so get its control and return it.
		 * Remember to make sure it has registered itself with
		 * the "real" transaction.
		 */

		nestedAction.interposeResource();

		control = nestedAction.control();
	    }
	}

	if (jtsLogger.logger.isTraceEnabled())
	    compareHierarchies(context, hier);

	return control;
    }

private static OSIInterposition __list = new OSIInterposition();

}
