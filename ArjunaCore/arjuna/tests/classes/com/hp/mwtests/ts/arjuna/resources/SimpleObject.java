/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.resources;

public class SimpleObject
{

    public SimpleObject()
    {
        state = 0;

        System.out.println("Created simple object.");
    }

    public void incr(int value)
    {
        state += value;
    }

    public void set(int value)
    {
        state = value;
    }

    public int get()
    {
        return state;
    }

    private int state;

};