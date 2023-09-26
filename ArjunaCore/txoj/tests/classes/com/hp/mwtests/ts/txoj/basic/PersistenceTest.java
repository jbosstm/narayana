/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.basic;



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
    	ParticipantStore store = StoreManager.getParticipantStore();
        OutputObjectState state = new OutputObjectState();
        Uid u = new Uid();

        assertTrue(store.write_committed(u, "/StateManager/LockManager/foo",
                state));

        InputObjectState inputState = store.read_committed(u,
                "/StateManager/LockManager/foo");

        assertNotNull(inputState);
        store.stop();
    }
    
    @Test
    public void testAtomicObject () throws Throwable
    {
        //StoreManager sm = new StoreManager(null, new TwoPhaseVolatileStore(new ObjectStoreEnvironmentBean()), null);

        AtomicObject obj1 = new AtomicObject(ObjectModel.MULTIPLE);
        
        obj1.set(50);
        
        AtomicObject obj2 = new AtomicObject(obj1.get_uid(), ObjectModel.MULTIPLE);
       
//        assertTrue(obj1.getStore().getClass().getName().equals(TwoPhaseVolatileStore.class.getName()));

        obj1.set(101);

        assertTrue(obj2.get() == 101);
    }
}