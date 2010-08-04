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
 * $Id: ExplicitStackServer.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.remote.servers;

import com.hp.mwtests.ts.jts.orbspecific.resources.*;
import com.hp.mwtests.ts.jts.TestModule.ExplicitStackPOATie;
import com.hp.mwtests.ts.jts.resources.TestUtility;

import com.arjuna.orbportability.*;

import com.arjuna.ats.internal.jts.ORBManager;

public class ExplicitStackServer
{
    public static void main(String[] args) throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        String refFile = args[0];

        ExplicitStackPOATie theObject = new ExplicitStackPOATie (new ExplicitStackImple());

        myOA.objectIsReady(theObject);

        Services serv = new Services(myORB);

        try
        {
            TestUtility.registerService(refFile, myORB.orb().object_to_string(myOA.corbaReference(theObject)));

            System.out.println("Ready");

            myOA.run();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        myOA.shutdownObject(theObject);

        System.out.println("**ExplicitStackServer exiting**");
    }
}
