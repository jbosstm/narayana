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
 * $Id: Interposition.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition.resources.arjuna;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.utils.Utility;
import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.interposition.ServerFactory;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.*;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import com.arjuna.ArjunaOTS.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

import java.util.List;
import java.util.LinkedList;

public class Interposition
{

    public Interposition ()
    {
        _head = new LinkedList(); // not synchronized as the methods that access it all are synchronized.
    }

public static ControlImple create (PropagationContext context) throws SystemException
    {
	if (__list != null)
	    return __list.setupHierarchy(context);
	else
	    return null;
    }

public static boolean destroy (Uid act)
    {
	if (__list != null)
	    return __list.removeHierarchy(act);
	else
	    return false;
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

	proxyAction = present(theUid);

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
	    proxyAction = null;
	}

	return controlPtr;
    }

    protected final synchronized ServerTopLevelAction present (Uid actUid)
    {
        if(_head == null) {
            return null;
        }

        for(ServerTopLevelAction action : _head) {
            if(actUid.equals(action.get_uid())) {
                return action;
            }
        }

        return null;
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

	if (tmpCoord == null)  // terminator may correctly be null
	{
	    return null;
	}

	ServerControl control = ServerFactory.create_transaction(tlUid, null, null,
								 tmpCoord, tmpTerm, ctx.timeout);

	action = new ServerTopLevelAction(control);

	if (!action.valid())
	{
	    try
	    {
		((ServerTopLevelAction) action).rollback();  // does dispose as well!
	    }
	    catch (Exception e)
	    {
	    }

	    throw new TRANSACTION_ROLLEDBACK();
	}

	ServerTopLevelAction newElement = (ServerTopLevelAction)action;

	_head.add(newElement);

	if (depth > 0) // current is a nested transaction
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

		control = ServerFactory.create_subtransaction(Utility.otidToUid(ctx.parents[i].otid),
								  tmpCoord, tmpTerm, control);

		nestedAction = new ServerNestedAction(control);

		if (!nestedAction.valid())
		{
		    /*
		     * Just deal with current transaction. Others must have been
		     * registered successfully, and will be deal with automatically
		     * when the parent transaction terminates.
		     */

		    try
		    {
			((ServerNestedAction) nestedAction).rollback_subtransaction();  // does dispose as well!
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

		action.addChild((ServerNestedAction) nestedAction);
		action = nestedAction;
	    }

	    /*
	     * Now deal with current transaction. If there is
	     * only one transaction we do nothing.
	     */

	    tmpCoord = ctx.current.coord;
	    tmpTerm = ctx.current.term;

	    control = ServerFactory.create_subtransaction(Utility.otidToUid(ctx.current.otid),
							      tmpCoord, tmpTerm, control);

	    nestedAction = new ServerNestedAction(control);

	    if (!nestedAction.valid())
	    {
		/*
		 * Just deal with current transaction. Others must have been
		 * registered successfully, and will be deal with automatically
		 * when the parent transaction terminates.
		 */

		try
		{
		    ((ServerNestedAction) nestedAction).rollback_subtransaction();  // does dispose as well!
		    nestedAction = null;
		}
		catch (Exception e)
		{
		}

		throw new TRANSACTION_ROLLEDBACK();
	    }

	    action.addChild((ServerNestedAction) nestedAction);
	}

	if (jtsLogger.logger.isDebugEnabled())
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

protected synchronized ControlImple checkHierarchy (ServerTopLevelAction hier,
						   PropagationContext context) throws SystemException
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
	     * the control to return.
	     */

	    control = hier.control();  // top-level transaction's control
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
		nestedAction = currentAction.getChild(Utility.otidToUid(context.parents[i].otid));

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

		Coordinator tmpCoord;
		Terminator tmpTerm;

		for (int j = differenceIndex; j >= 0; j--)
		{
		    tmpCoord = context.parents[j].coord;
		    tmpTerm = context.parents[j].term;

		    control = ServerFactory.create_subtransaction(Utility.otidToUid(context.parents[j].otid),
								      tmpCoord, tmpTerm, control);
		    nestedAction = new ServerNestedAction(control);

		    if (!nestedAction.valid())
		    {
			/*
			 * Just deal with current transaction. Others must have been
			 * registered successfully, and will be deal with automatically
			 * when the parent transaction terminates.
			 */

			try
			{
			    ((ServerNestedAction) nestedAction).rollback();  // does dispose as well!
			    nestedAction = null;
			}
			catch (Exception e)
			{
			}

			throw new TRANSACTION_ROLLEDBACK();
		    }

		    currentAction.addChild((ServerNestedAction) nestedAction);
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

	    Uid currentUid = Utility.otidToUid(context.current.otid);

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

		control = ServerFactory.create_subtransaction(currentUid,
								  currentID.coord, currentID.term, control);
		nestedAction = new ServerNestedAction(control);

		if (!nestedAction.valid())
		{
		    /*
		     * Just deal with current transaction. Others must have
		     * been registered successfully, and will be deal with
		     * automatically when the parent transaction terminates.
		     */

		    try
		    {
			((ServerNestedAction) nestedAction).rollback();  // does dispose as well!
			nestedAction = null;
		    }
		    catch (Exception e)
		    {
		    }

		    throw new TRANSACTION_ROLLEDBACK();
		}

		currentAction.addChild((ServerNestedAction) nestedAction);
	    }
	    else
	    {
		/*
		 * Same current, so get its control and return it.
		 */

		control = nestedAction.control();
	    }
	}

	if (jtsLogger.logger.isDebugEnabled())
	    compareHierarchies(context, hier);

	return control;
    }

    /**
     * @message com.arjuna.ats.internal.jts.interposition.resources.arjuna.ipfail {0} - could not find {1} to remove.
     */

    protected final synchronized boolean removeHierarchy (Uid theUid)
    {
        ServerTopLevelAction action = present(theUid);

        if (action != null)
        {
            _head.remove(action);
            return true;
        }
        else
        {
            if (jtsLogger.logger.isDebugEnabled())
            {
                if (jtsLogger.loggerI18N.isWarnEnabled())
                {
                    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.resources.arjuna.ipfail",
                            new Object[] {"Interposition.removeHeirarchy", theUid} );
                }
            }
        }

        return false;
    }

    /**
     * @message com.arjuna.ats.internal.jts.interposition.resources.arjuna.iptl TopLevel transactions not identical: {0} {1}
     * @message com.arjuna.ats.internal.jts.interposition.resources.arjuna.ipnt Nested transactions not identical.
     * @message com.arjuna.ats.internal.jts.interposition.resources.arjuna.ipnull Interposed hierarchy is null!
     */

protected final void compareHierarchies (PropagationContext ctx, ServerTopLevelAction action)
    {
	int depth = ctx.parents.length;
	Uid[] ctxHierarchy = new Uid [depth+1];
	boolean printHierarchies = false;

	for (int i = depth -1; i >= 0; i--)
	{
	    ctxHierarchy[i+1] = new Uid(Utility.otidToUid(ctx.parents[i].otid));
	}

	ctxHierarchy[0] = new Uid(Utility.otidToUid(ctx.current.otid));

	boolean problem = false;

	if (action != null)
	{
	    if (action.get_uid().notEquals(ctxHierarchy[depth]))
	    {
		if (jtsLogger.loggerI18N.isWarnEnabled())
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.resources.arjuna.iptl",
					      new Object[] {action.get_uid(), ctxHierarchy[depth-1]} );
		}

		printHierarchies = true;
	    }
	    else
	    {
		if (depth > 0)
		{
		    ServerNestedAction child = action.getChild(ctxHierarchy[depth-1]);

		    if (child != null)
		    {
			int i = 0;

			for (i = depth -2; (i >= 0) && (child != null); i--)
			{
			    child = child.getChild(ctxHierarchy[i]);

			    if (child == null)
			    {
				problem = true;
				break;
			    }
			}

			if (i != -1)
			    problem = true;
		    }
		    else
			problem = true;
		}

		if (problem)
		{
		    if (jtsLogger.loggerI18N.isWarnEnabled())
		    {
			jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.resources.arjuna.ipnt");
		    }

		    printHierarchies = true;
		}
	    }
	}
	else
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.interposition.resources.arjuna.ipnull");
	    }

	    printHierarchies = true;
	}

	if (!printHierarchies)
	    printHierarchies = jtsLogger.logger.isDebugEnabled();

	if (printHierarchies)
	{
	    synchronized (jtsLogger.logger)
	    {
		if (!problem)
		{
		    if (jtsLogger.logger.isDebugEnabled())
		    {
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, Utility.getHierarchy(ctx));
		    }

		    if (jtsLogger.logger.isDebugEnabled())
		    {
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					       com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, hierarchytoString(action));
		    }
		}
		else
		{
		    if (jtsLogger.logger.isWarnEnabled())
		    {
			jtsLogger.logger.warn(Utility.getHierarchy(ctx));
		    }

		    if (jtsLogger.logger.isWarnEnabled())
		    {
			jtsLogger.logger.warn(hierarchytoString(action));
		    }
		}
	    }
	}

	if (ctxHierarchy != null)
	{
	    for (int i = 0; i < (int) depth; i++)
	    {
		if (ctxHierarchy[i] != null)
		    ctxHierarchy[i] = null;
	    }

	    ctxHierarchy = null;
	}
    }

    private final String hierarchytoString(ServerTopLevelAction action)
    {
        String hier = "InterposedHierarchy:";

        if (action != null)
        {
            hier += action.get_uid();

            List<ServerNestedAction> children = action.getChildren();

            synchronized (children) {
                for(ServerNestedAction child : children) {
                    hier += "\n"+child.get_uid();
                    hier += child.getChildren(2);
                }
            }
        }
        else
            hier += "EMPTY";

        return hier;
    }

protected List<ServerTopLevelAction> _head;

private static Interposition __list = new Interposition();

}
