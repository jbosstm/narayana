/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation.postinit;

public class PostInitialisation2
{
    public PostInitialisation2()
    {
        System.out.println("PostInitialisation2: called");
        _called = true;
        _count++;
    }

    public static long      _count = 0;
    public static boolean   _called = false;
}