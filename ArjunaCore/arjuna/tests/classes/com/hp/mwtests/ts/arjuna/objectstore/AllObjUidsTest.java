/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AllObjUidsTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.objectstore.ObjectStore;

import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.arjuna.coordinator.TxControl;

import org.jboss.dtf.testframework.unittest.Test;

public class AllObjUidsTest extends Test
{

    public void run (String[] args)
    {
	ObjectStore objStore = TxControl.getStore();
	boolean passed = false;
	InputObjectState ios = new InputObjectState();
	String type = "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/ArjunaMSXAAtomicAction";

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].equals("-help"))
	    {
		System.err.println("Usage: [-help] [-type <type name>]");

		System.exit(0);
	    }

	    if (args[i].equals("-type"))
	    {
		type = args[i+1];

		objStore = new ObjectStore();
	    }
	}

	try
	{
	    if (objStore.allObjUids(type, ios, ObjectStore.OS_UNKNOWN))
	    {
		Uid id = new Uid(Uid.nullUid());

		do
		{
		    try
		    {
			id.unpack(ios);
		    }
		    catch (Exception ex)
		    {
			id = Uid.nullUid();
		    }

		    if (id.notEquals(Uid.nullUid()))
			System.err.println("Got UNKNOWN "+id);

		    passed = true;

		} while (id.notEquals(Uid.nullUid()));
	    }

	    System.err.println("\n");

	    if (objStore.allObjUids(type, ios, ObjectStore.OS_COMMITTED))
	    {
		Uid id = new Uid(Uid.nullUid());

		do
		{
		    try
		    {
			id.unpack(ios);
		    }
		    catch (Exception ex)
		    {
			id = Uid.nullUid();
		    }

		    if (id.notEquals(Uid.nullUid()))
			System.err.println("Got COMMITTED "+id);

		    passed = true;

		} while (id.notEquals(Uid.nullUid()));
	    }

	    System.err.println("\n");

	    if (objStore.allObjUids(type, ios, ObjectStore.OS_UNCOMMITTED))
	    {
		Uid id = new Uid(Uid.nullUid());

		do
		{
		    try
		    {
			id.unpack(ios);
		    }
		    catch (Exception ex)
		    {
			id = Uid.nullUid();
		    }

		    if (id.notEquals(Uid.nullUid()))
			System.err.println("Got UNCOMMITTED "+id);

		    passed = true;

		} while (id.notEquals(Uid.nullUid()));
	    }
	}
	catch (Exception ex)
	{
	    ex.printStackTrace();
	}

	if (passed)
	    assertSuccess();
	else
	    assertFailure();
    }

    public static void main(String[] args)
    {
        AllObjUidsTest test = new AllObjUidsTest();
        test.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());
        test.run(args);
    }

}
