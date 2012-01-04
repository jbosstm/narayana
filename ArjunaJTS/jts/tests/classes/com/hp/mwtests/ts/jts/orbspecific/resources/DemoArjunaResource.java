/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
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

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.NotSubtransaction;
import org.omg.CosTransactions.Unavailable;
import org.omg.CosTransactions.Vote;

import com.arjuna.ArjunaOTS.ArjunaSubtranAwareResource;
import com.arjuna.ArjunaOTS.ArjunaSubtranAwareResourceHelper;
import com.arjuna.ArjunaOTS.OTSAbstractRecord;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

public class DemoArjunaResource extends com.arjuna.ArjunaOTS.OTSAbstractRecordPOA
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

    public void alter (OTSAbstractRecord arg0)
    {
    }

    public void merge (OTSAbstractRecord arg0)
    {
    }

    public boolean propagateOnAbort ()
    {
        return false;
    }

    public boolean propagateOnCommit ()
    {
        return false;
    }

    public boolean saveRecord ()
    {
        return false;
    }

    public boolean shouldAdd (OTSAbstractRecord arg0)
    {
        return false;
    }

    public boolean shouldAlter (OTSAbstractRecord arg0)
    {
        return false;
    }

    public boolean shouldMerge (OTSAbstractRecord arg0)
    {
        return false;
    }

    public boolean shouldReplace (OTSAbstractRecord arg0)
    {
        return false;
    }

    public int type_id ()
    {
        return 101;
    }

    public String uid ()
    {
        return uid.stringForm();
    }
 

    private ArjunaSubtranAwareResource ref;
    private Uid uid = new Uid();
}

