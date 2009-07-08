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
 * Copyright (C) 2000, 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TerminationTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.local.timeout;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import org.omg.CosTransactions.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;

import org.junit.Test;
import static org.junit.Assert.*;

public class TerminationTest
{
    @Test
    public void test()
    {
        boolean commit = true;

        Control myControl = null;
        ORB myORB = null;
        RootOA myOA = null;

        try
        {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[] {}, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            TransactionFactoryImple theOTS = new TransactionFactoryImple();

            System.out.println("Creating transaction with 2 second timeout.");

            myControl = theOTS.create(2);

            assertNotNull( myControl );

            Terminator handle = myControl.get_terminator();

            try
            {
                System.out.println("Sleeping for 5 seconds.");

                Thread.sleep(5000);
            }
            catch (Exception e)
            {
            }

            if (handle != null)
            {
                System.out.print("Attempting to ");

                if (commit)
                    System.out.println("commit transaction. Should fail!");
                else
                    System.out.println("rollback transaction. Should fail!.");

                if (commit)
                    handle.commit(true);
                else
                    handle.rollback();

                assertFalse(commit);

                System.out.println("\nNow attempting to destroy transaction. Should fail!");

                OTSManager.destroyControl(myControl);
            }
            else
                System.err.println("No transaction terminator!");
        }
        catch (UserException e)
        {
            System.err.println("Caught UserException: "+e);
        }
        catch (SystemException e)
        {
            System.err.println("Caught SystemException: "+e);

            try
            {
                Coordinator coord = myControl.get_coordinator();
                Status s = coord.get_status();

                System.err.println("Transaction status: "+com.arjuna.ats.jts.utils.Utility.stringStatus(s));

                coord = null;
            }
            catch (Exception ex)
            {
            }
        }

        try
        {
            CurrentImple current = OTSImpleManager.current();

            current.set_timeout(2);

            System.out.println("\nNow creating current transaction with 2 second timeout.");

            current.begin();

            myControl = current.get_control();

            try
            {
                System.out.println("Sleeping for 5 seconds.");

                Thread.sleep(5000);
            }
            catch (Exception e)
            {
            }

            System.out.print("Attempting to ");

            if (commit)
                System.out.println("commit transaction. Should fail!");
            else
                System.out.println("rollback transaction. Should fail!.");

            if (commit)
                current.commit(true);
            else
                current.rollback();

            assertFalse(commit);
        }
        catch (UserException e)
        {
            System.err.println("Caught UserException: "+e);
            System.out.println("Test did not completed successfully.");
        }
        catch (SystemException e)
        {
            System.err.println("Caught SystemException: "+e);

            try
            {
                Coordinator coord = myControl.get_coordinator();
                Status s = coord.get_status();

                System.err.println("Transaction status: "+com.arjuna.ats.jts.utils.Utility.stringStatus(s));

                myControl = null;
                coord = null;
            }
            catch (Exception ex)
            {
            }

            System.out.println("Test completed successfully.");
        }

        myOA.destroy();
        myORB.shutdown();
    }
}
