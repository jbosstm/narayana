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
package org.jboss.jbossts.qa.Utils;

import org.jboss.dtf.testframework.unittest.Test;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technology Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RegisterOTSServer.java,v 1.2 2003/06/26 11:45:07 rbegg Exp $
 */

public class RegisterOTSServer extends Test
{
	public final static String NAME_SERVICE_BIND_NAME_PROPERTY = "ots.server.bindname";

	public void run(String[] args)
	{
		String bindName = System.getProperty(NAME_SERVICE_BIND_NAME_PROPERTY);

		if (bindName != null)
		{
			logInformation("Registering OTS Server '" + bindName + "'");

			try
			{
				ORBInterface.initORB(args, null);

				String[] transactionFactoryParams = new String[1];
				transactionFactoryParams[0] = ORBServices.otsKind;

				TransactionFactory transactionFactory = TransactionFactoryHelper.narrow(ORBServices.getService(ORBServices.transactionService, transactionFactoryParams));

				registerService(bindName, ORBInterface.orb().object_to_string(transactionFactory));

				assertReady();
				assertSuccess();
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
				assertFailure();
			}
		}
		else
		{
			logInformation("Bind name '" + NAME_SERVICE_BIND_NAME_PROPERTY + "' not specified");
			assertFailure();
		}
	}
}
