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

package org.jboss.jbossts.qa.CrashRecovery05Clients1;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client02a.java,v 1.2 2003/06/26 11:43:30 rbegg Exp $
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
 * $Id: Client02a.java,v 1.2 2003/06/26 11:43:30 rbegg Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery05.*;
import org.jboss.jbossts.qa.CrashRecovery05Utils.Delays;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client02a
{
	public static void main(String[] args)
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			String serviceIOR = ServerIORStore.loadIOR(args[args.length - 1]);
			AfterCrashService service = AfterCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR));

			CheckBehavior[] checkBehaviors = new CheckBehavior[1];
			checkBehaviors[0] = new CheckBehavior();
			checkBehaviors[0].allow_done = false;
			checkBehaviors[0].allow_returned_prepared = false;
			checkBehaviors[0].allow_returned_committing = true;
			checkBehaviors[0].allow_returned_committed = true;
			checkBehaviors[0].allow_returned_rolledback = true;
			checkBehaviors[0].allow_raised_not_prepared = false;

			boolean correct = true;

			service.setup_oper(1);

			correct = service.check_oper(checkBehaviors) && service.is_correct();

			Thread.sleep(Delays.replyCompletionDelay());

			ResourceTrace resourceTrace = service.get_resource_trace(0);

			correct = correct && ((resourceTrace == ResourceTrace.ResourceTraceCommit) || (resourceTrace == ResourceTrace.ResourceTraceRollback));

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
			System.err.println("Client02a.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client02a.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
