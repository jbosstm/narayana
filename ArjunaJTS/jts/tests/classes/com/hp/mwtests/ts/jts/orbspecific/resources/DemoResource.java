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
 * $Id: DemoResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import com.hp.mwtests.ts.jts.utils.ResourceTrace;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;

public class DemoResource extends org.omg.CosTransactions.ResourcePOA
{
    
    public DemoResource ()
    {
	ORBManager.getPOA().objectIsReady(this);

	ref = ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));

        trace = new ResourceTrace();
    }

    public Resource getResource ()
    {
	return ref;
    }
 
    public void registerResource () throws Unavailable, Inactive, SystemException
    {
	CurrentImple current = OTSImpleManager.current();
	Control myControl = current.get_control();
	Coordinator coord = myControl.get_coordinator();

	coord.register_resource(ref);

	if (!printThread)
	    System.out.println("Registered DemoResource");
    }

    public org.omg.CosTransactions.Vote prepare () throws SystemException
    {
	if (printThread)
	    System.out.println(Thread.currentThread());
	
	System.out.println("DEMORESOURCE : PREPARE");

        if (trace.getTrace() == ResourceTrace.ResourceTraceNone)
	    trace.setTrace(ResourceTrace.ResourceTracePrepare);
	else
	    trace.setTrace(ResourceTrace.ResourceTraceUnknown);

	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	if (printThread)
	    System.out.println(Thread.currentThread());
	
	System.out.println("DEMORESOURCE : ROLLBACK");

        if (trace.getTrace() == ResourceTrace.ResourceTraceNone)
	    trace.setTrace(ResourceTrace.ResourceTraceRollback);
	else
	{
	    if (trace.getTrace() == ResourceTrace.ResourceTracePrepare)
		trace.setTrace(ResourceTrace.ResourceTracePrepareRollback);
	    else
		trace.setTrace(ResourceTrace.ResourceTraceUnknown);
	}
    }

    public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	if (printThread)
	    System.out.println(Thread.currentThread());
	
	System.out.println("DEMORESOURCE : COMMIT");

        if (trace.getTrace() == ResourceTrace.ResourceTracePrepare)
	    trace.setTrace(ResourceTrace.ResourceTracePrepareCommit);
	else
	    trace.setTrace(ResourceTrace.ResourceTraceUnknown);
    }

    public void forget () throws SystemException
    {
	if (printThread)
	    System.out.println(Thread.currentThread());
	
	System.out.println("DEMORESOURCE : FORGET");

        if (trace.getTrace() == ResourceTrace.ResourceTracePrepare)
            trace.setTrace(ResourceTrace.ResourceTracePrepareForget);
        else if (trace.getTrace() == ResourceTrace.ResourceTracePrepareRollback)
            trace.setTrace(ResourceTrace.ResourceTracePrepareRollbackForget);
        else if (trace.getTrace() == ResourceTrace.ResourceTracePrepareCommit)
            trace.setTrace(ResourceTrace.ResourceTracePrepareCommitForget);
        else if (trace.getTrace() == ResourceTrace.ResourceTraceCommitOnePhase)
            trace.setTrace(ResourceTrace.ResourceTraceCommitOnePhaseForget);
        else if (trace.getTrace() == ResourceTrace.ResourceTracePrepareCommitHeurisiticRollback)
            trace.setTrace(ResourceTrace.ResourceTracePrepareCommitHeurisiticRollbackForget);
        else if (trace.getTrace() == ResourceTrace.ResourceTracePrepareHeuristicHazard)
            trace.setTrace(ResourceTrace.ResourceTracePrepareHeuristicHazardForget);
        else
            trace.setTrace(ResourceTrace.ResourceTraceUnknown);
    }

    public void commit_one_phase () throws HeuristicHazard, SystemException
    {
	if (printThread)
	    System.out.println(Thread.currentThread());
	
	System.out.println("DEMORESOURCE : COMMIT_ONE_PHASE");

        if (trace.getTrace() == ResourceTrace.ResourceTraceNone)
            trace.setTrace(ResourceTrace.ResourceTraceCommitOnePhase);
        else
            trace.setTrace(ResourceTrace.ResourceTraceUnknown);
    }

    public ResourceTrace getResourceTrace()
    {
        return(trace);
    }

    public static boolean printThread = false;

    private Resource ref;
    private ResourceTrace trace;
}

