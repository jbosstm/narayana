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
package com.hp.mwtests.ts.txoj.basic;

/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BasicActionTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.common.*;

import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;

import org.junit.Test;
import static org.junit.Assert.*;

public class BasicActionTest
{
    @Test
    public void run() throws TestException
    {
	AtomicObject foo = null;
	Uid u = null;

	if (u == null)
	    foo = new AtomicObject();
	else
	    foo = new AtomicObject(u);

	AtomicAction A = new AtomicAction();

        int value  = foo.get();
	try
	{
	    A.begin();

		foo.set(foo.get()+2);

	    A.commit();

        assertEquals(value+2, foo.get());

	}
	catch (Exception e)
	{
	    A.abort();
        fail("AtomicObject exception raised.");
	}

	System.out.println("\nWill now try some erroneous conditions.\n");

	    AtomicAction B = new AtomicAction();

	    u = new Uid();
	    foo = new AtomicObject(u);

	    B.begin();

	    try
	    {
		System.out.println("attempting to get value from non-existent object: "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }

	    System.out.println("trying to set value to 5");

	    try
	    {
		foo.set(5);
	    }
	    catch (Exception e)
	    {
	    }

	    try
	    {
		System.out.println("attempting to get value again: "+foo.get());
	    }
	    catch (Exception e)
	    {
	    }

	    B.commit();
	}

}
