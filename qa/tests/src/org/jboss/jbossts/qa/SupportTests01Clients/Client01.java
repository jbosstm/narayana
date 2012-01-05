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

package org.jboss.jbossts.qa.SupportTests01Clients;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client01.java,v 1.2 2003/06/26 11:45:05 rbegg Exp $
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
 * $Id: Client01.java,v 1.2 2003/06/26 11:45:05 rbegg Exp $
 */


import org.jboss.jbossts.qa.AITResources01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client01
{
	public static String status_text_from_int(int status)
	{
		switch (status)
		{
			case javax.transaction.Status.STATUS_ACTIVE:
				return ("STATUS_ACTIVE");
			case javax.transaction.Status.STATUS_COMMITTED:
				return ("STATUS_COMMITTED");
			case javax.transaction.Status.STATUS_COMMITTING:
				return ("STATUS_COMMITTING");
			case javax.transaction.Status.STATUS_MARKED_ROLLBACK:
				return ("STATUS_MARKED_ROLLBACK");
			case javax.transaction.Status.STATUS_NO_TRANSACTION:
				return ("STATUS_NO_TRANSACTION");
			case javax.transaction.Status.STATUS_PREPARED:
				return ("STATUS_PREPARED");
			case javax.transaction.Status.STATUS_PREPARING:
				return ("STATUS_PREPARING");
			case javax.transaction.Status.STATUS_ROLLEDBACK:
				return ("STATUS_ROLLEDBACK");
			case javax.transaction.Status.STATUS_ROLLING_BACK:
				return ("STATUS_ROLLING_BACK");
			case javax.transaction.Status.STATUS_UNKNOWN:
				return ("STATUS_UNKNOWN");
		}
		return ("!!ERROR!!");
	}

	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

			String counterIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			Counter counter = CounterHelper.narrow(ORBInterface.orb().string_to_object(counterIOR));

			tm.begin();

			javax.transaction.Transaction transaction = tm.getTransaction();

			counter.increase();

			tm.commit();

			System.err.println("Transaction Status (reported by actual transaction): " + status_text_from_int(transaction.getStatus()));
			System.err.println("Transaction Status (reported by transaction manager): " + status_text_from_int(tm.getStatus()));

			System.out.println("Passed");
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client04.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client04.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
