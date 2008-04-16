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
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashService02;
import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.RecoveryTransaction;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;

public class Client001 extends BaseTestClient
{
	public static void main(String[] args)
	{
		Client001 test = new Client001(args);
	}

	private Client001(String[] args)
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

			CrashService02 mService = new CrashService02(mNumberOfResources, mCrashPoint, mCrashType);

			//start transaction	to check all is ok.
			startTx();
			mService.setupOper(getUniquePrefix());
			mService.doWork(mMaxIteration);
			commit();

			for (int ii = 0; ii < mNumberOfResources; ii++)
			{
				mService.mAbstractRecordList[ii].resetValue();
			}

			RecoveryTransaction tx = new RecoveryTransaction(mAtom.get_uid());

			tx.doCommit();

			try
			{
				for (int i = 0; i < mNumberOfResources; i++)
				{
					if (mService.mAbstractRecordList[i].getValue() != mMaxIteration * mNumberOfResources)
					{
						Debug("Error checking resource " + i + " value  = " + mService.mAbstractRecordList[i].getValue());
						mCorrect = false;

						qaAssert(false);
					}
				}
			}
			catch (Exception e)
			{
				Fail("Exception whilst checking resource", e);
			}

			qaAssert(mCorrect);
		}
		catch (Exception e)
		{
			Fail("Error in Client001.test() :", e);
		}
	}

}
