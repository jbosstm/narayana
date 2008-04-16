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

package org.jboss.jbossts.qa.RawResources02Clients2;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client088.java,v 1.2 2003/06/26 11:44:46 rbegg Exp $
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
 * $Id: Client088.java,v 1.2 2003/06/26 11:44:46 rbegg Exp $
 */


import org.jboss.jbossts.qa.RawResources02.*;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;
import org.omg.CosTransactions.HeuristicHazard;

public class Client088
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

			ResourceBehavior[] resourceBehaviors1 = new ResourceBehavior[1];
			resourceBehaviors1[0] = new ResourceBehavior();
			resourceBehaviors1[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteReadOnly;
			resourceBehaviors1[0].rollback_behavior = RollbackBehavior.RollbackBehaviorReturn;
			resourceBehaviors1[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors1[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorReturn;

			ResourceBehavior[] resourceBehaviors2 = new ResourceBehavior[1];
			resourceBehaviors2[0] = new ResourceBehavior();
			resourceBehaviors2[0].prepare_behavior = PrepareBehavior.PrepareBehaviorReturnVoteRollback;
			resourceBehaviors2[0].rollback_behavior = RollbackBehavior.RollbackBehaviorRaiseHeuristicCommit;
			resourceBehaviors2[0].commit_behavior = CommitBehavior.CommitBehaviorReturn;
			resourceBehaviors2[0].commitonephase_behavior = CommitOnePhaseBehavior.CommitOnePhaseBehaviorReturn;

			boolean correct = true;

			System.err.println("begin transaction");
			OTS.current().begin();

			service1.oper(resourceBehaviors1, OTS.current().get_control());

			service2.oper(resourceBehaviors2, OTS.current().get_control());

			ResourceTrace expectedResourceTrace1 = ResourceTrace.ResourceTracePrepare;

			try
			{
				System.err.println("commit transaction");
				OTS.current().commit(true);
				System.err.println("transaction committed, no HeuristicHazard exception thrown - test failed");
				correct = false;
			}
			catch (HeuristicHazard heuristicHazard)
			{
				// No exception used to be thrown, but this has been changed to HeuristicHazard from TeT 2.2.2.
				// MKS 17/06/2002
				System.err.println("expected HeuristicHazard exception thrown");
			}

			correct = correct && service1.is_correct() && service2.is_correct();

			ResourceTrace resourceTrace1 = service1.get_resource_trace(0);
			ResourceTrace resourceTrace2 = service2.get_resource_trace(0);

			correct = correct && (resourceTrace1 == expectedResourceTrace1);
			System.err.println("resourceTrace1 = " + resourceTrace1.value());

			correct = correct && (resourceTrace2 == ResourceTrace.ResourceTracePrepareRollbackForget);
			System.err.println("resourceTrace2 = " + resourceTrace2.value());

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
			System.err.println("Client088.main: " + exception);
			exception.printStackTrace(System.err);
			System.out.println("Failed");
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client088.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
