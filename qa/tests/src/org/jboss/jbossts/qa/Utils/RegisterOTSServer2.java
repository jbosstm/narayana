/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;



public class RegisterOTSServer2
{
	public final static String NAME_SERVICE_BIND_NAME_PROPERTY = "ots.server.bindname";

	public static void main(String[] args)
	{
		String bindName = System.getProperty(NAME_SERVICE_BIND_NAME_PROPERTY);

		if (bindName != null)
		{
			System.out.println("Registering OTS Server '" + bindName + "'");

			try
			{
				ORBInterface.initORB(args, null);

				String[] transactionFactoryParams = new String[1];
				transactionFactoryParams[0] = ORBServices.otsKind;

				TransactionFactory transactionFactory = TransactionFactoryHelper.narrow(ORBServices.getService(ORBServices.transactionService, transactionFactoryParams));

				registerService(bindName, ORBInterface.orb().object_to_string(transactionFactory));

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

    public static void registerService(String name, String ior) throws IOException
    {
        File file = new File(name);
        if(file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(ior.getBytes());
        fout.close();
    }
}