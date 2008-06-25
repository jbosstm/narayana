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
 * Copyright (C) 2000, 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ContextManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.context;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;

import com.arjuna.ats.jts.extensions.*;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.arjuna.template.*;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;

import com.arjuna.common.util.logging.*;

import java.io.PrintStream;
import java.util.*;
import java.lang.Object;
import org.omg.CORBA.*;

import org.omg.PortableInterceptor.*;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.utils.ThreadUtil;

import org.omg.CosTransactions.SubtransactionsUnavailable;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.InvalidControl;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;
import java.lang.OutOfMemoryError;
import java.util.EmptyStackException;
import org.omg.CORBA.ORBPackage.InvalidName;

/**
 * This class is responsible for managing the thread-to-transaction
 * context mappings.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ContextManager.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.internal.jts.context.genfail {0} caught exception: {1}
 */

public class ContextManager
{

    /**
     * @message com.arjuna.ats.internal.jts.context.picreffail Failed when getting a reference to PICurrent.
     */

    public ContextManager ()
    {	
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::ContextManager ()");
	}

	try
	{
	    _piCurrent = org.omg.PortableInterceptor.CurrentHelper.narrow(ORBManager.getORB().orb().resolve_initial_references("PICurrent"));
	}
	catch (InvalidName ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.picreffail");
	    }

	    ex.printStackTrace();

	    throw new FatalError("ContextManager "+jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.context.picreffail")+" "+ex);
	}
	catch (Exception ex)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.picreffail");
	    }

	    ex.printStackTrace();

	    throw new FatalError("ContextManager "+jtsLogger.logMesg.getString("com.arjuna.ats.internal.jts.context.picreffail")+" "+ex);
	}
    }

    /**
     * Get the current transaction associated with the invoking thread. Do
     * not look in the PI thread data.
     *
     * Does not need to be synchronized since it is implicitly single-threaded.
     *
     * @return the context.
     */

    public ControlWrapper current (String threadId) throws SystemException
    {
	Object arg = otsCurrent.get(threadId);
	ControlWrapper wrapper = null;

	if (arg != null)
	{
	    try
	    {
		Stack hier = (Stack) arg;

		return (ControlWrapper) hier.peek();
	    }
	    catch (EmptyStackException e)
	    {
	    }
	}

	return null;
    }

    /**
     * Get the transaction for the invoking thread. If there isn't one in
     * the normal thread associated data then look in the PI implementation.
     *
     * @return the current transaction for the invoking thread.
     */

    public ControlWrapper current () throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::current ()");
	}

	Object arg = otsCurrent.get(ThreadUtil.getThreadId());
	ControlWrapper wrapper = null;

	if (arg != null)
	{
	    try
	    {
		Stack hier = (Stack) arg;

		wrapper = (ControlWrapper) hier.peek();
	    }
	    catch (EmptyStackException e)
	    {
		e.printStackTrace();
	    }
	}

	/*
	 * If we do not have a context currently, then check to see if
	 * we have just been spawned to handle a transactional invocation.
	 * If so, there may be a context handle associated with this
	 * thread in piCurrent.
	 *
	 * We only do this for the current thread, hence the difference
	 * between the two versions of ContextManager.current.
	 */

	if (wrapper == null)
	{
	    wrapper = currentPIContext();

	    try
	    {
		if (wrapper != null)
		{
		    pushAction(wrapper);
		}
	    }
	    catch (Throwable ex)
	    {
		if (jtsLogger.loggerI18N.isWarnEnabled())
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.genfail",
					      new Object[] { "ContextManager.current", ex} );
		}

		ex.printStackTrace();
		
		throw new BAD_OPERATION();
	    }
	}

	return wrapper;
    }

    public final ControlWrapper popAction (String threadId)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::popAction ()");
	}

	ControlWrapper action = null;
	Object arg = otsCurrent.get(threadId);

	if (arg != null)
	{
	    Stack sl = (Stack) arg;

	    try
	    {
		/*
		 * When we pushed the action we did the check for whether
		 * it was local to save time now.
		 */

		action = (ControlWrapper) sl.pop();
	    }
	    catch (EmptyStackException e)
	    {
	    }
	
	    /*
	     * If size now zero we can delete from thread
	     * specific data.
	     */
	
	    if (sl.size() == 0)
	    {
		sl = null;

		otsCurrent.remove(threadId);

		disassociateContext();
	    }
	}
	
	/*
	 * Now update action in thread's notion of current if
	 * this action is local.
	 */
    
	// Check that action is local and not a proxy.

	if (action != null)
	{
	    /*
	     * Now update action in thread's notion of current if
	     * this action is local.
	     */
    
	    // Check that action is local and not a proxy.

	    if (action.isLocal())
	    {
		/*
		 * If transaction is terminated by another thread
		 * then our thread-action information may have already
		 * been removed from the action.
		 */

		try
		{
		    ThreadActionData.popAction(threadId);
		}
		catch (EmptyStackException e)
		{
		}
	    }
	}
	
	return action;
    }

    public final ControlWrapper popAction ()
    {
        return popAction(ThreadUtil.getThreadId());
    }

    public final void purgeActions (String threadId)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::purgeActions ()");
	}

	/*
	 * Don't do anything with these actions, i.e., do
	 * not commit/abort them. Just because this thread is
	 * finished with them does not mean other threads
	 * are!
	 */
	
	ControlWrapper ptr = popAction(threadId);

	while (ptr != null)
	{
	    ptr = null;

	    ptr = popAction(threadId);
	    
	} while (ptr != null);
    }

    public final void purgeActions ()
    {
        purgeActions(ThreadUtil.getThreadId());
    }

    /**
     * Force the thread-to-transaction association. Applications should not use
     * this method.
     *
     * @since JTS 2.1.1.
     */

    public void associate () throws SystemException 
    {
	current();
    }

    /**
     * We could maintain a list of suspended action hierarchies and resume
     * the right one (and the right place!) given the control. However, this
     * can lead to memory leaks, since we never know when to remove this
     * hierarchy information. So, for now we simply rely on the propagation
     * context.
     */

    public final boolean addRemoteHierarchy (Control cont)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::addRemoteHierarchy ()");
	}

	/*
	 * Here until we can make this work with recreate.
	 */

	if (false)
	{
	    pushAction(new ControlWrapper(cont));

	    return true;
	}
	else
	{
	    boolean isError = false;
    
	    try
	    {
		Coordinator coord = cont.get_coordinator();
		PropagationContext ctx = coord.get_txcontext();

		if (ctx != null)
		{
		    /*
		     * Depth must be non-zero or we wouldn't be here!
		     */
	    
		    int depth = ctx.parents.length;

		    for (int i = depth -1; i >= 0; i--)
		    {
			/*
			 * No memory leak as we delete either when suspend
			 * is called, or the transaction is terminated.
			 */

			Coordinator tmpCoord = ctx.parents[i].coord;
			Terminator tmpTerm = ctx.parents[i].term;

			Control theControl = TransactionFactoryImple.createProxy(tmpCoord, tmpTerm);
		
			pushAction(new ControlWrapper(theControl));  // takes care of thread/BasicAction for us.
		    }
		    
		    ctx = null;
		}
		else
		{
		    /*
		     * If we can't get a propagation context then we cannot
		     * create the hierarchy!
		     */
	    
		    isError = true;
		}

		coord = null;
	    }
	    catch (Exception e)
	    {
		isError = true;
	    }

	    return isError;
	}
    }

    /*
     * All OTSArjuna controls have a method for getting their parent.
     */

    public final boolean addActionControlHierarchy (ActionControl cont)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::addActionControlHierarchy ()");
	}

	boolean isError = false;
    
	try
	{
	    ActionControl actControl = cont;
	    Control parentControl = actControl.getParentControl();
	    Stack hier = new Stack();

	    while (parentControl != null)
	    {
		hier.push(new ControlWrapper(parentControl));

		actControl = com.arjuna.ArjunaOTS.ActionControlHelper.narrow(parentControl);

		/*
		 * Currently assume that entire hierarchy will contain only one
		 * type of action, i.e., Arjuna actions or someone elses!
		 */

		if (actControl != null)
		    parentControl = actControl.getParentControl();
		else
		    parentControl = null;
	    }

	    actControl = null;

	    try
	    {
		ControlWrapper wrapper = (ControlWrapper) hier.pop();
	    
		while (wrapper != null)
		{
		    pushAction(wrapper);
		
		    wrapper = null;
		
		    wrapper = (ControlWrapper) hier.pop();
		}
	    }
	    catch (EmptyStackException e)
	    {
	    }
	}
	catch (Exception e)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.genfail",
					  new Object[] { "ContextManager.addActionControlHierarchy", e} );
	    }

	    e.printStackTrace();
	    
	    isError = true;
	}

	return isError;
    }

    /*
     * Given a ControlWrapper we can create the hierarchy quickly, since
     * we have the implementation information to hand.
     */

    public final boolean addControlImpleHierarchy (ControlImple which)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::addControlImpleHierarchy ()");
	}

	boolean isError = false;
    
	try
	{
	    ControlImple curr = which.getParentImple();
	    Stack hier = new Stack();

	    while (curr != null)
	    {
		hier.push(new ControlWrapper(curr));

		curr = curr.getParentImple();
	    }

	    try
	    {
		ControlWrapper wrapper = (ControlWrapper) hier.pop();
	    
		while (wrapper != null)
		{
		    pushAction(wrapper);
		
		    wrapper = null;
		
		    wrapper = (ControlWrapper) hier.pop();
		}
	    }
	    catch (EmptyStackException e)
	    {
	    }
	}
	catch (Exception e)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.genfail",
					  new Object[] { "ContextManager.addActionControlImple", e} );
	    }
	    
	    e.printStackTrace();
	    
	    isError = true;
	}

	return isError;
    }
    
    /**
     * If we have a hierarchy of remote actions (controls) then they will not
     * be registered with BasicAction.
     * Also, they will either all be remote references to controls, or all but
     * the current action will be proxy/wrapper controls, i.e., controls which
     * contain references to the remote coordinator/terminator.
     */

    public final void pushAction (ControlWrapper action)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "ContextManager::pushAction ()");
	}

	final String threadId = ThreadUtil.getThreadId() ;
	Stack sl = (Stack) otsCurrent.get(threadId);
	boolean isNew = false;
	
	if (sl == null)
	{
	    isNew = true;
	    sl = new Stack();
	}

	// Check here that action is local and not a proxy.

	/*
	 * If it's a local transaction then save the transaction
	 * pointer. We'll need it when we pop the transaction
	 * later.
	 */

	if (action != null)
	    action.determineLocality();

	/*
	 * Doesn't need to be synchronized since only this thread
	 * can play with its own stack!
	 */

	sl.push(action);

	if (isNew)
	    otsCurrent.put(threadId, sl);

	associateContext();

	if (action.isLocal())
	{
	    /*
	     * Add thread to action list!
	     */

	    /*
	     * Given a Control we can maintain a mapping to the
	     * actual action.
	     *
	     * Do we want this to work for remote actions? Yes, because
	     * we want all actions to know about active threads, even
	     * those that are remote. (But we don't do it yet!)
	     *
	     * Call action to increment number of threads. This is all we
	     * need to do for remote actions. If local, we need to make this
	     * action the current action.
	     */

	    ThreadActionData.pushAction(action.getImple().getImplHandle());
	}
    }

    public ControlWrapper currentPIContext () throws SystemException
    {
	if (_piCurrent != null)
	{
	    try
	    {
		int slotId = OTSManager.getReceivedSlotId();
		
		if (slotId == -1)
		    return null;

		org.omg.CORBA.Any ctx = _piCurrent.get_slot(slotId);

		/*
		 * If we have something then we must be a server thread.
		 * In which case we save the thread id so that the server
		 * interceptor can do the suspend when the call returns.
		 */

		if (ctx.type().kind().value() != TCKind._tk_null)
		{
		    ControlWrapper control = null;
		    
		    // Is this just a Coordinator, or a full blown context?

		    if (ctx.type().kind().value() == TCKind._tk_string)
		    {
			control = createProxy(ctx);
		    }
		    else
		    {
			control = createHierarchy(ctx);
		    }

		    org.omg.CORBA.Any threadData = ORBManager.getORB().orb().create_any();

		    threadData.insert_string(ThreadUtil.getThreadId());

		    _piCurrent.set_slot(slotId, threadData);

		    return control;
		}
		else
		    return null;
	    }
	    catch (NullPointerException e)
	    {
		// slot not set.

		return null;
	    }
	    catch (InvalidSlot is)
	    {
		// Something very wrong
		
		throw new org.omg.CORBA.INTERNAL();
	    }
	}
	else
	    return null;
    }

    /**
     * @message com.arjuna.ats.internal.jts.context.coorref {0} expected a Coordinator reference and did not get one: {1}
     */

    public final ControlWrapper createProxy (org.omg.CORBA.Any ctx) throws SystemException
    {
	String stringRef = null;
	
	try
	{
	    stringRef = ctx.extract_string();

	    /*
	     * Is this a thread id or an IOR? If the latter then use it,
	     * otherwise ignore it as:
	     *
	     * (i) this thread has been re-used before our filter has had a
	     * chance to remove the threading information from the slot. This
	     * will happen later.
	     *
	     * or
	     *
	     * (ii) the thread is calling back into itself to setup the
	     * BasicAction structure.
	     *
	     * Either way we can safely ignore.
	     */

	    if (stringRef.startsWith(IORTag))
	    {
		org.omg.CORBA.Object obj = ORBManager.getORB().orb().string_to_object(stringRef);
		Coordinator theCoordinator = org.omg.CosTransactions.CoordinatorHelper.narrow(obj);
	    
		if (theCoordinator == null)
		    throw new BAD_PARAM();

		return new ControlWrapper(TransactionFactoryImple.createProxy(theCoordinator, null));
	    }
	    else
		return null;
	}
	catch (BAD_PARAM e1)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.genfail",
					  new Object[] { "ContextManager", stringRef} );
	    }

	    e1.printStackTrace();
	}
	catch (Exception e2)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.genfail",
					  new Object[] { "ContextManager", e2} );
	    }

	    throw new UNKNOWN(e2.toString());
	}

	return null;
    }

    public final ControlWrapper createHierarchy (org.omg.CORBA.Any ctx) throws SystemException
    {
	if (ctx != null)
	{
	    try
	    {
		PropagationContext theContext = org.omg.CosTransactions.PropagationContextHelper.extract(ctx);
	    
		if (OTSImpleManager.localFactory())
		{
		    TransactionFactoryImple theFactory = OTSImpleManager.factory();

		    return new ControlWrapper(theFactory.recreateLocal(theContext));
		}
		else
		{
		    TransactionFactory theFactory = OTSImpleManager.get_factory();
		    
		    return new ControlWrapper(theFactory.recreate(theContext));
		}
	    }
	    catch (SystemException ex)
	    {
		if (jtsLogger.loggerI18N.isWarnEnabled())
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.genfail",
					      new Object[] { "ContextManager.createHierarchy", ex} );
		}

		throw ex;
	    }
	    catch (Exception e)
	    {
		if (jtsLogger.loggerI18N.isWarnEnabled())
		{
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.context.genfail",
					      new Object[] { "ContextManager.createHierarchy", e} );
		}

		e.printStackTrace();
		
		throw new UNKNOWN();
	    }
	}
	else
	    return null;
    }

    private final void associateContext () throws SystemException
    {
	if (_piCurrent != null)
	{
	    try
	    {
		int slotId = OTSManager.getLocalSlotId();
		
		if (slotId != -1)
		{
		    org.omg.CORBA.Any localDataAny = ORBManager.getORB().orb().create_any();

		    localDataAny.insert_string(ThreadUtil.getThreadId());

		    _piCurrent.set_slot(slotId, localDataAny);
		}
	    }
	    catch (InvalidSlot is)
	    {
		// Something very wrong

		throw new org.omg.CORBA.INTERNAL();
	    }
	}
    }

    private final void disassociateContext () throws SystemException
    {
	if (_piCurrent != null)
	{
	    try
	    {
		int slotId = OTSManager.getLocalSlotId();
		
		if (slotId != -1)
		{
		    _piCurrent.set_slot(slotId, null);
		}
	    }
	    catch (InvalidSlot is)
	    {
		// Something very wrong

		throw new org.omg.CORBA.INTERNAL();
	    }
	}
    }

    private Hashtable otsCurrent = new Hashtable();

    private org.omg.PortableInterceptor.Current _piCurrent = null;

    private static final String IORTag = "IOR";
    
}
