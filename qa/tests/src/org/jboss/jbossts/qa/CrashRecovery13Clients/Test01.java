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
 * $Id: Test01.java,v 1.1 2004/09/20 15:25:17 nmcl Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery13Clients;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;

public class Test01
{
	public static void main(String[] args)
	{
		System.setProperty("com.arjuna.ats.jta.xaRecoveryNode", "1");
		System.setProperty("XAResourceRecovery1", "com.hp.mwtests.ts.jta.recovery.DummyXARecoveryResource");

		try
		{
			RecoveryManager manager = RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);

			manager.scan();
			manager.scan();

			System.out.println("Passed.");
		}
		catch (Exception ex)
		{
			System.out.println("Failed.");
		}
		System.clearProperty("com.arjuna.ats.jta.xaRecoveryNode");
		System.clearProperty("XAResourceRecovery1");
	}

}
