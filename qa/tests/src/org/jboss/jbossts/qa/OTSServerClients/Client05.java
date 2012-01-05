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
//
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
//
// Arjuna Technologies Ltd.,
// Newcastle upon Tyne,
// Tyne and Wear,
// UK.
//

package org.jboss.jbossts.qa.OTSServerClients;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client05.java,v 1.2 2003/06/26 11:44:17 rbegg Exp $
 */

/*
 * Try to get around the differences between Ansi CPP and
 * K&R cpp with concatenation.
 */

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client05.java,v 1.2 2003/06/26 11:44:17 rbegg Exp $
 */


import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ORBServices;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

public class Client05
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			TransactionFactory transactionFactory = null;


			String[] transactionFactoryParams = new String[1];
			transactionFactoryParams[0] = ORBServices.otsKind;

			transactionFactory = TransactionFactoryHelper.narrow(ORBServices.getService(ORBServices.transactionService, transactionFactoryParams));


			boolean correct;
			Control control = transactionFactory.create(4);

			Thread.sleep(8000);

			try
			{
				control.get_terminator().commit(false);
				correct = false;
			}
			catch (INVALID_TRANSACTION invalidTransaction)
			{
				correct = true;
			}
			catch (BAD_OPERATION badOperation)
			{
				correct = true;
			}
			catch (org.omg.CORBA.OBJECT_NOT_EXIST object_not_exist_exception)
			{
				// This test creates a transaction with timeout period of 4 seconds then
				// sleeps for 8 seconds.
				// When the timeout goes off at the transaction service, the transaction is
				// rolled back and destroyed.
				// The subsequent call to commit on the transaction results in an
				// org.omg.CORBA.OBJECT_NOT_EXIST exception being thrown.
				// The JTS specification appears to be quite vague in this area, however our
				// implementation is compliant with this vagueness.
				// Hence, For the purposes of this test, org.omg.CORBA.OBJECT_NOT_EXIST being thrown
				// does not indicate a failure - BD 20/06/01

				correct = true;
			}
			catch (Exception exception)
			{
				System.err.println("Client05.main: commit exception = " + exception);
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
			System.err.println("Client05.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client05.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
