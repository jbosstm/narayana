/*
 * SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation.postinit;

public class AllPostInitialisation
{
    public AllPostInitialisation()
    {
        System.out.println("AllPostInitialisation: called");
        _called = true;
    }

    public static boolean   _called = false;
}