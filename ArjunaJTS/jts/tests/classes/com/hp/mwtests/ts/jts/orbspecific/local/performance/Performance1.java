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
 * $Id: Performance1.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.local.performance;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;

import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class Performance1
{
    @Test
    public void test()
    {
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

            double iters = 1000.0;
            boolean doCommit = true;

            // Run ten interations first.

            CurrentImple current = OTSImpleManager.current();

            for (int i = 0; i < 10; i++)
            {
                current.begin();

                if (doCommit)
                    current.commit(true);
                else
                    current.rollback();
            }

            // Record the start time.

            Date startTime = new Date();

            // Run 1000 interations.

            for (int i = 0; i < iters; i++)
            {
                current.begin();

                if (doCommit)
                    current.commit(true);
                else
                    current.rollback();
            }

            // Record the end time.

            Date endTime = new Date();
            double txnTime = (float)((endTime.getTime()-startTime.getTime())/iters);
            double txnPSec = 1000.0/txnTime;

            System.out.println("Average time for empty transaction = "+txnTime);
            System.out.println("Transactions per second = "+txnPSec);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail();
        }

        myOA.destroy();
        myORB.shutdown();
    }
}

