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

public class tranobject_i extends org.omg.CosTransactions.ResourcePOA
{
    
    public tranobject_i ()
    {
	ORBManager.getPOA().objectIsReady(this);

	ref = ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public Resource getReference ()
    {
	return ref;
    }
 
    public org.omg.CosTransactions.Vote prepare () throws SystemException
    {
	System.out.println("TRANOBJECT : PREPARE");
    
	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("TRANOBJECT : ROLLBACK");
    }

    public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	System.out.println("TRANOBJECT : COMMIT");
    }

    public void forget () throws SystemException
    {
	System.out.println("TRANOBJECT : FORGET");
    }

    public void commit_one_phase () throws SystemException, HeuristicHazard
    {
	System.out.println("TRANOBJECT : COMMIT_ONE_PHASE");
    }
    
    private Resource ref;

}