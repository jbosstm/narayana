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
package org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.StateManager.impl.BasicStateRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

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

			//sleep
			qautil.sleep();

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
