/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.qa.DefaultTimeout;

import com.arjuna.ats.arjuna.common.arjPropertyManager;

import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

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

                correct = (transactionManager.getStatus() == Status.STATUS_ROLLEDBACK);

				transactionManager.rollback();
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