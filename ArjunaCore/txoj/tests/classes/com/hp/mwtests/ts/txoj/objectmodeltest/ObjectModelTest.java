/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.objectmodeltest;



import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.hp.mwtests.ts.txoj.common.exceptions.TestException;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class ObjectModelTest
{
    @Test
    public void testSINGLE() throws IOException, TestException
    {
        AtomicObject obj = new AtomicObject(ObjectModel.SINGLE);
        AtomicAction A = new AtomicAction();

        A.begin();

        obj.set(1234);

        A.commit();

        assertEquals(1234, obj.get());
    }
    
    @Test
    public void testMULTIPLE() throws IOException, TestException
    {
        AtomicObject obj1 = new AtomicObject(ObjectModel.MULTIPLE);
        AtomicObject obj2 = new AtomicObject(obj1.get_uid(), ObjectModel.MULTIPLE);
        AtomicAction A = new AtomicAction();

        A.begin();

        obj1.set(1234);

        A.commit();

        assertEquals(1234, obj2.get());
    }

}