/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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