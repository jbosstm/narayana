/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

public class Client001a extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client001a test = new Client001a(args);
	}

	private Client001a(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(3);
			setNumberOfResources(2);
			setUniquePrefix(1);

            CrashRecoveryDelays.awaitRecoveryArjunaCore();

			//restore objects from uid's
			BasicStateRecord[] mStateRecordList = new BasicStateRecord[mNumberOfResources];
			for (int j = 0; j < mNumberOfResources; j++)
			{
				String key = getResourceName("resource_" + j);
				try
				{
					mStateRecordList[j] = new BasicStateRecord(qautil.loadUid(key));
					qautil.clearUid(key);
				}
				catch (Exception e)
				{
					Debug("Error when reading uid store");
					mCorrect = false;
				}
			}

			//record should hav ebeen commited by recovery manager
			for (int j = 0; j < mNumberOfResources; j++)
			{
				if (mStateRecordList[j].getValue() != (mMaxIteration + 1))
				{
					Debug("value is incorrect: " + mStateRecordList[j].getValue());
					mCorrect = false;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client001a.test() :", e);
		}
	}

}