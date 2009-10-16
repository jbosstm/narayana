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
package org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashAbstractRecord;
import org.jboss.jbossts.qa.ArjunaCore.LockManager.impl.BasicLockRecord;
import org.jboss.jbossts.qa.ArjunaCore.Utils.BaseTestClient;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ActivateDestroyTest1.java,v 1.2 2003/12/01 13:37:53 nmcl Exp $
 */

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
