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
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Test03.java,v 1.1 2004/10/13 15:45:47 nmcl Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery13Clients;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.jboss.jbossts.qa.CrashRecovery13Impls.ExampleXAResource;

public class Test03
{
	public static void main(String[] args) throws Exception
	{
		System.setProperty("XAConnectionRecovery1", "ExampleXAConnectionRecovery");

		ORB myORB = null;
		RootOA myOA = null;

		try
		{
			myORB = ORB.getInstance("test");
			myOA = OA.getRootOA(myORB);

			myORB.initORB(args, null);
			myOA.initOA();

			ORBManager.setORB(myORB);
			ORBManager.setPOA(myOA);
		}
		catch (Exception e)
		{
			System.err.println("Initialisation failed: " + e);

			System.exit(0);
		}

		com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple rm = new com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple(true);

		try
		{
			Thread.sleep(140000);
		}
		catch (Exception ex)
		{
		}

		if (ExampleXAResource.passed)
		{
			System.out.println("Passed.");
		}
		else
		{
			System.out.println("Failed.");
		}
		System.clearProperty("XAConnectionRecovery1");
	}
}
