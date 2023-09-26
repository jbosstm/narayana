/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.orbportability.initialisation.preinit;

public class AllPreInitialisation
{
    public AllPreInitialisation()
    {
        System.out.println("AllPreInitialisation: called");
        _called = true;
    }

    public static boolean   _called = false;
}