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
 * $Id: AtomicResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;

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

