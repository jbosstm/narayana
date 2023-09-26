/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.recovery;

import static org.junit.Assert.assertFalse;

import java.util.Vector;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.xa.XidImple;

public class RecoveryTest
{
    @Test
    public void test()
    {
        Vector xaRecoveryNodes = new Vector();
        xaRecoveryNodes.add("2");

        System.err.println("Bogus XA node name: "+"2");

        XidImple xid = new XidImple(new Uid());
        String nodeName = XAUtils.getXANodeName(xid);

        // should fail.

        System.err.println("XA node name: "+nodeName);
        System.err.println("Xid to recover: "+xid);

        assertFalse( xaRecoveryNodes.contains(nodeName) );
    }

}