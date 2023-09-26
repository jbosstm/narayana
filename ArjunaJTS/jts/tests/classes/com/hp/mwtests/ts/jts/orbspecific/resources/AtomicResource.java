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
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.internal.jts.ORBManager;

public class AtomicResource extends org.omg.CosTransactions.ResourcePOA
{

    public AtomicResource (boolean doCommit)
    {
	ORBManager.getPOA().objectIsReady(this);
	
	shouldCommit = doCommit;

	ref = org.omg.CosTransactions.ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public Resource getReference ()
    {
	return ref;
    }
 
    public org.omg.CosTransactions.Vote prepare () throws SystemException, HeuristicMixed, HeuristicHazard 
    {
	System.out.println("ATOMIC : PREPARE");

	if (shouldCommit)
	{
	    System.out.println("\tATOMIC : VoteCommit");

	    return Vote.VoteCommit;
	}
	else
	{
	    System.out.println("\tATOMIC : VoteRollback");
	    
	    return Vote.VoteRollback;
	}
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("ATOMIC : ROLLBACK");
    }

    public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	System.out.println("ATOMIC : COMMIT");
    }

    public void forget () throws SystemException
    {
	System.out.println("ATOMIC : FORGET");
    }

    public void commit_one_phase () throws SystemException, HeuristicHazard
    {
	System.out.println("ATOMIC : COMMIT_ONE_PHASE");
    }

    private boolean shouldCommit;
    private Resource ref;
 
}