/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.hp.mwtests.ts.arjuna.tools;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.ParticipantStoreBean;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.RecoveryStoreBean;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.ParticipantStoreProxy;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.RecoveryStoreProxy;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.StoreManagerProxy;
import com.arjuna.ats.internal.arjuna.objectstore.*;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class OtherObjectStoreAPIJMXTest {
    private RecoveryStoreBean rsb;
    private ParticipantStoreBean psb;

    private RecoveryStoreProxy rsProxy;
    private ParticipantStoreProxy psProxy;

    public void createMBeans (RecoveryStore rs, ParticipantStore ps) throws Exception
    {
        // create MBeans representing the ObjectStore
        rsb = new RecoveryStoreBean(rs);
        psb = new ParticipantStoreBean(ps);

        // and register them with the local MBean Server
        rsb.start();
        psb.start();

        // obtain (JMX) proxies for the recovery and participant stores
        rsProxy = StoreManagerProxy.getRecoveryStore(null);
        psProxy = StoreManagerProxy.getParticipantStore(null);
    }

    @After
    public void tearDown () throws Exception
    {
        // Unregister MBeans
        rsb.stop();
        psb.stop();

        StoreManagerProxy.releaseProxy();
    }

    @Test
    public void testActionStore() throws Exception
    {
        ObjectStoreEnvironmentBean objectStoreEnvironmentBean = new ObjectStoreEnvironmentBean();
        objectStoreEnvironmentBean.setLocalOSRoot( "tmp" );

        ActionStore as = new ActionStore(objectStoreEnvironmentBean);

        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";

        createMBeans(as, as);

        for (int i = 0; i < 10; i++)
        {
            Uid u = new Uid();

            psProxy.write_uncommitted(u, tn, buff);

            psProxy.commit_state(u, tn);

            assertTrue(rsProxy.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);

            InputObjectState ios = new InputObjectState();

            rsProxy.allObjUids("", ios);

            assertTrue(psProxy.read_uncommitted(u, tn) == null);

            rsProxy.write_committed(u, tn, buff);
            rsProxy.read_committed(u, tn);

            assertTrue(!psProxy.remove_uncommitted(u, tn));

            rsProxy.remove_committed(u, tn);

            assertTrue(!rsProxy.hide_state(u, tn));

            assertTrue(!rsProxy.reveal_state(u, tn));
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

        createMBeans(as, as);

        for (int i = 0; i < 10; i++)
        {
            Uid u = new Uid();

            psProxy.write_uncommitted(u, tn, buff);

            psProxy.commit_state(u, tn);

            assertTrue(rsProxy.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);

            InputObjectState ios = new InputObjectState();

            rsProxy.allObjUids("", ios);

            assertTrue(psProxy.read_uncommitted(u, tn) == null);

            rsProxy.write_committed(u, tn, buff);
            rsProxy.read_committed(u, tn);

            assertTrue(!as.remove_uncommitted(u, tn));

            rsProxy.remove_committed(u, tn);

            assertTrue(!rsProxy.hide_state(u, tn));

            assertTrue(!rsProxy.reveal_state(u, tn));
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

        createMBeans(as, as);

        for (int i = 0; i < 10; i++)
        {
            Uid u = new Uid();

            psProxy.write_uncommitted(u, tn, buff);

            psProxy.commit_state(u, tn);

            assertTrue(rsProxy.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);

            InputObjectState ios = new InputObjectState();

            rsProxy.allObjUids("", ios);

            assertTrue(psProxy.read_uncommitted(u, tn) == null);

            rsProxy.write_committed(u, tn, buff);
            rsProxy.read_committed(u, tn);

            assertTrue(!psProxy.remove_uncommitted(u, tn));

            rsProxy.remove_committed(u, tn);

            assertTrue(!rsProxy.hide_state(u, tn));

            assertTrue(!rsProxy.reveal_state(u, tn));
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

        createMBeans(as, as);
        for (int i = 0; i < 10; i++)
        {
            Uid u = new Uid();

            psProxy.write_uncommitted(u, tn, buff);

            psProxy.commit_state(u, tn);

            assertTrue(rsProxy.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);

            InputObjectState ios = new InputObjectState();

            rsProxy.allObjUids("", ios);

            assertTrue(psProxy.read_uncommitted(u, tn) == null);

            rsProxy.write_committed(u, tn, buff);
            rsProxy.read_committed(u, tn);

            assertTrue(!psProxy.remove_uncommitted(u, tn));

            rsProxy.remove_committed(u, tn);

            assertTrue(!rsProxy.hide_state(u, tn));

            assertTrue(!rsProxy.reveal_state(u, tn));
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

        createMBeans(as, as);
        for (int i = 0; i < 10; i++)
        {
            Uid u = new Uid();

            psProxy.write_uncommitted(u, tn, buff);

            psProxy.commit_state(u, tn);

            assertTrue(rsProxy.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);

            InputObjectState ios = new InputObjectState();

            rsProxy.allObjUids("", ios);

            assertTrue(psProxy.read_uncommitted(u, tn) == null);

            rsProxy.write_committed(u, tn, buff);
            rsProxy.read_committed(u, tn);

            assertTrue(!psProxy.remove_uncommitted(u, tn));

            rsProxy.remove_committed(u, tn);

            assertTrue(!rsProxy.hide_state(u, tn));

            assertTrue(!rsProxy.reveal_state(u, tn));
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

        createMBeans(as, as);
        for (int i = 0; i < 10; i++)
        {
            Uid u = new Uid();

            InputObjectState ios = new InputObjectState();

            try
            {
                rsProxy.allObjUids("", ios);
            }
            catch (final Exception ex)
            {
            }

            try
            {
                assertTrue(psProxy.read_uncommitted(u, tn) == null);
            }
            catch (final Exception ex)
            {
            }

            try
            {
                psProxy.commit_state(u, tn);
            }
            catch (final Exception ex)
            {
            }

            rsProxy.write_committed(u, tn, buff);

            assertTrue(rsProxy.currentState(u, tn) == StateStatus.OS_COMMITTED);

            rsProxy.read_committed(u, tn);

            try
            {
                assertTrue(psProxy.remove_uncommitted(u, tn));
            }
            catch (final Exception ex)
            {
            }

            rsProxy.remove_committed(u, tn);

            try
            {
                assertTrue(rsProxy.hide_state(u, tn));
            }
            catch (final Exception ex)
            {
            }

            try
            {
                assertTrue(rsProxy.reveal_state(u, tn));
            }
            catch (final Exception ex)
            {
            }
        }
    }

}
