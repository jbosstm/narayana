/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.tools;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.hp.mwtests.ts.jta.jts.common.ExtendedCrashRecord;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

import org.junit.After;
import org.junit.Before;
import org.omg.CORBA.ORBPackage.InvalidName;

import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

import org.junit.BeforeClass;
import org.junit.AfterClass;

import javax.management.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class JTSOSBTestBase extends TestBase {
	@BeforeClass
	public static void beforeClass() {
		RecordTypeManager.manager().add(new RecordTypeMap() {
			public Class<? extends AbstractRecord> getRecordClass() {
				return ExtendedCrashRecord.class;
			}

			public int getType() {
				return RecordType.USER_DEF_FIRST0;
			}
		});
	}

	@BeforeClass
	public static void initOrb() throws InvalidName {
		int recoveryOrbPort = jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort();
		final Properties p = new Properties();
		p.setProperty("OAPort", ""+recoveryOrbPort);
		p.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", ""+recoveryOrbPort);
		p.setProperty("com.sun.CORBA.POA.ORBServerId", ""+recoveryOrbPort);

		ORB orb = ORB.getInstance("test");
		OA oa = OA.getRootOA(orb);
		orb.initORB(new String[] {}, p);
		oa.initOA();

		ORBManager.setORB(orb);
		ORBManager.setPOA(oa);
	}

	@AfterClass
	public static void shutdownOrb() {
		ORBManager.getPOA().destroy();
		ORBManager.getORB().shutdown();
		ORBManager.reset();
	}

	@Before
	public void beforeTest()
	{
		emptyObjectStore();
	}

	public ObjStoreBrowser createObjStoreBrowser(boolean probe) throws MBeanException {
		ObjStoreBrowser osb = new ObjStoreBrowser();

		osb.viewSubordinateAtomicActions(true);

		if (probe) {
			osb.start();
			osb.probe();
		}

		return osb;
	}

    private void showAllMBeans(MBeanServer mbs) {
        try {
            Set<ObjectInstance> allBeans = mbs.queryMBeans(new ObjectName("narayana.logStore:*"), null) ;
            System.out.printf("%d MBeans:%n", allBeans.size());
            for (ObjectInstance oi : allBeans)
                System.out.printf("\t%s%n", oi.getObjectName().getCanonicalName());
        } catch (MalformedObjectNameException e) {
            System.out.printf("error dumping MBeans %s%n", e.getMessage());
        }
    }

    protected void assertBeanWasCreated(ArjunaTransactionImple txn) throws MBeanException {
        int heuristicParticipantCount = generatedHeuristicHazard(txn);

        ObjStoreBrowser osb = createObjStoreBrowser(true);

        MBeanServer mbs = JMXServer.getAgent().getServer();

        showAllMBeans(mbs);

		try {
			String type = ObjStoreBrowser.canonicalType(txn.type());

			StringBuilder beanName = new StringBuilder(osb.getObjStoreBrowserMBeanName() + ",itype=").
					append(type).append(",uid=").append(txn.get_uid().fileStringForm());

			System.out.printf("assertBeanWasCreated: bean name = %s%n", beanName);

			Set<ObjectInstance> transactions = mbs.queryMBeans(new ObjectName(beanName.toString()), null);
			Set<ObjectInstance> participants = mbs.queryMBeans(new ObjectName(beanName.append(",puid=*").toString()), null);
			Map<String, String> attributes;

			assertEquals(1, transactions.size());

			assertEquals(heuristicParticipantCount, participants.size());

			ObjectInstance participant = participants.iterator().next();

			attributes = getMBeanValues(mbs, participant.getObjectName());

			assertEquals("HEURISTIC", attributes.get("Status"));

		} catch (Exception e) {
			e.printStackTrace();
			fail("bean was not created: " + e.getMessage());
		} finally {
			osb.stop();
		}
    }

	private Map<String, String> getMBeanValues(MBeanServerConnection cnx, ObjectName on, String ... attributeNames)
			throws InstanceNotFoundException, IOException, ReflectionException, IntrospectionException {

		if (attributeNames.length == 0) {
			MBeanInfo info = cnx.getMBeanInfo( on );
			MBeanAttributeInfo[] attributeArray = info.getAttributes();
			int i = 0;
			attributeNames = new String[attributeArray.length];

			for (MBeanAttributeInfo ai : attributeArray)
				attributeNames[i++] = ai.getName();
		}

		AttributeList attributes = cnx.getAttributes(on, attributeNames);
		Map<String, String> values = new HashMap<String, String>();

		for (javax.management.Attribute attribute : attributes.asList()) {
			Object value = attribute.getValue();

			values.put(attribute.getName(), value == null ? "" : value.toString());
		}

		return values;
	}

	/**
	 * Generate a transaction log that contains a heuristic hazard
	 * @param txn a transaction of the desired type
	 * @return the number of participants that that will have generated a heuristic hazard
	 */
    protected int generatedHeuristicHazard(ArjunaTransactionImple txn) {
		ThreadActionData.purgeActions();

		ExtendedCrashRecord recs[] = {
				new ExtendedCrashRecord(ExtendedCrashRecord.CrashLocation.NoCrash, ExtendedCrashRecord.CrashType.Normal),
				new ExtendedCrashRecord(ExtendedCrashRecord.CrashLocation.CrashInCommit, ExtendedCrashRecord.CrashType.HeuristicHazard)
		};

        txn.start();

        for (ExtendedCrashRecord rec : recs)
            txn.add(rec);

        txn.end(true);

        return 1;
    }
}