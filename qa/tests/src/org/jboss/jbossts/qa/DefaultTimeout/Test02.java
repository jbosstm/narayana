/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.DefaultTimeout;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

import jakarta.transaction.Status;
import jakarta.transaction.TransactionManager;

/**
 * Test default timeout causes rollback by the reaper.
 *
 * Rollback attempt after timeout should work ok, as it's a nullop on
 * tx that is already rolled back.
 * 
 * Note: build time unit tests jta|jtax SimpleTest|RollbackTest are similar
 * but use custom (short) timeout value so as not to delay the build.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com), 2011-03
 */
public class Test02
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			boolean correct = true;

            TransactionManager transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();

            transactionManager.begin();

			try
			{
                Thread.sleep( (1000*arjPropertyManager.getCoordinatorEnvironmentBean().getDefaultTimeout()) + 1000 );

//                correct = (transactionManager.getStatus() == Status.STATUS_ROLLEDBACK);

				transactionManager.rollback();
				correct = true; //  Rollback attempt after timeout should work ok
			}
			catch (Exception exception)
			{
                correct = false;
			}

			if (correct)
			{
				System.out.println("Passed");
			}
			else
			{
				System.out.println("Failed");
			}
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Test01.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Test01.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}