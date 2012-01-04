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
 * $Id: BasicTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.statemanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.Uid;
import com.hp.mwtests.ts.arjuna.resources.BasicObject;

public class BasicTest
{
    @Test
    public void test() throws Exception
    {
        AtomicAction A = new AtomicAction();
        BasicObject bo = new BasicObject();

        A.begin();

        bo.set(2);

        A.commit();
        
        assertTrue(bo.getStore() != null);
        assertTrue(bo.getStoreRoot() != null);
        
        assertEquals(bo.getObjectModel(), ObjectModel.SINGLE);
    }

    @Test
    public void testNested () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();      
        BasicObject bo = new BasicObject();
        Uid u = bo.get_uid();
        
        A.begin();
        B.begin();
        
        bo.set(2);

        B.commit();
        A.commit();

        bo = new BasicObject(u);
        
        A = new AtomicAction();
        
        A.begin();
        
        assertEquals(bo.get(), 2);
        
        A.commit();
    }
}
