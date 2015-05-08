/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.hp.mwtests.ts.jta.jts.tools;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreBrowser;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.hp.mwtests.ts.jta.jts.common.ExtendedCrashRecord;
import com.hp.mwtests.ts.jta.jts.common.TestBase;
import org.junit.After;
import org.omg.CORBA.ORBPackage.InvalidName;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

import org.junit.BeforeClass;
import org.junit.AfterClass;

import java.util.Properties;

import static org.junit.Assert.assertNotNull;

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

    public void setUp()
	{
        emptyObjectStore();
	}

	@After
    public void tearDown()
	{
        emptyObjectStore();
    }

	public ObjStoreBrowser createObjStoreBrowser(boolean probe) {
		ObjStoreBrowser osb = new ObjStoreBrowser();

		osb.viewSubordinateAtomicActions(true);

		if (probe) {
			osb.start();
			osb.probe();
		}

		return osb;
	}

    protected void assertBeanWasCreated(ArjunaTransactionImple txn) {
        generatedHeuristicHazard(txn);

        ObjStoreBrowser osb = createObjStoreBrowser(true);

        UidWrapper w = osb.findUid(txn.get_uid());

        assertNotNull(w);
    }

    protected void generatedHeuristicHazard(ArjunaTransactionImple txn) {
		ThreadActionData.purgeActions();

		ExtendedCrashRecord recs[] = {
				new ExtendedCrashRecord(ExtendedCrashRecord.CrashLocation.NoCrash, ExtendedCrashRecord.CrashType.Normal),
				new ExtendedCrashRecord(ExtendedCrashRecord.CrashLocation.CrashInCommit, ExtendedCrashRecord.CrashType.HeuristicHazard)
		};

        txn.start();

        for (ExtendedCrashRecord rec : recs)
            txn.add(rec);

        txn.end(false);
    }
}
