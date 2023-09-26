/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

import com.arjuna.orbportability.OA;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.SystemException;
import org.omg.PortableServer.Servant;



public class OAInterface
{
	private static OA _oa;

    public static void initializeOA() throws InvalidName, SystemException
    {
        _oa = OA.getRootOA(ORBInterface.getORB());
        _oa.initPOA();
    }

	public static void initOA()
	{
		_oa = OA.getRootOA(ORBInterface.getORB());

		try
		{
			_oa.initPOA();
		}
		catch (Exception e)
		{
			System.err.println("Failed to initialise OA: " + e);
		}
	}

	public static void objectIsReady(Servant s)
	{
		_oa.objectIsReady(s);
	}

	public static org.omg.CORBA.Object corbaReference(Servant obj)
	{
		return _oa.corbaReference(obj);
	}

	public static void shutdownOA()
	{
		_oa.destroy();
	}
}