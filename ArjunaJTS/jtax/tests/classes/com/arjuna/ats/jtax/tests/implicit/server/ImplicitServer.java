/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.ats.jtax.tests.implicit.server;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ImplicitServer.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.mwlabs.testframework.unittest.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jtax.tests.implicit.impl.*;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.common.Environment;

public class ImplicitServer extends Test
{
    public void run(String[] args)
    {
        if (args.length == 0)
        {
            System.err.println("No name provided for server");
            assertFailure();
        }
        else
        {
            jtaPropertyManager.propertyManager.setProperty(Environment.JTA_TM_IMPLEMENTATION, com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
            jtaPropertyManager.propertyManager.setProperty(Environment.JTA_UT_IMPLEMENTATION, com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

            try
            {
                ORB orb = ORB.getInstance("implicitserver-orb");
                OA oa = OA.getRootOA(orb);

                orb.initORB(args, null);
                oa.initPOA(args);

                RemoteImpl impl = new RemoteImpl();

                oa.objectIsReady(impl);

                org.omg.CORBA.Object obj = oa.corbaReference(impl);

                registerService(args[0], orb.orb().object_to_string(obj));

                assertReady();

                orb.orb().run();
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                assertFailure();
            }
        }
    }

    public static void main(String[] args)
    {
        ImplicitServer test = new ImplicitServer();
        test.initialise(null, null, args, new LocalHarness());

        test.runTest();
    }
}
