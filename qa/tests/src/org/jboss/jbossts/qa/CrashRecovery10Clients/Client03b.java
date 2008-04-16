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

package org.jboss.jbossts.qa.CrashRecovery10Clients;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Client03b.java,v 1.2 2003/06/26 11:43:46 rbegg Exp $
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
 * $Id: Client03b.java,v 1.2 2003/06/26 11:43:46 rbegg Exp $
 */


import com.arjuna.ats.arjuna.coordinator.AddOutcome;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import org.jboss.jbossts.qa.CrashRecovery10.*;
import org.jboss.jbossts.qa.CrashRecovery10Impls.StartCrashAbstractRecordImpl;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.OTS;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Client03b
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

			service1.set(OTS.current().get_control(), 0);
			service2.set(OTS.current().get_control(), 0);

			OTS.current().commit(true);

			OTS.current().begin();

			service1.set(OTS.current().get_control(), 1);
			service2.set(OTS.current().get_control(), 1);

			correct = (BasicAction.Current().add(new StartCrashAbstractRecordImpl(StartCrashAbstractRecordImpl.CRASH_IN_COMMIT)) == AddOutcome.AR_ADDED);

			if (correct)
			{
				OTS.current().commit(true);
			}

			System.out.println("Failed");
		}
		catch (Exception exception)
		{
			System.out.println("Failed");
			System.err.println("Client03b.main: " + exception);
			exception.printStackTrace(System.err);
		}

		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.println("Client03b.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
