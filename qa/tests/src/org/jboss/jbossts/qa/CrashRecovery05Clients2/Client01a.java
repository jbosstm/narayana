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

package org.jboss.jbossts.qa.CrashRecovery05Clients2;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client01a.java,v 1.2 2003/06/26 11:43:31 rbegg Exp $
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
 * $Id: Client01a.java,v 1.2 2003/06/26 11:43:31 rbegg Exp $
 */


import org.jboss.jbossts.qa.CrashRecovery05.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.jboss.jbossts.qa.Utils.CrashRecoveryDelays;

public class Client01a extends ClientBase
{
	public static void main(String[] args)
	{
		try
		{
			init(args, null);

			String serviceIOR1 = ServerIORStore.loadIOR(args[args.length - 2]);
			AfterCrashService service1 = AfterCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR1));

			String serviceIOR2 = ServerIORStore.loadIOR(args[args.length - 1]);
			AfterCrashService service2 = AfterCrashServiceHelper.narrow(ORBInterface.orb().string_to_object(serviceIOR2));

			CheckBehavior[] checkBehaviors1 = new CheckBehavior[1];
			checkBehaviors1[0] = new CheckBehavior();
			checkBehaviors1[0].allow_done = true;
			checkBehaviors1[0].allow_returned_prepared = true;
			checkBehaviors1[0].allow_returned_committing = true;
			checkBehaviors1[0].allow_returned_committed = true;
			checkBehaviors1[0].allow_returned_rolledback = false;
			checkBehaviors1[0].allow_raised_not_prepared = false;

			CheckBehavior[] checkBehaviors2 = new CheckBehavior[1];
			checkBehaviors2[0] = new CheckBehavior();
			checkBehaviors2[0].allow_done = false;
			checkBehaviors2[0].allow_returned_prepared = true;
			checkBehaviors2[0].allow_returned_committing = true;
			checkBehaviors2[0].allow_returned_committed = true;
			checkBehaviors2[0].allow_returned_rolledback = false;
			checkBehaviors2[0].allow_raised_not_prepared = false;

			boolean correct = true;

			service1.setup_oper(1);
			service2.setup_oper(1);

			correct = updateResult(correct, service1.check_oper(checkBehaviors1));
			correct = updateResult(correct, service2.check_oper(checkBehaviors2));
			correct = updateResult(correct, service1.is_correct());
			correct = updateResult(correct, service2.is_correct());

			CrashRecoveryDelays.awaitReplayCompletionCR05(5); // scale factor could maybe be reduced

			ResourceTrace resourceTrace1 = service1.get_resource_trace(0);
			ResourceTrace resourceTrace2 = service2.get_resource_trace(0);

			System.out.printf("Client01a check: resourceTrace1=%d resourceTrace2=%d%n", resourceTrace1.value(), resourceTrace2.value());

			correct = updateResult(correct, ((resourceTrace1 == ResourceTrace.ResourceTraceNone) || (resourceTrace1 == ResourceTrace.ResourceTraceCommit)));
			correct = updateResult(correct, (resourceTrace2 == ResourceTrace.ResourceTraceCommit));

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
			System.err.println("Client01a.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try {
			fini();
		}
		catch (Exception exception)
		{
			System.err.println("Client01a.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}

	private static int step = 0;

	private static boolean updateResult(boolean prev, boolean value) {
		step += 1;
		System.out.printf("Client01a check: step %d %b%n", step, value);
		return prev && value;
	}
}
