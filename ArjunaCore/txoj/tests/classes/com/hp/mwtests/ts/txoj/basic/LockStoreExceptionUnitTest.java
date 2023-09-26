/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.basic;

import org.junit.Test;

import com.arjuna.ats.txoj.exceptions.LockStoreException;

public class LockStoreExceptionUnitTest
{
    @Test
    public void test()
    {
        LockStoreException ex = new LockStoreException();
        
        ex = new LockStoreException("Oops foobar!");
        
        ex = new LockStoreException("Another issue!", new NullPointerException());
        
        ex = new LockStoreException(new IllegalArgumentException());
    }
}