/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: heuristic.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.hp.mwtests.ts.jts.utils.Util;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.ExplicitInterposition;
import com.arjuna.ats.jts.extensions.*;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;

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
