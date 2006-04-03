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
 * $Id: setget_i.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.hp.mwtests.ts.jts.TestModule.*;
import com.hp.mwtests.ts.jts.utils.Util;

import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;

import com.arjuna.ats.jts.ExplicitInterposition;
import com.arjuna.ats.jts.extensions.*;

import com.arjuna.orbportability.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;

public class setget_i extends com.hp.mwtests.ts.jts.TestModule.SetGetPOA
{

    public setget_i ()
    {
	ORBManager.getPOA().objectIsReady(this);
	
	value = 0;

	ref = SetGetHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public setget_i (String name)
    {
	ORBManager.getPOA().objectIsReady(this, name.getBytes());

	value = 0;

	ref = SetGetHelper.narrow(ORBManager.getPOA().corbaReference(this));
    }

    public SetGet getReference ()
    {
	return ref;
    }

    public void set (short n, Control control) throws SystemException
    {
	System.out.println("setget_i.set "+n);

	try
	{
	    if (control != null)
	    {
		ExplicitInterposition manager = new ExplicitInterposition();
		
		manager.registerTransaction(control);

		System.out.println("setget_i.set - managed to set up interposition hierarchy");
    
		CurrentImple current = OTSImpleManager.current();
		Control cont = current.get_control();

		if (cont == null)
		    System.err.println("setget_i.set error - current returned no control!");
		else
		{
		    System.out.println("setget_i.set - current returned a control!");

		    cont = null;
		}
		
		System.out.println("setget_i.set - beginning nested action");

		current.begin();

		cont = current.get_control();

		if (cont != null)
		{
		    Coordinator coord = cont.get_coordinator();

		    System.out.println("setget_i.set - registering self");
		
		    coord.register_resource(ref);

		    coord = null;
		    cont = null;
		}
		else
		    System.err.println("setget_i.set - current did not return control after begin!");
	    
		value = n;
	    
		System.out.println("setget_i.set - committing nested action");
	
		current.commit(true);

		manager.unregisterTransaction();

		manager = null;
	    }
	    else
		System.err.println("setget_i::set error - no control!");
	}
	catch (InterpositionFailed ex)
	{
	    System.err.println("setget_i.set - error in setting up hierarchy");

	    throw new UNKNOWN();
	}
	catch (Throwable e)
	{
	    System.err.println("setget_i::set - caught exception: "+e);
	}

	System.out.println("setget_i.set - finished");
    }

    public short get (Control p) throws SystemException
    {
	return value;
    }

    public void commit_subtransaction (Coordinator parent) throws SystemException
    {
	System.out.println("SETGET_I : COMMIT_SUBTRANSACTION");
    }

    public void rollback_subtransaction () throws SystemException
    {
	System.out.println("SETGET_I : ROLLBACK_SUBTRANSACTION");
    }

    public org.omg.CosTransactions.Vote prepare () throws SystemException
    {
	System.out.println("SETGET_I : PREPARE");
    
	return Vote.VoteCommit;
    }

    public void rollback () throws SystemException, HeuristicCommit, HeuristicMixed, HeuristicHazard
    {
	System.out.println("SETGET_I : ROLLBACK");
    }

    public void commit () throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
	System.out.println("SETGET_I : COMMIT");
    }

    public void forget () throws SystemException
    {
	System.out.println("SETGET_I : FORGET");
    }

    public void commit_one_phase () throws SystemException, HeuristicHazard
    {
	System.out.println("SETGET_I : COMMIT_ONE_PHASE");
    }

    private short value;
    private SetGet ref;
 
}

