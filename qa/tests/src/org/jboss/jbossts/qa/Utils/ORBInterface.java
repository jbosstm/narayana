/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

import com.arjuna.orbportability.ORB;

import java.util.Properties;



public class ORBInterface
{
	private final static String ORB_NAME = "ats-qa-orb";

	private static ORB _orb = null;

	public static void initORB(String[] params, Properties props)
	{
		_orb = ORB.getInstance(ORB_NAME);
		_orb.initORB(params, props);
	}

	public static ORB getORB()
	{
		return _orb;
	}

	public static org.omg.CORBA.ORB orb()
	{
		return _orb.orb();
	}

	public static void run()
	{
		_orb.orb().run();
	}

	public static void shutdownORB()
	{
		_orb.shutdown();
	}
}