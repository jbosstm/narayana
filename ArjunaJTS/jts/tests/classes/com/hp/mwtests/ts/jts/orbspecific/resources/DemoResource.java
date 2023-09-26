/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourceHelper;
import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;

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