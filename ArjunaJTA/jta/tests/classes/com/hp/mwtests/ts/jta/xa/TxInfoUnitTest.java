/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.xa.TxInfo;
import com.arjuna.ats.jta.xa.XidImple;

public class TxInfoUnitTest
{
    @Test
    public void test()
    {
        TxInfo tx = new TxInfo(new XidImple(new Uid()));
        
        tx.setState(-1);
        
        assertEquals(tx.getState(), TxInfo.UNKNOWN);
    }
}