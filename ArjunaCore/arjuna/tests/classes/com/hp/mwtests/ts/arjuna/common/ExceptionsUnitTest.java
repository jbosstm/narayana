/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.common;

import org.junit.Test;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreError;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

public class ExceptionsUnitTest
{
    @Test
    public void test ()
    {
        FatalError fe = new FatalError();
        
        fe = new FatalError("problem");
        fe = new FatalError("problem", new NullPointerException());
        fe = new FatalError(new NullPointerException());
        
        ObjectStoreError os = new ObjectStoreError();
        
        os = new ObjectStoreError("problem");
        os = new ObjectStoreError("problem", new NullPointerException());
        os = new ObjectStoreError(new NullPointerException());
        
        ObjectStoreException ox = new ObjectStoreException();
        
        ox = new ObjectStoreException("problem");
        ox = new ObjectStoreException("problem", new NullPointerException());
        ox = new ObjectStoreException(new NullPointerException());
    }
}