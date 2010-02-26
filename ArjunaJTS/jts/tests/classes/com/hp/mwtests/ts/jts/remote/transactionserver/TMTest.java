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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TMTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.transactionserver;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.ORBManager;

import org.omg.CosTransactions.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class TMTest
{
    @Test
    public void test() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);


        TransactionFactory theOTS = null;
        Control topLevelControl = null;
        Services serv = new Services(myORB);

        int resolver = Services.getResolver();
        
        try
        {
            String[] params = new String[1];

            params[0] = Services.otsKind;

            org.omg.CORBA.Object obj = serv.getService(Services.transactionService, params, resolver);

            params = null;
            theOTS = TransactionFactoryHelper.narrow(obj);
        }
        catch (Exception e)
        {
            fail("Unexpected bind exception: "+e);
            e.printStackTrace(System.err);
        }

        System.out.println("Creating transaction.");

        try
        {
            topLevelControl = theOTS.create(0);
        }
        catch (Exception e)
        {
            fail("Create call failed: "+e);
            e.printStackTrace(System.err);
        }

        myOA.destroy();
        myORB.shutdown();
    }
}
