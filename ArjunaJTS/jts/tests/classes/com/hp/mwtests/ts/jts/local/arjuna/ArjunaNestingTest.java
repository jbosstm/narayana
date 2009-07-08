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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ArjunaNestingTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.arjuna;

import com.hp.mwtests.ts.jts.orbspecific.resources.*;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArjunaNestingTest
{
    @Test
    public void run() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        boolean doAbort = false;
        boolean registerSubtran = false;
        org.omg.CosTransactions.Current current = OTSManager.get_current();
        DemoArjunaResource sr = new DemoArjunaResource();

        try
        {
            current.begin();
            current.begin();
            current.begin();
        }
        catch (SystemException sysEx)
        {
            fail("Unexpected system exception:" +sysEx);
            sysEx.printStackTrace(System.err);
        }
        catch (UserException se)
        {
            fail("Unexpected user exception:" +se);
            se.printStackTrace(System.err);
        }

        try
        {
            sr.registerResource(registerSubtran);
        }
        catch (SystemException ex1)
        {
            fail("Unexpected system exception: "+ex1);
            ex1.printStackTrace(System.err);
        }
        catch (Exception e)
        {
            fail("call to registerSubtran failed: "+e);
            e.printStackTrace(System.err);
        }

        try
        {
            System.out.println("committing first nested transaction");
            current.commit(true);

            System.out.println("committing second nested transaction");
            current.commit(true);

            if (!doAbort)
            {
                System.out.println("committing top-level transaction");
                current.commit(true);
            }
            else
            {
                System.out.println("aborting top-level transaction");
                current.rollback();
            }
        }
        catch (Exception ex)
        {
            fail("Caught unexpected exception: "+ex);
            ex.printStackTrace(System.err);
        }

        myOA.shutdownObject(sr);

        myOA.destroy();
        myORB.shutdown();
    }

}

