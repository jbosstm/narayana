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
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.TestModule.TranGrid;
import com.hp.mwtests.ts.jts.TestModule.TranGridHelper;

public class trangrid_i extends com.hp.mwtests.ts.jts.TestModule.TranGridPOA
{

    public trangrid_i (short h, short w)
    {
	ORBManager.getPOA().objectIsReady(this);
	
	m_height = h;   // set up height
	m_width = w;    // set up width
	// now allocate the 2-D array: as an array of pointers to 1-D arrays.
	m_a = new short [h][w];

	ref = TranGridHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public trangrid_i (short h, short w, String objectName)
    {
	ORBManager.getPOA().objectIsReady(this, objectName.getBytes());
	
	m_height = h;   // set up height
	m_width = w;    // set up width
	// now allocate the 2-D array: as an array of pointers to 1-D arrays.
	m_a = new short [h][w];

	ref = TranGridHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public TranGrid getReference ()
    {
	return ref;
    }
 
    public short height () throws SystemException
    {
	System.out.println("height "+Thread.currentThread());

	CurrentImple current = OTSImpleManager.current();
	org.omg.CosTransactions.Control control = current.get_control();
	org.omg.CosTransactions.Control ptr = control;

	if (ptr != null)
	{
	    System.out.println("trangrid_i.height - found implicit transactional context.");

	    ptr = null;
	}
	else
	{
	    System.err.println("Error: trangrid_i.height - no implicit transactional context.");

	    System.exit(0);
	}
    
	return m_height;
    }

    public short width () throws SystemException
    {
	CurrentImple current = OTSImpleManager.current();
	org.omg.CosTransactions.Control control = current.get_control();
	org.omg.CosTransactions.Control ptr = control;

	if (ptr != null)
	{
	    System.out.println("trangrid_i.width - found implicit transactional context.");

	    ptr = null;
	}
	else
	{
	    System.err.println("Error: trangrid_i.width - no implicit transactional context.");

	    System.exit(0);
	}
    
	return m_width;
    }

    public void set (short n, short m, short value) throws SystemException
    {
	CurrentImple current = OTSImpleManager.current();
	org.omg.CosTransactions.Control control = current.get_control();

	if (control != null)
	{
	    System.out.println("trangrid_i.set - found implicit transactional context!");

	    try
	    {
		org.omg.CosTransactions.Coordinator co = control.get_coordinator();

		co.register_resource(ref);
	    }
	    catch (Exception e)
	    {
		System.err.println("trangrid_i.set - caught exception: "+e);
	    }
	}
	else
	    System.err.println("Error - set has no transaction control, therefore will ignore!");
	
	m_a[n][m] = value;
    }

    public short get (short n, short m) throws SystemException
    {
	return m_a[n][m];
    }

    public org.omg.CosTransactions.Vote prepare () throws SystemException, HeuristicMixed, HeuristicHazard
    {
	System.out.println("TRANGRID : PREPARE");
    
	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("TRANGRID : ROLLBACK");
    }

    public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	System.out.println("TRANGRID : COMMIT");
    }

    public void forget () throws SystemException
    {
	System.out.println("TRANGRID : FORGET");
    }
    
    public void commit_one_phase () throws SystemException, HeuristicHazard
    {
	System.out.println("TRANGRID : COMMIT_ONE_PHASE");
    }

    private short m_height;  // store the height
    private short m_width;   // store the width
    private short[][] m_a;   // a 2-D array to store the grid data itself
    private TranGrid ref;
 
}