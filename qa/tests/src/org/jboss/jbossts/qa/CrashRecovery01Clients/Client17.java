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

package org.jboss.jbossts.qa.CrashRecovery01Clients;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client17.java,v 1.2 2003/06/26 11:43:16 rbegg Exp $
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
 * $Id: Client17.java,v 1.2 2003/06/26 11:43:16 rbegg Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery01.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

public class Client17
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
			Service service1 = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR1));

			String serviceIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
			Service service2 = ServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR2));

			boolean correct = true;

			OTS.current().begin();

			service1.setup_oper(1);
			service2.setup_oper(1);

			OTS.current().rollback_only();

			try
			{
				OTS.current().commit(true);
				correct = false;
			}
			catch (TRANSACTION_ROLLEDBACK transactionRolledBack)
			{
			}
//  code changed to cope with recovery manager fix
// 	that makes reply_completion cause resource to rollback even though
//	transaction has completed
			ResourceTrace resourceTrace1 = service1.get_resource_trace(0);
			ResourceTrace resourceTrace2 = service2.get_resource_trace(0);

//  trace should be rollback
			correct = correct && (resourceTrace1 == ResourceTrace.ResourceTraceRollback);
			correct = correct && (resourceTrace2 == ResourceTrace.ResourceTraceRollback);

//  check_oper will invoke reply_completion and check the state of the transaction
			correct = correct && service1.check_oper();
			correct = correct && service2.check_oper();

// now sleep to let reply completion do its job 1 second should be more than enough
			Thread.sleep(10 * 1000);

			correct = correct && service1.is_correct();
			correct = correct && service2.is_correct();
//  after reply_completion is called the resource will have rollback called on
//	it again, changing the ResourceTrace to ResourceTrace.ResourceTraceUnknown
			resourceTrace1 = service1.get_resource_trace(0);
			resourceTrace2 = service2.get_resource_trace(0);

			correct = correct && (resourceTrace1 == ResourceTrace.ResourceTraceUnknown);
			correct = correct && (resourceTrace2 == ResourceTrace.ResourceTraceUnknown);

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
			System.err.println("Client17.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client17.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
