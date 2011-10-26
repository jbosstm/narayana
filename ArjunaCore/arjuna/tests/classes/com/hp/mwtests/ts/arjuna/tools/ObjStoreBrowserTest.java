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

import java.util.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.recovery.RecoveryDriver;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

import com.arjuna.ats.arjuna.tools.osb.mbean.*;

import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.ats.arjuna.AtomicAction;

public class ObjStoreBrowserTest {
	private RecoveryManagerImple rcm;
	private RecoveryDriver rd;

	@Before
	public void setUp () throws Exception
	{
		recoveryPropertyManager.getRecoveryEnvironmentBean().setPeriodicRecoveryPeriod(1);
		recoveryPropertyManager.getRecoveryEnvironmentBean().setRecoveryBackoffPeriod(1);

		rcm = new RecoveryManagerImple(true);
		rcm.addModule(new AtomicActionRecoveryModule());
		rd = new RecoveryDriver(RecoveryManager.getRecoveryManagerPort(),
				recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryAddress(), 100000);
	}

	@After
	public void tearDown () throws Exception
	{
		rcm.removeAllModules(false);
		rcm.stop(false);
	}

	/**
	 * create an MBean to represent an ObjectStore
	 * @return An object that maintains MBeans representing completing transactions
	 */
	private ObjStoreBrowser createObjStoreBrowser() {
		ObjStoreBrowser osb = new ObjStoreBrowser();

		// define which object store types we are prepared to represent by mbeans
		osb.setTypes( new HashMap<String, String>() {{
			put("StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction", "com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean");
		}});

		return osb;
	}

	@Test
	public void testOSEntryBean() throws Exception {
		com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new OSEntryBean());
	}

	@Test
	public void testLogRecordWrapper() throws Exception {
		com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(new LogRecordWrapper(Uid.nullUid()));
	}

	@Test
	public void testObjectStoreBrowser() throws Exception {
		com.arjuna.common.tests.simple.EnvironmentBeanTest.testBeanByReflection(createObjStoreBrowser());
	}

	@Test
	public void basicOSBTest () throws Exception
	{
		ObjStoreBrowser osb = new ObjStoreBrowser("os");

		Properties p = ObjStoreBrowser.loadProperties("invalid property file");

		assertTrue(p.size() == 0);
		osb.start();
		osb.probe();

		// there should not be any MBeans
		assertNull(osb.findUid(Uid.nullUid()));

		// listing beans of an invalid type returns null
		assertNull(osb.probe("InvalidType", "BeanClass"));
		
		// TODO windows
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
			// listing beans of a valid type returns an empty list
			assertNotNull(osb.probe("Recovery",
					"com.arjuna.ats.arjuna.tools.osb.mbean.OSEntryBean"));
		}

		osb.stop();
	}

	/**
	 * Create an atomic action with two participants, one of which will generate a heuristic during phase 2.
	 * The test will move the heuristic back into the prepared state and trigger recovery to replay phase 2.
	 * The test then asserts that the corresponding MBeans have been unregistered.
	 * @throws Exception if test fails unexpectedly
	 */
	@Test
	public void aaReplayTest() throws Exception {
		// TODO windows
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
		aaTest(true);
		}
	}
	
	/**
	 * Similar to aaReplayTest except that the whole transaction record is removed from the object store
	 * (instead of replaying the record that generates a heuristic).
	 * @throws Exception if test fails unexpectedly
	 */
	@Test
	public void aaRemoveTest() throws Exception {
		// TODO windows
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
		aaTest(false);
		}
	}

	public void aaTest(boolean replay) throws Exception {
		ObjStoreBrowser osb = createObjStoreBrowser();
		AtomicAction A = new AtomicAction();
		CrashRecord recs[] = {
				new CrashRecord(CrashRecord.CrashLocation.NoCrash, CrashRecord.CrashType.Normal),
				new CrashRecord(CrashRecord.CrashLocation.CrashInCommit, CrashRecord.CrashType.HeuristicHazard)
		};

		// register CrashRecord record type so that it is persisted in the object store correctly
		RecordTypeManager.manager().add(new RecordTypeMap() {
			public Class<? extends AbstractRecord> getRecordClass () { return CrashRecord.class;}
			public int getType () {return RecordType.USER_DEF_FIRST0;}
		});

		// create an atomic action, register crash records with it and then commit
		A.begin();

		for (CrashRecord rec : recs)
			A.add(rec);

		int outcome = A.commit();

		// the second participant should have generated a heuristic during commit
		assertEquals(ActionStatus.H_HAZARD, outcome);

		// generate MBeans representing the atomic action that was just committed
		osb.start();
		osb.probe();

		// there should be one MBean corresponding to the AtomicAction A
		UidWrapper w = osb.findUid(A.get_uid());
		assertNotNull(w);
		OSEntryBean ai = w.getMBean();
		assertNotNull(ai);

		// the MBean should wrap an ActionBean
		assertTrue(ai instanceof ActionBean);
		ActionBean actionBean = (ActionBean) ai;

		// and there should be one MBean corresponding to the CrashRecord that got the heuristic:
		int recCount = 0;
		for (CrashRecord rec : recs) {
			LogRecordWrapper lw = actionBean.getParticipant(rec);

			if (lw != null) {
				recCount += 1;

				assertTrue(lw.isHeuristic());

				// put the participant back onto the pending list
				lw.setStatus("PREPARED");
				// and check that the record is no longer in a heuristic state
				assertFalse(lw.isHeuristic());
			}
		}

		assertEquals(1, recCount);

		if (!replay) {
			actionBean.remove();
		} else {
			/*
			* prompt the recovery manager to replay the record that was
			* moved off the heuristic list and back onto the prepared list
			*/
			rd.synchronousScan();
		}

		/*
		 * Since the recovery scan (or explicit remove request) will have successfully removed the record from
		 * the object store another probe should cause the MBean representing the record to be unregistered
		 */
		osb.probe();

		// look up the MBean and verify that it no longer exists
		w = osb.findUid(A.get_uid());
		assertNull(w);

		osb.dump(new StringBuilder());
		osb.stop();
	}

	// define an MBean interface for use in the next test
	public interface NotAnotherMBean extends ObjStoreItemMBean {}

	@Test
	public void testJMXServer() throws Exception {

		class NonCompliantBean implements NotAnotherMBean {}
		
		ObjStoreBrowser osb = createObjStoreBrowser();
		OSEntryBean bean;
		String validName = "jboss.jta:type=TestObjectStore";

		osb.start();
		osb.probe();

		bean = new OSEntryBean();

		// MalformedObjectNameException
		assertNull(JMXServer.getAgent().registerMBean("InvalidName", bean));
		assertFalse(JMXServer.getAgent().unregisterMBean("InvalidName"));

		// InstanceNotFoundException
		assertFalse(JMXServer.getAgent().unregisterMBean(validName));

		// NotCompliantMBeanException
		assertNull(JMXServer.getAgent().registerMBean(validName, new NonCompliantBean()));

		// Do it right this time
		int cnt = JMXServer.getAgent().queryNames(validName, null).size();
		assertNotNull(JMXServer.getAgent().registerMBean(validName, bean));
		assertEquals(cnt + 1, JMXServer.getAgent().queryNames(validName, null).size());

		// InstanceAlreadyExistsException
		assertNull(JMXServer.getAgent().registerMBean(validName, bean));

		// Make sure unregistering a valid bean works
		assertTrue(JMXServer.getAgent().unregisterMBean(validName));
		assertEquals(0, JMXServer.getAgent().queryNames(validName, null).size());

		osb.stop();
	}
}
