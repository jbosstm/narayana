/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2008,
 * @author JBoss, a division of Red Hat.
 */
package com.hp.mwtests.ts.jts.resources;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Useful util functions for JTS tests.
 */
public class TestUtility
{
    public static String getService(String name) throws IOException
    {
        String returnValue = null;

        BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
        returnValue = fin.readLine();
        fin.close();

        return returnValue;
    }

    public static void registerService(String name, String ior) throws IOException
    {
        FileOutputStream fout = new FileOutputStream(name);
        fout.write(ior.getBytes());
        fout.close();
    }

    public static void assertTrue(boolean value) {
        if(!value) {
            fail("assertionFailed");
        }
    }

    public static void assertEquals(int a, int b) {
        if(a != b) {
            fail("assertionFailed");
        }
    }

    public static void fail(String message) {
        System.out.println("failing: "+message);
        System.out.println("Failed");
    }

}
