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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client01.java,v 1.4 2004/07/30 15:19:44 jcoleman Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery12Clients;

import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.jboss.jbossts.qa.CrashRecovery12Resources.Resource01;
import org.jboss.jbossts.qa.CrashRecovery12Resources.Resource02;

import javax.transaction.UserTransaction;

public class Client01
{
	public static String resultsFile = "Client01.log";

	public static void main(String[] args)
	{
		int crashIn = Resource01.NOCRASH;
		;

		if (args.length >= 1)
		{
			if (args[0].startsWith("p") || args[0].startsWith("P"))
			{
				crashIn = Resource01.PREPARE;
			}
			if (args[0].startsWith("c") || args[0].startsWith("C"))
			{
				crashIn = Resource01.COMMIT;
			}
			if (args[0].startsWith("r") || args[0].startsWith("R"))
			{
				crashIn = Resource01.ROLLBACK;
			}
		}
		if (args.length >= 2)
		{
			resultsFile = args[1];
		}

		try
		{
			ORB myORB = ORB.getInstance("Client01");
			RootOA myOA = OA.getRootOA(myORB);

			myORB.initORB(args, null);
			myOA.initOA();

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Failed");
		}
		try
		{
			UserTransaction ut = com.arjuna.ats.jta.UserTransaction.userTransaction();

			ut.begin();

			TransactionImple txImple = (TransactionImple) TransactionManager.transactionManager().getTransaction();

			txImple.enlistResource(new Resource01(crashIn, resultsFile));
			txImple.enlistResource(new Resource02());

			ut.commit();
			System.out.println("Passed");
		}
		catch (javax.transaction.RollbackException rbx)
		{
			System.out.println("Passed");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println("Failed");
		}

	}
}
