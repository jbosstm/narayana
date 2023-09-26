/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashService01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client001b extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client001b test = new Client001b(args);
	}

	private Client001b(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(5);
			setNumberOfResources(4);
			setCrashPoint(3);
			setCrashType(2);
			setUniquePrefix(1);

			//create new container object
			CrashService01 mService = new CrashService01(mNumberOfResources);
			//create crash record so it is processed first
			mService.createCrashRecord(mCrashPoint, mCrashType);

			//start transaction	and do work
			startTx();
			mService.setupOper(getUniquePrefix());
			mService.doWork(mMaxIteration);

			ServerIORStore.storeIOR("CrashAbstractRecord", mAtom.get_uid().stringForm());

			commit();

			//we do not need to do anything else it should finish here if not print failed
			Fail();
		}
		catch (Exception e)
		{
			Fail("Error in Client001b.test() :", e);
		}
	}

}