/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

import com.arjuna.orbportability.Services;
import org.omg.CORBA.ORBPackage.InvalidName;

import java.io.IOException;



public class ORBServices
{
	private static Services _services = null;

	public final static String transactionService = Services.transactionService;
	public final static String otsKind = com.arjuna.orbportability.Services.otsKind;

	public synchronized static org.omg.CORBA.Object getService(String name, Object[] params) throws IOException, InvalidName
	{
		if (_services == null)
		{
			_services = new Services(ORBInterface.getORB());
		}

		return _services.getService(name, params);
	}
}