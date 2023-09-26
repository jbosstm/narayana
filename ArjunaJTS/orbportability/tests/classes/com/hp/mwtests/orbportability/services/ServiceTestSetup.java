/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.orbportability.services;

import org.junit.Test;

import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.orbportability.orbspecific.orbinstance.SimpleObjectImpl;

public class ServiceTestSetup
{
	private final static String ORB_INSTANCE_NAME = "servicetest-orb";
	private final static String TEST_SERVICE_NAME = "com.mwtests.orbportability.services.ServiceTestSetup.TestService";

    @Test
	public void test() throws Exception
	{
			/** Create ORB and OA **/
			ORB testORB = ORB.getInstance( ORB_INSTANCE_NAME );
			OA testOA = OA.getRootOA( testORB );

			/** Initialise ORB and OA **/
			testORB.initORB(new String[] {}, null);
			testOA.initPOA(new String[] {});

			/** Create services object **/
			Services testServ = new Services(testORB);

			String[] params = new String[1];
			params[0] = com.arjuna.orbportability.Services.otsKind;

			SimpleObjectImpl servant = new com.hp.mwtests.orbportability.orbspecific.orbinstance.SimpleObjectImpl();

			testOA.objectIsReady(servant);

			/*
			 * Register using the default mechanism.
			 */
			testServ.registerService(com.hp.mwtests.orbportability.orbspecific.orbtests.SimpleObjectHelper.narrow(testOA.corbaReference(servant)), TEST_SERVICE_NAME, params, Services.CONFIGURATION_FILE);
	}
}