/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ServiceTestSetup.java 2342 2006-03-30 13:06:17Z  $
 */
package com.hp.mwtests.orbportability.services;

import com.arjuna.orbportability.Services;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.hp.mwtests.orbportability.orbspecific.orbinstance.SimpleObjectImpl;

import org.junit.Test;
import static org.junit.Assert.*;

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

