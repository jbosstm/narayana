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
 * $Id: DemoSubTranResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;

import com.arjuna.orbportability.*;
import com.hp.mwtests.ts.jts.utils.ResourceTrace;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;

public class DemoSubTranResource extends org.omg.CosTransactions.SubtransactionAwareResourcePOA
{
    
    public DemoSubTranResource ()
    {
	ORBManager.getPOA().objectIsReady(this);

	ref = SubtransactionAwareResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));

        trace = new ResourceTrace();

	numSubtransactionsRolledback = 0;
	numSubtransactionsCommitted = 0;
    }

    public SubtransactionAwareResource getReference ()
    {
	return ref;
    }
 
    public void registerResource (boolean registerSubtran) throws Unavailable, Inactive, NotSubtransaction, SystemException
    {
	CurrentImple current = OTSImpleManager.current();
	Control myControl = current.get_control();
	Coordinator coord = myControl.get_coordinator();
	
	if (registerSubtran)
	    coord.register_subtran_aware(ref);
	else
	    coord.register_resource(ref);
	
	System.out.println("Registered DemoSubTranResource");
    }

    public void commit_subtransaction (Coordinator parent) throws SystemException
    {
        numSubtransactionsCommitted++;
	System.out.println("DEMOSUBTRANRESOURCE : COMMIT_SUBTRANSACTION");
    }

    public void rollback_subtransaction () throws SystemException
    {
	System.out.println("DEMOSUBTRANRESOURCE : ROLLBACK_SUBTRANSACTION");
        numSubtransactionsRolledback++;
    }

    public org.omg.CosTransactions.Vote prepare () throws SystemException
    {
	System.out.println("DEMOSUBTRANRESOURCE : PREPARE");

        if (trace.getTrace() == ResourceTrace.ResourceTraceNone)
	    trace.setTrace(ResourceTrace.ResourceTracePrepare);
	else
	    trace.setTrace(ResourceTrace.ResourceTraceUnknown);

	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("DEMOSUBTRANRESOURCE : ROLLBACK");

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
	System.out.println("DEMOSUBTRANRESOURCE : COMMIT");

        if (trace.getTrace() == ResourceTrace.ResourceTracePrepare)
	    trace.setTrace(ResourceTrace.ResourceTracePrepareCommit);
	else
	    trace.setTrace(ResourceTrace.ResourceTraceUnknown);
    }

    public void forget () throws SystemException
    {
	System.out.println("DEMOSUBTRANRESOURCE : FORGET");

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
	System.out.println("DEMOSUBTRANRESOURCE : COMMIT_ONE_PHASE");

        if (trace.getTrace() == ResourceTrace.ResourceTraceNone)
            trace.setTrace(ResourceTrace.ResourceTraceCommitOnePhase);
        else
            trace.setTrace(ResourceTrace.ResourceTraceUnknown);
    }

    public int getNumberOfSubtransactionsCommitted()
    {
        return(numSubtransactionsCommitted);
    }

    public int getNumberOfSubtransactionsRolledBack()
    {
        return(numSubtransactionsRolledback);
    }

    public ResourceTrace getResourceTrace()
    {
        return(trace);
    }

    private SubtransactionAwareResource ref;
    private ResourceTrace trace;

    private int numSubtransactionsCommitted;
    private int numSubtransactionsRolledback;
}

