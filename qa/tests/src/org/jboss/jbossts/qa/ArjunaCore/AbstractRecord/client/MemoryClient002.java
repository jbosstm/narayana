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
package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.impl.Service01;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import org.jboss.jbossts.qa.ArjunaCore.Utils.qautil;

public class MemoryClient002 extends BaseTestClient
{
	public static void main(String[] args)
	{
		MemoryClient002 test = new MemoryClient002(args);
	}

	private MemoryClient002(String[] args)
	{
		super(args);
	}

	public void Test()
	{
		try
		{
			setNumberOfCalls(3);
			setNumberOfResources(2);
			getClientThreshold(1);

			Service01 mService = new Service01(mNumberOfResources);
			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			commit();

			//lets go to sleep to see if this helps the vm clean itself up
			qautil.runGarbageCollection();

			//get first memory reading.
			getFirstReading();

			mService = new Service01(mNumberOfResources);
			startTx();
			mService.setupOper(true);
			mService.doWork(mMaxIteration);
			abort();

			//lets go to sleep to see if this helps the vm clean itself up
			qautil.runGarbageCollection();

			getSecondReading();

			qaMemoryAssert();
		}
		catch (Exception e)
		{
			Fail("Error in MemoryClient002.test() :", e);
		}
	}

}
