/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.orbportability.initialisation.preinit;

public class PreInitialisation2
{
    public PreInitialisation2()
    {
        System.out.println("PreInitialisation2: called");
        _called = true;
        _count++;
    }

    public static long      _count = 0;
    public static boolean   _called = false;
}