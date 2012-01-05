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
 * $Id: Test02.java,v 1.1 2004/09/20 15:25:17 nmcl Exp $
 */

package org.jboss.jbossts.qa.CrashRecovery13Clients;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.xa.XidImple;

import java.util.Vector;

public class Test02
{
	public static void main(String[] args)
	{
		Vector xaRecoveryNodes = new Vector();
		boolean passed = false;
		Uid bogusNodeName = new Uid();

		xaRecoveryNodes.add("2");

		System.err.println("Bogus XA node name: " + bogusNodeName);

		XidImple xid = new XidImple(new Uid());
		String nodeName = XAUtils.getXANodeName(xid);

		// should fail.

		System.err.println("XA node name: " + nodeName);
		System.err.println("Xid to recover: " + xid);

		if (!xaRecoveryNodes.contains(nodeName))
		{
			passed = true;
		}

		if (passed)
		{
			System.err.println("Passed.");
		}
		else
		{
			System.err.println("Failed.");
		}
	}

}
