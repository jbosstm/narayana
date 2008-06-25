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
package com.hp.mwtests.ts.txoj.hammer;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Hammer.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.txoj.common.*;
import java.lang.Thread;
import java.util.Random;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.resources.HammerThreadedObject;
import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;

import java.lang.InterruptedException;

public class Hammer extends Test
{

public void run(String[] args)
    {
	HammerThreadedObject.object = new AtomicObject();
	HammerThreadedObject object1 = new HammerThreadedObject(2);
	HammerThreadedObject object2 = new HammerThreadedObject(-2);

	object1.start();
	object2.start();

	try
	{
	    object1.join();
	    object2.join();
	}
	catch (InterruptedException e)
	{
	}

	/* If we didn't deadlock, we'll get to here. */
	assertSuccess();
    }

public static void main(String[] args)
    {
        Hammer test = new Hammer();
        test.initialise(null, null, args, new LocalHarness());
        test.runTest();
    }
};
