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
package com.arjuna.ats.jtax.tests.implicit.client;

/*
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ImplicitClient.java 2342 2006-03-30 13:06:17Z  $
 */

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.ats.jta.common.*;
import com.arjuna.ats.jta.*;

import org.jboss.dtf.testframework.unittest.Test;
import org.jboss.dtf.testframework.unittest.LocalHarness;

public class ImplicitClient extends Test
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
            try
            {
                jtaPropertyManager.getPropertyManager().setProperty(Environment.JTA_TM_IMPLEMENTATION, com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
                jtaPropertyManager.getPropertyManager().setProperty(Environment.JTA_UT_IMPLEMENTATION, com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

                ORB orb = ORB.getInstance("implicitserver-orb");
                OA oa = OA.getRootOA(orb);

                orb.initORB(args, null);
                oa.initPOA(args);

                org.omg.CORBA.Object obj = orb.orb().string_to_object(getService(args[0]));

                Example.test test = Example.testHelper.narrow(obj);

                TransactionManager.transactionManager().begin();

                test.invoke();

                TransactionManager.transactionManager().commit();

                assertSuccess();

                oa.destroy();
                orb.destroy();
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
        ImplicitClient client = new ImplicitClient();
        client.initialise(null, null, args, new LocalHarness());
        client.runTest();
    }
}
