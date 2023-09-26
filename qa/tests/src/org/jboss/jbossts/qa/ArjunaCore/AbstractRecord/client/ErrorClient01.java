/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 12-Mar-02
 * Time: 14:19:06
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.ErrorService01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class ErrorClient01 extends BaseTestClient
{
	public static void main(String[] args)
	{
		ErrorClient01 test = new ErrorClient01(args);
	}

	private ErrorClient01(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfResources(3);
			setCrashPoint(2);
			setCrashType(1);

			ErrorService01 mService = new ErrorService01(mNumberOfResources);
			int mFinalValue = 0;

			createTx();
			//com.arjuna.ats.arjuna.logging.debug.DebugController.controller().println(0, 0, 0, "tests");
			try
			{
				begin();
				mService.setupOper();
				mService.setCrash(mCrashPoint, mCrashType);
				mFinalValue = intCommit();
			}
			catch (Exception e)
			{
				Fail("Error doing work", e);
			}

			Debug("final value = " + mFinalValue);
			Debug(ActionStatus.stringForm(mFinalValue));

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in ErrorClient01.test() :", e);
		}
	}

}