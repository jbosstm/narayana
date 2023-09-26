/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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