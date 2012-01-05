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
// UK
//

package org.jboss.jbossts.qa.AITResources02Servers;

/*
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Server06.java,v 1.2 2003/06/26 11:43:14 rbegg Exp $
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
 * $Id: Server06.java,v 1.2 2003/06/26 11:43:14 rbegg Exp $
 */


import org.jboss.jbossts.qa.AITResources02.*;
import org.jboss.jbossts.qa.AITResources02Impls.AITCounterImpl01;
import org.jboss.jbossts.qa.AITResources02Impls.AITCounterImpl02;
import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;
import org.jboss.jbossts.qa.Utils.ServerIORStore;

public class Server06
{
	public static void main(String args[])
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();

			AITCounterImpl01 aitCounterImpl1 = new AITCounterImpl01();
			AITCounterImpl02 aitCounterImpl2 = new AITCounterImpl02();
			AITCounterImpl01 aitCounterImpl3 = new AITCounterImpl01();
			AITCounterImpl02 aitCounterImpl4 = new AITCounterImpl02();

			CounterPOATie servant1 = new CounterPOATie(aitCounterImpl1);
			CounterPOATie servant2 = new CounterPOATie(aitCounterImpl2);
			CounterPOATie servant3 = new CounterPOATie(aitCounterImpl3);
			CounterPOATie servant4 = new CounterPOATie(aitCounterImpl4);

			OAInterface.objectIsReady(servant1);
			OAInterface.objectIsReady(servant2);
			OAInterface.objectIsReady(servant3);
			OAInterface.objectIsReady(servant4);
			Counter aitCounter1 = CounterHelper.narrow(OAInterface.corbaReference(servant1));
			Counter aitCounter2 = CounterHelper.narrow(OAInterface.corbaReference(servant2));
			Counter aitCounter3 = CounterHelper.narrow(OAInterface.corbaReference(servant3));
			Counter aitCounter4 = CounterHelper.narrow(OAInterface.corbaReference(servant4));

			ServerIORStore.storeIOR(args[args.length - 4], ORBInterface.orb().object_to_string(aitCounter1));
			ServerIORStore.storeIOR(args[args.length - 3], ORBInterface.orb().object_to_string(aitCounter2));
			ServerIORStore.storeIOR(args[args.length - 2], ORBInterface.orb().object_to_string(aitCounter3));
			ServerIORStore.storeIOR(args[args.length - 1], ORBInterface.orb().object_to_string(aitCounter4));

			System.out.println("Ready");

			ORBInterface.run();
		}
		catch (Exception exception)
		{
			System.err.println("Server06.main: " + exception);
			exception.printStackTrace(System.err);
		}
	}
}
