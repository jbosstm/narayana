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
package com.hp.mwtests.orbportability.orbinfo;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORBInfo;

import org.junit.Test;
import static org.junit.Assert.*;

public class ORBInfoTest
{
    @Test
    public void test() throws Exception
    {
        ORB orb = ORB.getInstance( "test.orb" );
        OA oa = OA.getRootOA( orb );

        orb.initORB(new String[] {}, null);
        oa.initOA(new String[] {});

        System.out.println("          ORBInfo.getOrbName: "+ORBInfo.getOrbName());
        System.out.println("  ORBInfo.getOrbMajorVersion: "+ORBInfo.getOrbMajorVersion());
        System.out.println("  ORBInfo.getOrbMinorVersion: "+ORBInfo.getOrbMinorVersion());
        System.out.println("ORBInfo.getCorbaMajorVersion: "+ORBInfo.getCorbaMajorVersion());
        System.out.println("ORBInfo.getCorbaMinorVersion: "+ORBInfo.getCorbaMinorVersion());
        System.out.println("     ORBInfo.getOrbEnumValue: "+ORBInfo.getOrbEnumValue());

        oa.destroy();
        orb.destroy();
    }
}
