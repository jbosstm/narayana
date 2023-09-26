/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.Utils;

public class OrbSetup implements Setup
{
	public void start(String[] args) throws Exception, Error
	{
		try
		{
			ORBInterface.initORB(args, null);
			OAInterface.initOA();
		}
		catch (Exception exception)
		{
			System.err.print("OrbSetup.start: ");
			exception.printStackTrace(System.err);

			throw exception;
		}
		catch (Error error)
		{
			System.err.print("OrbSetup.start: ");
			error.printStackTrace(System.err);

			throw error;
		}
	}

	public void stop() throws Exception, Error
	{
		try
		{
			OAInterface.shutdownOA();
			ORBInterface.shutdownORB();
		}
		catch (Exception exception)
		{
			System.err.print("OrbSetup.stop: ");
			exception.printStackTrace(System.err);

			throw exception;
		}
		catch (Error error)
		{
			System.err.print("OrbSetup.stop: ");
			error.printStackTrace(System.err);

			throw error;
		}
	}

}