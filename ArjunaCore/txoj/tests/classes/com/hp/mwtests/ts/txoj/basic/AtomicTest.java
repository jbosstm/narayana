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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AtomicTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.common.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

import org.junit.Test;
import static org.junit.Assert.*;

public class AtomicTest
{
    @Test
    public void run()
    {
	AtomicObject foo = new AtomicObject();
	Uid u = foo.get_uid();

	AtomicAction A = new AtomicAction();

    try {
	    A.begin();

	    foo.set(2);

	    A.commit();

	    int finalVal = foo.get();

        assertEquals(2, finalVal);


	    foo = new AtomicObject(u);

	    A = new AtomicAction();

	    A.begin();

	    foo.set(4);

	    A.commit();

	    finalVal = foo.get();

        assertEquals(4, finalVal);

	}
	catch (TestException e)
	{
	    A.abort();

        fail("AtomicObject exception raised.");
	}

    }

}
