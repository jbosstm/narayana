/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: LastResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import com.hp.mwtests.ts.arjuna.resources.*;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.LastResourceRecord;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.mwlabs.testframework.unittest.Test;

public class LastResource extends Test
{

    public void run (String[] args)
    {
        try
        {
	    boolean success = false;
            AtomicAction A = new AtomicAction();
	    OnePhase opRes = new OnePhase();
	    
	    System.err.println("Starting top-level action.");

            A.begin();

	    A.add(new LastResourceRecord(opRes));
	    A.add(new ShutdownRecord(ShutdownRecord.FAIL_IN_PREPARE));
	    
	    A.commit();

	    if (opRes.status() != OnePhase.COMMITTED)
	    {
		System.err.println("Confirmed that one-phase record is last in prepare.");

		A = new AtomicAction();
		opRes = new OnePhase();

		A.begin();
		
		System.err.println("\nStarting new top-level action.");

		A.add(new LastResourceRecord(opRes));
		A.add(new ShutdownRecord(ShutdownRecord.FAIL_IN_COMMIT));

		A.commit();
		
		if (opRes.status() == OnePhase.COMMITTED)
		{
		    System.err.println("Confirmed that one-phase record is first in commit.");

		    A = new AtomicAction();
		    
		    A.begin();
		    
		    A.add(new LastResourceRecord(new OnePhase()));
		    
		    if (A.add(new LastResourceRecord(new OnePhase())) == AddOutcome.AR_DUPLICATE)
		    {
			System.err.println("\nConfirmed that only one such resource can be added.");
			
			assertSuccess();
		    }
		    else
		    {
			System.err.println("\nMultiple such resources can be added!");
			
			assertFailure();
		    }
		}
		else
		{
		    System.err.println("One-phase record is last in commit!");

		    assertFailure();
		}
	    }
	    else
	    {
		System.err.println("One-phase record is first in prepare!");

		assertFailure();
	    }
        }
        catch (Exception e)
        {
            logInformation("Unexpected Exception - "+e);
            e.printStackTrace(System.err);
            assertFailure();
        }
    }

    public static void main(String[] args)
    {
        LastResource test = new LastResource();
        test.initialise(null, null, args, new com.arjuna.mwlabs.testframework.unittest.LocalHarness());
        test.run(args);
    }

};
