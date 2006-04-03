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
 * $Id: DemoArjunaResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;

public class DemoArjunaResource extends com.arjuna.ArjunaOTS.ArjunaSubtranAwareResourcePOA
{

    public DemoArjunaResource ()
    {
	ORBManager.getPOA().objectIsReady(this);

	ref = ArjunaSubtranAwareResourceHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public ArjunaSubtranAwareResource getReference ()
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

	System.out.println("Registered DemoArjunaResource");
    }

    public org.omg.CosTransactions.Vote prepare_subtransaction () throws SystemException
    {
	System.out.println("DEMOARJUNARESOURCE : PREPARE_SUBTRANSACTION");

	return Vote.VoteCommit;
    }

    public void commit_subtransaction (Coordinator parent) throws SystemException
    {
	System.out.println("DEMOARJUNARESOURCE : COMMIT_SUBTRANSACTION");
    }

    public void rollback_subtransaction () throws SystemException
    {
	System.out.println("DEMOARJUNARESOURCE : ROLLBACK_SUBTRANSACTION");
    }

    public org.omg.CosTransactions.Vote prepare () throws SystemException
    {
	System.out.println("DEMOARJUNARESOURCE : PREPARE");
    
	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("DEMOARJUNARESOURCE : ROLLBACK");
    }

    public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	System.out.println("DEMOARJUNARESOURCE : COMMIT");
    }

    public void forget () throws SystemException
    {
	System.out.println("DEMOARJUNARESOURCE : FORGET");
    }

    public void commit_one_phase () throws SystemException, HeuristicHazard
    {
	System.out.println("DEMOARJUNARESOURCE : COMMIT_ONE_PHASE");
    }

    private ArjunaSubtranAwareResource ref;
 
}

