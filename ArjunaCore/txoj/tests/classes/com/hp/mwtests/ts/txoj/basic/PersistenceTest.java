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
 * $Id: PersistenceTest.java 2342 2006-03-30 13:06:17Z  $
 */

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore;
import com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class PersistenceTest
{
    @Test
    public void test () throws Throwable
    {
        ParticipantStore store = new ShadowingStore(new ObjectStoreEnvironmentBean());
        OutputObjectState state = new OutputObjectState();
        Uid u = new Uid();

        assertTrue(store.write_committed(u, "/StateManager/LockManager/foo",
                state));

        InputObjectState inputState = store.read_committed(u,
                "/StateManager/LockManager/foo");

        assertNotNull(inputState);
    }
    
    @Test
    public void testTwoPhaseVolatile () throws Throwable
    {
        StoreManager sm = new StoreManager(null, new TwoPhaseVolatileStore(new ObjectStoreEnvironmentBean()), null);

        AtomicObject obj1 = new AtomicObject(ObjectModel.MULTIPLE);
        
        obj1.set(50);
        
        AtomicObject obj2 = new AtomicObject(obj1.get_uid(), ObjectModel.MULTIPLE);
       
        assertTrue(obj1.getStore().getClass().getName().equals(TwoPhaseVolatileStore.class.getName()));

        obj1.set(101);

        assertTrue(obj2.get() == 101);
    }
}
