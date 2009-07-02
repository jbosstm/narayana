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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DestroyTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.destroy;

import com.arjuna.ats.arjuna.AtomicAction;

import com.hp.mwtests.ts.arjuna.resources.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class DestroyTest
{
    @Test
    public void test()
    {
        for (int i = 0; i < 100; i++) {
            AtomicAction A = new AtomicAction();

            A.begin();

            BasicObject bo = new BasicObject();

            bo.set(2);

            A.commit();

            AtomicAction B = new AtomicAction();

            B.begin();

            bo.destroy();

            B.abort();

            AtomicAction C = new AtomicAction();

            C.begin();

            bo.destroy();

            C.commit();
        }
    }
}
