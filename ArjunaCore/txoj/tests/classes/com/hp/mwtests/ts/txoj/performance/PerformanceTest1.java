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
package com.hp.mwtests.ts.txoj.performance;

/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PerformanceTest1.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;

import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.resources.RecoverableObject;

import org.junit.Test;
import static org.junit.Assert.*;

public class PerformanceTest1
{
    @Test
    public void recoverableTest()
    {
        long iters = 4;

        RecoverableObject foo = new RecoverableObject();
        AtomicAction A = new AtomicAction();
        long t1 = System.currentTimeMillis();

        A.begin();

        for (int c = 0; c < iters; c++)
        {
            foo.set(2);
        }

        A.commit();
    }

    @Test
    public void persistentTest()
    {
        long iters = 4;

        AtomicObject foo = new AtomicObject();
        AtomicAction A = new AtomicAction();
        long t1 = System.currentTimeMillis();

        A.begin();

        try {
            for (int c = 0; c < iters; c++)
            {
                foo.set(2);
            }
        }
        catch (TestException e)
        {
            fail("AtomicObject exception raised.");
        }

        A.commit();
    }
}
