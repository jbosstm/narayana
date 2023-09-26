/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

import com.arjuna.orbportability.Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;



public class SetupOTSServer2
{
    public final static String NAME_SERVICE_BIND_NAME_PROPERTY = "ots.server.bindname";

	public static void main(String[] args)
	{
		String bindName = System.getProperty(NAME_SERVICE_BIND_NAME_PROPERTY);

		if (bindName != null)
		{
			System.out.println("Looking up OTS Server '" + bindName + "'");

			try
			{
				String transactionServiceIOR = getService(bindName);

				ORBInterface.initORB(args, null);

				String[] transactionFactoryParams = new String[1];
				transactionFactoryParams[0] = ORBServices.otsKind;

				Services services = new Services(ORBInterface.getORB());

				services.registerService(ORBInterface.orb().string_to_object(transactionServiceIOR), ORBServices.transactionService, transactionFactoryParams);

                System.out.println("Ready");
			}
			catch (Exception e)
			{
				e.printStackTrace(System.err);
                System.out.println("Failed");
			}
		}
		else
		{
			System.out.println("Bind name '" + NAME_SERVICE_BIND_NAME_PROPERTY + "' not specified");
            System.out.println("Failed");
		}
	}

    public static String getService(String name) throws IOException
    {
        BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
        String returnValue = fin.readLine();
        fin.close();
        return returnValue;
    }
}