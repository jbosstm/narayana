/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourceHelper;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.internal.jts.ORBManager;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;

public class heuristic extends org.omg.CosTransactions.ResourcePOA
{
    
    public heuristic (boolean p)
    {
	ORBManager.getPOA().objectIsReady(this);

        trace = new ResourceTrace();

	heuristicPrepare = p;

	ref = ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public Resource getReference ()
    {
	return ref;
    }

    public ResourceTrace getTrace()
    {
        return trace;
    }

    public org.omg.CosTransactions.Vote prepare () throws HeuristicMixed, HeuristicHazard, SystemException
    {
	System.out.println("HEURISTIC : PREPARE");

        if (trace.getTrace() == ResourceTrace.ResourceTraceNone)
	    trace.setTrace(ResourceTrace.ResourceTracePrepare);
	else
	    trace.setTrace(ResourceTrace.ResourceTraceUnknown);

	if (heuristicPrepare)
	{
	    System.out.println("HEURISTIC : throwing HeuristicHazard");

            if (trace.getTrace() == ResourceTrace.ResourceTracePrepare)
	    	trace.setTrace(ResourceTrace.ResourceTracePrepareHeuristicHazard);
	    else
	    	trace.setTrace(ResourceTrace.ResourceTraceUnknown);

	    throw new HeuristicHazard();
	}

	System.out.println("\tHEURISTIC : VoteCommit");
    
	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("HEURISTIC : ROLLBACK");

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
	System.out.println("HEURISTIC : COMMIT");

        if (trace.getTrace() == ResourceTrace.ResourceTracePrepare)
	    trace.setTrace(ResourceTrace.ResourceTracePrepareCommit);
	else
	    trace.setTrace(ResourceTrace.ResourceTraceUnknown);

	if (!heuristicPrepare)
	{
	    System.out.println("HEURISTIC : throwing HeuristicRollback");

            if (trace.getTrace() == ResourceTrace.ResourceTracePrepareCommit)
		trace.setTrace(ResourceTrace.ResourceTracePrepareCommitHeurisiticRollback);
	    else
		trace.setTrace(ResourceTrace.ResourceTraceUnknown);

	    throw new HeuristicRollback();
	}
    }

    public void forget () throws SystemException
    {
	System.out.println("HEURISTIC : FORGET");

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

    public void commit_one_phase () throws SystemException, HeuristicHazard
    {
	System.out.println("HEURISTIC : COMMIT_ONE_PHASE");

        if (trace.getTrace() == ResourceTrace.ResourceTraceNone)
            trace.setTrace(ResourceTrace.ResourceTraceCommitOnePhase);
        else
            trace.setTrace(ResourceTrace.ResourceTraceUnknown);
    }

    private boolean heuristicPrepare;
    private Resource ref;
    private ResourceTrace trace;
}