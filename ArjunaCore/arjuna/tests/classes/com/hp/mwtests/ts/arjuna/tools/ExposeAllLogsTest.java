/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.tools;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Test that the tooling can exposed all log record types
 *
 * @author Mike Musgrove
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class ExposeAllLogsTest {
    private static final String FOO_TYPE = "StateManager/LockManager/foo";
    private final String osMBeanName = com.arjuna.ats.arjuna.common.arjPropertyManager.getObjectStoreEnvironmentBean().getJmxToolingMBeanName();

    @Test
    public void test1() throws Exception
    {
        test(true);
    }

    @Test
    public void test2() throws Exception
    {
        test(false);
    }

    private void test(boolean exposeAllLogsViaJMX) throws Exception
    {
        RecoveryStore store = StoreManager.getRecoveryStore();
        Set<Uid> uids;
        Map<Uid, ObjectName> uids2 = new HashMap<Uid, ObjectName>();
        JMXServer agent = JMXServer.getAgent();

        // create a record that by default the tooling does not expose
        byte[] data = new byte[10240];
        OutputObjectState state = new OutputObjectState();
        Uid u = new Uid();

        state.packBytes(data);
        assertTrue(store.write_committed(u, FOO_TYPE, state));

        // check that the record is not exposed
        probeObjectStore(false, false);
        // get uids via the object store API
        uids = getUids(store, new HashSet<Uid>(), FOO_TYPE);
        // and validate that there is a uid corresponding to u
        assertTrue(uids.contains(u));

        // get uids via JMX
        getUids(uids2, agent.queryNames(osMBeanName + ",*", null));

        // and validate that there is no MBean corresponding to u
        assertFalse(uids2.containsKey(u));

        // now try the same but tell the browser to expose all log records
        probeObjectStore(true, exposeAllLogsViaJMX);

        // and get the uids for log record MBeans
        uids2.clear();
        getUids(uids2, agent.queryNames(osMBeanName + ",*", null));

        // and validate that there is now an MBean corresponding to u
        assertTrue(uids2.containsKey(u));

        // test that the MBean remove operation works
        agent.getServer().invoke(uids2.get(u), "remove", null, null);

        // check that both the log record and the MBean were removed
        uids.clear();
        getUids(store, uids, FOO_TYPE);
        assertFalse(uids.contains(u));

        uids2.clear();
        getUids(uids2, agent.queryNames(osMBeanName + ",*", null));
        assertFalse(uids2.containsKey(u));
    }

    private void probeObjectStore(boolean exposeAllLogs, boolean useJMX) throws MBeanException {
        ObjectStoreEnvironmentBean osEnvBean = BeanPopulator.getDefaultInstance(ObjectStoreEnvironmentBean.class);

        osEnvBean.setExposeAllLogRecordsAsMBeans(exposeAllLogs);

        ObjStoreBrowser osb = new ObjStoreBrowser();

        // make sure the object store tooling MBean is ready
        osb.start();

        if (useJMX)
            osb.setExposeAllRecordsAsMBeans(exposeAllLogs);

        osb.probe();
    }

    // Given a set of MBean names find their corresponding Uids
    private Map<Uid, ObjectName> getUids(Map<Uid, ObjectName> uids, Set<ObjectName> osEntries) {
        MBeanServer mbs = JMXServer.getAgent().getServer();

        for (ObjectName name : osEntries) {
            Object id = getProperty(mbs, name, "Id");

            if (id != null)
                uids.put(new Uid(id.toString()), name);
        }

        return uids;
    }

    // look up an MBean property
    private static Object getProperty(MBeanServer mbs, ObjectName name, String id) {
        try {
            return mbs.getAttribute(name, id);
        } catch (AttributeNotFoundException e) {
            // ok
        } catch (Exception e) {
            System.out.println("Exception looking up attribute " + id + " for object name " + name);
            e.printStackTrace();
        }

        return null;
    }

    // lookup all log records of a given type
    private Set<Uid> getUids(RecoveryStore recoveryStore, Set<Uid> uids, String type) {
        try {
            InputObjectState states = new InputObjectState();

            if (recoveryStore.allObjUids(type, states) && states.notempty()) {
                boolean finished = false;

                do {
                    Uid uid = UidHelper.unpackFrom(states);

                    if (uid.notEquals(Uid.nullUid())) {
                        uids.add(uid);
                    } else {
                        finished = true;
                    }

                } while (!finished);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return uids;
    }
}