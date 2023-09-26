/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.hp.mwtests.ts.arjuna.resources.BasicObject;

public class PersistenceUnitTest
{
    @Test
    public void testSaveRestore()
    {
        final BasicObject obj = new BasicObject();
        final Uid objUid = obj.get_uid();

        obj.set(1234);
        obj.deactivate();

        final BasicObject rec = new BasicObject(objUid);
        int res = rec.get();

        assertEquals(1234, res);
    }
}