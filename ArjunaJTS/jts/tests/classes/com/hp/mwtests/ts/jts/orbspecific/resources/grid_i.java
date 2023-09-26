/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.UserException;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.ResourceHelper;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.internal.jts.ORBManager;

public class grid_i extends com.hp.mwtests.ts.jts.TestModule.gridPOA
{
    
    public grid_i (int h, int w, String markerName)
    {
	ORBManager.getPOA().objectIsReady(this, markerName.getBytes());

	m_height = h;   // set up height
	m_width = w;    // set up width
	// now allocate the 2-D array: as an array of pointers to 1-D arrays.
	m_a = new int [h][w];

	ref = ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public grid_i (int h, int w)
    {
	ORBManager.getPOA().objectIsReady(this);
	
	m_height = h;   // set up height
	m_width = w;    // set up width
	// now allocate the 2-D array: as an array of pointers to 1-D arrays.
	m_a = new int [h][w];

	ref = ResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public Resource getReference ()
    {
	return ref;
    }
 
    public int height () throws SystemException
    {
	return m_height;
    }

    public int width () throws SystemException
    {
	return m_width;
    }

    public void set (int n, int m, int value, Control cp) throws SystemException
    {
	try
	{
	    Coordinator co = cp.get_coordinator();
	    
	    if (co != null)
		co.register_resource(ref);
	    else
		System.err.println("Error - no transaction coordinator!");
	    
	    m_a[n][m] = value;
	}
	catch (UserException e)
	{
	    throw new UNKNOWN();
	}
	catch (SystemException e)
	{
	    throw new UNKNOWN();
	}
    }

    public int get (int n, int m, Control p) throws SystemException
    {
	return m_a[n][m];
    }

    public org.omg.CosTransactions.Vote prepare () throws SystemException
    {
	System.out.println("GRID : PREPARE");
    
	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("GRID : ROLLBACK");
    }

    public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	System.out.println("GRID : COMMIT");
    }

    public void forget () throws SystemException
    {
	System.out.println("GRID : FORGET");
    }

    public void commit_one_phase () throws SystemException, HeuristicHazard
    {
	System.out.println("GRID : COMMIT_ONE_PHASE");
    }

    private int m_height;  // store the height
    private int m_width;   // store the width
    private int[][] m_a;      // a 2-D array to store the grid data itself
    private Resource ref;
 
}