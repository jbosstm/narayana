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
package com.hp.mwtests.orbportability.orbspecific.orbinstance;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleServer
{
    @Test
    public void test() throws Exception
    {
        ORB orb = ORB.getInstance("main_orb");
        ORB orb2 = ORB.getInstance("main_orb_2");
        RootOA oa = RootOA.getRootOA(orb);
        RootOA oa2 = RootOA.getRootOA(orb2);

        orb.initORB(new String[] {},null);
        oa.initOA(new String[] {});

        orb2.initORB(new String[] {},null);
        oa2.initOA(new String[] {});

        SimpleObjectImpl obj = new SimpleObjectImpl();

        assertTrue( oa.objectIsReady(obj) );

        assertTrue( oa2.objectIsReady(obj) );

        oa.destroy();
        orb.shutdown();
        oa2.destroy();
        orb2.shutdown();
    }
}
