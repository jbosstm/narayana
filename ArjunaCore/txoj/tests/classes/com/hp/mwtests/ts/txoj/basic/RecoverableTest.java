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
 * $Id: RecoverableTest.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.ats.arjuna.*;

import com.hp.mwtests.ts.txoj.common.resources.RecoverableObject;

import org.junit.Test;
import static org.junit.Assert.*;

public class RecoverableTest
{
    @Test
    public void test()
    {
        RecoverableObject foo = new RecoverableObject();

        AtomicAction A = new AtomicAction();

        A.begin();

        foo.set(2);

        assertEquals(2, foo.get());

        A.abort();

        assertEquals(0, foo.get());

        AtomicAction B = new AtomicAction();

        B.begin();

        foo.set(4);

        assertEquals(4, foo.get());

        B.commit();

        assertEquals(4, foo.get());
    }
}

