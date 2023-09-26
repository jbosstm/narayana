/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.orbportability.initialisation.preinit;

public class PreInitialisation
{
    public PreInitialisation()
    {
        System.out.println("PreInitialisation: called");
        _called = true;
        _count++;
    }

    public static long      _count = 0;
    public static boolean   _called = false;
}