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
package com.hp.mwtests.ts.arjuna.objectstore;

/*
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ObjectStoreTest.java 2342 2006-03-30 13:06:17Z  $
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.arjuna.ats.internal.arjuna.common.UidHelper;
import org.junit.Test;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StateType;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.utils.FileLock;
import com.arjuna.ats.internal.arjuna.objectstore.ActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.CacheStore;
import com.arjuna.ats.internal.arjuna.objectstore.FileLockingStore;
import com.arjuna.ats.internal.arjuna.objectstore.HashedActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.HashedStore;
import com.arjuna.ats.internal.arjuna.objectstore.NullActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowNoFileLockStore;
import com.arjuna.ats.internal.arjuna.objectstore.ShadowingStore;
import com.arjuna.ats.internal.arjuna.objectstore.VolatileStore;
import com.arjuna.ats.internal.arjuna.objectstore.TwoPhaseVolatileStore;

class DummyOS extends FileLockingStore
{
    public DummyOS(ObjectStoreEnvironmentBean objectStoreEnvironmentBean) throws ObjectStoreException
    {
        super(objectStoreEnvironmentBean);
    }

    public boolean lock ()
    {
        return super.lock(new File("foo"), FileLock.F_WRLCK, true);
    }
    
    public boolean unlock ()
    {
        return super.unlock(new File("foo"));
    }
    
    @Override
    protected InputObjectState read_state (Uid u, String tn, int s)
            throws ObjectStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean remove_state (Uid u, String tn, int s)
            throws ObjectStoreException
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean write_state (Uid u, String tn, OutputObjectState buff,
            int s) throws ObjectStoreException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public int typeIs ()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean commit_state (Uid u, String tn) throws ObjectStoreException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public int currentState (Uid u, String tn) throws ObjectStoreException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean hide_state (Uid u, String tn) throws ObjectStoreException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean reveal_state (Uid u, String tn) throws ObjectStoreException
    {
        // TODO Auto-generated method stub
        return false;
    }
    
}

public class ObjectStoreTest
{
    @Test
    public void testActionStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        ActionStore as = new ActionStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            as.write_uncommitted(u, tn, buff);
            
            as.commit_state(u, tn);
            
            assertTrue(as.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);
            
            InputObjectState ios = new InputObjectState();
            
            as.allObjUids("", ios);
            
            assertTrue(as.read_uncommitted(u, tn) == null);
            
            as.write_committed(u, tn, buff);
            as.read_committed(u, tn);
            
            assertTrue(!as.remove_uncommitted(u, tn));
            
            as.remove_committed(u, tn);
            
            assertTrue(!as.hide_state(u, tn));
            
            assertTrue(!as.reveal_state(u, tn));
        }
    }
    
    @Test
    public void testShadowNoFileLockStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        ShadowNoFileLockStore as = new ShadowNoFileLockStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            as.write_uncommitted(u, tn, buff);
            
            as.commit_state(u, tn);
            
            assertTrue(as.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);
            
            InputObjectState ios = new InputObjectState();
            
            as.allObjUids("", ios);
            
            assertTrue(as.read_uncommitted(u, tn) == null);
            
            as.write_committed(u, tn, buff);
            as.read_committed(u, tn);
            
            assertTrue(!as.remove_uncommitted(u, tn));
            
            as.remove_committed(u, tn);
            
            assertTrue(!as.hide_state(u, tn));
            
            assertTrue(!as.reveal_state(u, tn));
        }
    }
    
    @Test
    public void testHashedStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        HashedStore as = new HashedStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            as.write_uncommitted(u, tn, buff);
            
            as.commit_state(u, tn);
            
            assertTrue(as.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);
            
            InputObjectState ios = new InputObjectState();
            
            as.allObjUids("", ios);
            
            assertTrue(as.read_uncommitted(u, tn) == null);
            
            as.write_committed(u, tn, buff);
            as.read_committed(u, tn);
            
            assertTrue(!as.remove_uncommitted(u, tn));
            
            as.remove_committed(u, tn);
            
            assertTrue(!as.hide_state(u, tn));
            
            assertTrue(!as.reveal_state(u, tn));
        }
    }
    
    //@Test
    public void testCacheStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        CacheStore as = new CacheStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            as.write_uncommitted(u, tn, buff);
            
            as.commit_state(u, tn);
            
            assertTrue(as.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);
            
            InputObjectState ios = new InputObjectState();
            
            as.allObjUids("", ios);
            
            as.read_uncommitted(u, tn);  // may or may not be there if cache flush hasn't happened
            
            as.write_committed(u, tn, buff);
            as.read_committed(u, tn);
            
            as.remove_uncommitted(u, tn);  // may or may not be there if cache flush hasn't happened
            
            as.remove_committed(u, tn);
            
            assertTrue(!as.hide_state(u, tn));
            
            assertTrue(!as.reveal_state(u, tn));
        }
    }
    
    @Test
    public void testHashedActionStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        HashedActionStore as = new HashedActionStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            as.write_uncommitted(u, tn, buff);
            
            as.commit_state(u, tn);
            
            assertTrue(as.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);
            
            InputObjectState ios = new InputObjectState();
            
            as.allObjUids("", ios);
            
            assertTrue(as.read_uncommitted(u, tn) == null);
            
            as.write_committed(u, tn, buff);
            as.read_committed(u, tn);
            
            assertTrue(!as.remove_uncommitted(u, tn));
            
            as.remove_committed(u, tn);
            
            assertTrue(!as.hide_state(u, tn));
            
            assertTrue(!as.reveal_state(u, tn));
        }
    }
    
    @Test
    public void testShadowingStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        ShadowingStore as = new ShadowingStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            as.write_uncommitted(u, tn, buff);
            
            as.commit_state(u, tn);
            
            assertTrue(as.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);
            
            InputObjectState ios = new InputObjectState();
            
            as.allObjUids("", ios);
            
            assertTrue(as.read_uncommitted(u, tn) == null);
            
            as.write_committed(u, tn, buff);
            as.read_committed(u, tn);
            
            assertTrue(!as.remove_uncommitted(u, tn));
            
            as.remove_committed(u, tn);
            
            assertTrue(!as.hide_state(u, tn));
            
            assertTrue(!as.reveal_state(u, tn));
        }
    }
    
    @Test
    public void testNullActionStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        NullActionStore as = new NullActionStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            as.write_uncommitted(u, tn, buff);

            as.commit_state(u, tn);
            
            assertTrue(as.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);
            
            InputObjectState ios = new InputObjectState();
            
            as.allObjUids("", ios);
            
            assertTrue(as.read_uncommitted(u, tn) == null);
            
            as.write_committed(u, tn, buff);
            as.read_committed(u, tn);
            
            assertTrue(!as.remove_uncommitted(u, tn));
            
            as.remove_committed(u, tn);
            
            assertTrue(!as.hide_state(u, tn));
            
            assertTrue(!as.reveal_state(u, tn));
        }
    }

    @Test
    public void testVolatileStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        VolatileStore as = new VolatileStore(objectStoreEnvironmentBean);
        
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";
        
        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();
            
            InputObjectState ios = new InputObjectState();
            
            try
            {
                as.allObjUids("", ios);
            }
            catch (final Exception ex)
            {              
            }
            
            try
            {
                assertTrue(as.read_uncommitted(u, tn) == null);
            }
            catch (final Exception ex)
            {
            }
            
            try
            {
                as.commit_state(u, tn);
            }
            catch (final Exception ex)
            {
            }
            
            as.write_committed(u, tn, buff);
            
            assertTrue(as.currentState(u, tn) == StateStatus.OS_COMMITTED);
            
            as.read_committed(u, tn);
            
            try
            {
                assertTrue(as.remove_uncommitted(u, tn));
            }
            catch (final Exception ex)
            {
            }
            
            as.remove_committed(u, tn);
            
            try
            {
                assertTrue(as.hide_state(u, tn));
            }
            catch (final Exception ex)
            {
            }
            
            try
            {               
                assertTrue(as.reveal_state(u, tn));
            }
            catch (final Exception ex)
            {
            }
        }
    }

    @Test
    public void testTwoVolatileStores () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean1 = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean1.setLocalOSRoot( "tmp" );
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean2 = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean2.setLocalOSRoot( "tmp2" );
        objectStoreEnvironmentBean2.setVolatileStoreSupportAllObjUids(true);

        VolatileStore as1 = new VolatileStore(objectStoreEnvironmentBean1);
        VolatileStore as2 = new VolatileStore(objectStoreEnvironmentBean2);

        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";

        try {
            as1.allObjUids("", new InputObjectState());
            fail("testTwoVolatileStores: allObjUids should have failed");
        } catch (final Exception ex) {
        }

        try {
            as1.allTypes(new InputObjectState());
            fail("testTwoVolatileStores: allTypes should have failed");
        } catch (final Exception ex) {
        }

        try {
            as2.allObjUids("", new InputObjectState());
        } catch (final Exception ex) {
            fail("testTwoVolatileStores: allObjUids should have passed");
        }

        try {
            as2.allTypes(new InputObjectState());
        } catch (final Exception ex) {
            fail("testTwoVolatileStores: allTypes should have passed");
        }
    }

    private void addType(ObjectStore store, String type) throws Exception {
        byte[] data = new byte[10240];
        OutputObjectState state = new OutputObjectState();
        Uid u = new Uid();

        state.packBytes(data);

        assertTrue(store.write_committed(u, type, state));
    }

    private Collection<String> getAllTypes(ObjectStore store) throws Exception {
        Collection<String> allTypes = new ArrayList<>();
        InputObjectState types = new InputObjectState();

        assertTrue(store.allTypes(types));

        while (true) {
            try {
                String typeName = types.unpackString();
                if (typeName.length() == 0)
                    break;
                allTypes.add(typeName);
            } catch (IOException e1) {
                break;
            }
        }

        return allTypes;
    }

    private Collection<Uid> getUids(ObjectStore store, String type) throws Exception {
        Collection<Uid> uids = new ArrayList<>();
        InputObjectState ios = new InputObjectState();

        assertTrue(store.allObjUids(type, ios));

        while (true) {
            Uid uid = UidHelper.unpackFrom(ios);

            if (uid.equals( Uid.nullUid() ))
                break;

            uids.add(uid);
        }

        return uids;
    }

    @Test
    public void testTypedVolatileStore () throws Exception {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );
        objectStoreEnvironmentBean.setVolatileStoreSupportAllObjUids(true);

        typedVolatileStoreCommon(new VolatileStore(objectStoreEnvironmentBean));
    }

    @Test
    public void testTypedTwoPhaseVolatileStore () throws Exception {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );
        objectStoreEnvironmentBean.setVolatileStoreSupportAllObjUids(true);

        typedVolatileStoreCommon(new TwoPhaseVolatileStore(objectStoreEnvironmentBean));
    }

    private void typedVolatileStoreCommon(ObjectStore as) throws Exception {
        // TypedVolatileStore and TypedTwoPhaseVolatileStore extend VolatileStore and TwoPhaseVolatileStore,
        // respectively, by supporting the allTypes and allObjUids methods
        int FOO_UID_COUNT = 10;
        int BAR_UID_COUNT = 20;

        for (int i = 0; i < FOO_UID_COUNT; i++)
            addType(as, FOO_TYPE);

        for (int i = 0; i < BAR_UID_COUNT; i++)
            addType(as, BAR_TYPE);

        // test that allTypes contains the added types
        Collection<String> allTypes = getAllTypes(as);

        assertTrue(allTypes.contains(FOO_TYPE));
        assertTrue(allTypes.contains(BAR_TYPE));

        // test that allObjUids is correct
        Collection<Uid> fooUids = getUids(as, FOO_TYPE);
        Collection<Uid> barUids = getUids(as, BAR_TYPE);

        assertEquals(FOO_UID_COUNT, fooUids.size());
        assertEquals(BAR_UID_COUNT, barUids.size());

        // assert that the two collections do not overlap
        fooUids.removeAll(barUids);
        assertEquals(FOO_UID_COUNT, fooUids.size());

        // now test the remaining methods of VolatileStore (same as testVolatileStore)
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";

        for (int i = 0; i < 100; i++)
        {
            Uid u = new Uid();

            try
            {
                assertTrue(as.read_uncommitted(u, tn) == null);
            }
            catch (final Exception ex)
            {
            }

            try
            {
                as.commit_state(u, tn);
            }
            catch (final Exception ex)
            {
            }

            as.write_committed(u, tn, buff);

            assertTrue(as.currentState(u, tn) == StateStatus.OS_COMMITTED);

            as.read_committed(u, tn);

            as.remove_committed(u, tn);

            try
            {
                assertTrue(as.hide_state(u, tn));
            }
            catch (final Exception ex)
            {
            }

            try
            {
                assertTrue(as.reveal_state(u, tn));
            }
            catch (final Exception ex)
            {
            }
        }
    }

    @Test
    public void testFileLockingStore () throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        DummyOS as = new DummyOS(objectStoreEnvironmentBean);
        
        assertTrue(as.typeIs() != -1);
        
        assertTrue(as.lock());
        assertTrue(as.unlock());
    }
    
    @Test
    public void testIterator () throws Exception
    {
        Uid u1 = new Uid();
        Uid u2 = new Uid();
        
        assertTrue(StoreManager.getTxLog().write_committed(u1, "foo", new OutputObjectState()));
        assertTrue(StoreManager.getTxLog().write_committed(u2, "foo", new OutputObjectState()));
        
        ObjectStoreIterator iter = new ObjectStoreIterator(StoreManager.getRecoveryStore(), "foo");

        // iteration ordering is not guaranteed.

        List<Uid> uids = new ArrayList<Uid>();
        do {
            Uid uid = iter.iterate();
            if (uid.equals(Uid.nullUid())) {
                break;
            }
            uids.add(uid);
        } while (true);

        boolean foundU1 = false;
        boolean foundU2 = false;
        StringBuffer listOfUids = new StringBuffer("u1:"+u1.stringForm()+"u2:"+u2.stringForm());
        for (Uid uid: uids) {
            if (u1.equals(uid)) {
                foundU1 = true;
            } else if (u2.equals(uid)) {
                foundU2 = true;
            }
            listOfUids.append("1:"+uid.toString());
        }
        assertTrue("U1 search " + listOfUids.toString(), foundU1);
        assertTrue("U2 search " + listOfUids.toString(), foundU2);
    }

    private static final boolean validate(ObjectStore objStore)
    {
        if (objStore == null)
            return false;
        
        boolean passed = false;       

        if (objStore.getClass().getName().equals(imple))
        {
            if (objStore.shareState() == StateType.OS_SHARED) {
                if (objStore.storeDir().equals(objectStoreDir)) {
                    if (objStore.storeRoot().equals(localOSRoot))
                        passed = true;
                    else
                        System.err.println("ObjectStore root wrong: " + objStore.storeRoot());
                } else
                    System.err.println("ObjectStore dir wrong: " + objStore.storeDir());
            } else
                System.err.println("Share state wrong: " + objStore.shareState());
        } else
            System.err.println("Implementation wrong: " + objStore.getClass().getSimpleName());

        return passed;
    }

    private static String imple = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreType();
    private static String localOSRoot = "foo";
    private static String objectStoreDir = "tmp"+"/bar";

    private static final String FOO_TYPE = File.separator+"StateManager"+File.separator+"LockManager"+File.separator+"foo";
    private static final String BAR_TYPE = File.separator+"StateManager"+File.separator+"LockManager"+File.separator+"bar";
}
