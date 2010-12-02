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
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.ParticipantStoreProxy;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.RecoveryStoreProxy;
import com.arjuna.ats.arjuna.tools.osb.api.proxy.StoreManagerProxy;
import javax.management.*;

import com.arjuna.ats.arjuna.tools.osb.api.mbeans.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ObjectStoreAPIJMXTest {
	private RecoveryStoreBean rsb;
	private ParticipantStoreBean psb;

	private RecoveryStoreProxy prs;
	private ParticipantStoreProxy pps;
    private boolean notified;

    private NotificationListener listener = new NotificationListener() {
        public void handleNotification(Notification notification, Object handback) {
            notified = true;
        }
    };

	@Before
	public void setUp () throws Exception
	{
        notified = false;

		// create MBeans representing the ObjectStore
		rsb = new RecoveryStoreBean();
		psb = new ParticipantStoreBean();

		// and register them with the local MBean Server
		rsb.start();
		psb.start();

		// obtain (JMX) proxies for the recovery and participant stores
		prs = StoreManagerProxy.getRecoveryStore(listener);
		pps = StoreManagerProxy.getParticipantStore(listener);
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
	public void testRecoveryStoreBean() throws Exception {
		com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new RecoveryStoreBean());
	}

	@Test
	public void testParticipantStoreBean() throws Exception {
		com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new ParticipantStoreBean());
	}

	@Test
	public void testShadowNoFileLockStore () throws Exception
	{
        final OutputObjectState buff = new OutputObjectState();
        final String tn = "/StateManager/junit";

        System.out.println("Testing shadow file store");

        for (int i = 0; i < 10; i++)
        {
            Uid u = new Uid();

            pps.write_uncommitted(u, tn, buff);

            pps.commit_state(u, tn);

            assertTrue(prs.currentState(u, tn) != StateStatus.OS_UNCOMMITTED);

            InputObjectState ios = new InputObjectState();

            prs.allObjUids("", ios);

            assertTrue(pps.read_uncommitted(u, tn) == null);

            prs.write_committed(u, tn, buff);
            prs.read_committed(u, tn);

            assertTrue(!pps.remove_uncommitted(u, tn));

            prs.remove_committed(u, tn);

            assertTrue(!prs.hide_state(u, tn));

            assertTrue(!prs.reveal_state(u, tn));
        }
    }

    //@Test
    //TODO this test only works on an initially empty store
    public void testIterator () throws Exception
    {
        Uid u1 = new Uid();
        Uid u2 = new Uid();

        prs.write_committed(u1, "foo", new OutputObjectState());
        prs.write_committed(u2, "foo", new OutputObjectState());

        ObjectStoreIterator iter = new ObjectStoreIterator(prs, "foo");

        // iteration ordering is not guaranteed.

        Uid x = iter.iterate();
        assertTrue(x.notEquals(Uid.nullUid()));
        assertTrue(x.equals(u1) || x.equals(u2));

        Uid y = iter.iterate();
        assertTrue(y.notEquals(Uid.nullUid()));
        assertTrue(y.equals(u1) || y.equals(u2));

        assertTrue(iter.iterate().equals(Uid.nullUid()));
    }

    @Test
	public void testNotification() throws Exception {
        // calling stop on the MBean should generate a notification
        rsb.stop();

        // make sure we wait long enough for it to be sent        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // check that the MBean notification was sent
        assertTrue(notified);
    }

    @Test
    public void testStoreManagerProxyHelpers() throws Exception {
        assertEquals(prs, StoreManagerProxy.getRecoveryStore());
        assertEquals(pps, StoreManagerProxy.getParticipantStore());
        assertEquals(prs, StoreManagerProxy.getRecoveryStore(null, null));
        assertEquals(pps, StoreManagerProxy.getParticipantStore(null, null));
    }
}
