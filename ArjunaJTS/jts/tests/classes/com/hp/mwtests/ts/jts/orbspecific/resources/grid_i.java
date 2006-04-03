/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * $Id: grid_i.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.hp.mwtests.ts.jts.utils.Util;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;

import com.arjuna.ats.jts.OTSManager;
import com.arjuna.ats.jts.ExplicitInterposition;
import com.arjuna.ats.jts.extensions.*;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CORBA.UNKNOWN;

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

