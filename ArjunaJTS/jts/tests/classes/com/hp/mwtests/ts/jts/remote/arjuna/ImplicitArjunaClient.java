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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ImplicitArjunaClient.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.arjuna;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.resources.TestUtility;
import com.hp.mwtests.ts.jts.TestModule.stackHelper;
import com.hp.mwtests.ts.jts.TestModule.stack;

import org.omg.CORBA.IntHolder;

import org.junit.Test;
import static org.junit.Assert.*;

public class ImplicitArjunaClient
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

        String refFile = "/tmp/stack.ref";
        String serverName = "Stack";
        CurrentImple current = OTSImpleManager.current();

        if (System.getProperty("os.name").startsWith("Windows"))
        {
            refFile = "C:\\temp\\stack.ref";	}

        stack stackVar = null;   // pointer the grid object that will be used.

        try
        {
            current.begin();

            try
            {
                Services serv = new Services(myORB);

                stackVar = stackHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(refFile)));
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                fail();
            }

            System.out.println("pushing 1 onto stack");

            stackVar.push(1);

            System.out.println("pushing 2 onto stack");

            stackVar.push(2);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }

        try
        {
            current.commit(false);

            current.begin();

            IntHolder val = new IntHolder(-1);

            if (stackVar.pop(val) == 0)
            {
                System.out.println("popped top of stack "+val.value);

                current.begin();

                stackVar.push(3);

                System.out.println("pushed 3 onto stack. Aborting nested action.");

                current.rollback();

                stackVar.pop(val);

                System.out.println("popped top of stack is "+val.value);

                current.commit(false);

                assertEquals(1, val.value);

            }
            else
            {
                fail("Error getting stack value.");

                current.rollback();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}

