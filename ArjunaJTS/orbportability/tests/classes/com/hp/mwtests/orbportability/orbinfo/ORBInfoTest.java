/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.orbportability.orbinfo;

import org.junit.Test;

import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.ORBInfo;

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