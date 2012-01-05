/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Created by IntelliJ IDEA.
 * User: peter craddock
 * Date: 12-Mar-02
 * Time: 14:19:06
 */
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.ErrorService01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class ErrorClient02 extends BaseTestClient
{
	public static void main(String[] args)
	{
		ErrorClient02 test = new ErrorClient02(args);
	}

	private ErrorClient02(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfResources(getNumberOfArgs());
			setCrashPoint(getNumberOfArgs() - 1);
			int[] mCrashType = null;

			try
			{

				mCrashType = new int[getNumberOfArgs() - 2];

				//first set defaults
				for (int i = 0; i < mCrashType.length; i++)
				{
					mCrashType[i] = 7; //default 'finished_ok'
				}

				//now populate with passed values
				for (int i = 0; i < mCrashType.length; i++)
				{
					//any exception will result in default array list being used
					mCrashType[i] = Integer.parseInt(getArg(i + 2));
				}

			}
			catch (NumberFormatException nfe)
			{
				Debug("Using default value of 0 : ", nfe);
			}
			catch (Exception e)
			{
				Debug("Using default value of 0 : ", e);
			}

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

//            Debug("final value = " + mFinalValue);
			//Debug(ActionStatus.printString(mFinalValue));
			Debug(mFinalValue + "");

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in ErrorClient02.test() :", e);
		}
	}

}
