/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class RestoreClient001a extends BaseTestClient
{
	public static void main(String[] args)
	{
		RestoreClient001a test = new RestoreClient001a(args);
	}

	private RestoreClient001a(String[] args)
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
					Debug("Error when creating ior store");
					mCorrect = false;
				}
			}

			//check if objects and final values have been restored.
			for (int j = 0; j < mNumberOfResources; j++)
			{
				if (mStateRecordList[j].getValue() != mMaxIteration)
				{
					mCorrect = false;
					Debug("the value has not been retored: " + mStateRecordList[j].getValue());
					break;
				}
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in RestoreClient001a.test() :", e);
		}
	}

}