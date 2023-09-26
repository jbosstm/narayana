/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashAbstractRecord;
import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import com.arjuna.ats.arjuna.common.arjPropertyManager;



public class ActivateDestroyTest1 extends BaseTestClient
{
	public static void main(String[] args)
	{
        /*
        * Default intentions list is to order by Uid (improves
        * performance). But for this test we need to order by type.
        */
        arjPropertyManager.getCoordinatorEnvironmentBean().setAlternativeRecordOrdering(true);

		ActivateDestroyTest1 test = new ActivateDestroyTest1(args);
	}

	private ActivateDestroyTest1(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		/** Set argument relative positions **/
		setNumberOfCalls(2);
		setNumberOfResources(1);

		try
		{

			BasicLockRecord basicRecord = new BasicLockRecord();

			System.out.println("created object " + basicRecord.get_uid());

			this.startTx();

			System.out.println("basicRecord.increase()");
			basicRecord.increase(1, 0);

			System.out.println("basicRecord.destroy()");
			basicRecord.destroy();

			CrashAbstractRecord crashRecord = new CrashAbstractRecord(1, 0);
			this.add(crashRecord);

			this.commit();

			this.Fail();
		}
		catch (Exception e)
		{
			Fail("Error doing work", e);
		}
	}
}