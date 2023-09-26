/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.recovery;



import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Test;

import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem;

public class TransactionStatusManagerItemTest
{
    @Test
    public void test() throws Exception
    {
        int _test_port = 4321;
        String _test_host = InetAddress.getLocalHost().getHostAddress();

        // Check that a transaction status manager item can be created correctly.

        assertTrue( TransactionStatusManagerItem.createAndSave(_test_port) );

        TransactionStatusManagerItem _tsmi = TransactionStatusManagerItem.get();

        assertEquals(_test_port, _tsmi.port());
        assertEquals(_test_host, _tsmi.host());

        // Check that a transaction status manager item can be re-created correctly.

        _tsmi = null;
        _tsmi = TransactionStatusManagerItem.recreate(Utility.getProcessUid());

        assertEquals(_test_port, _tsmi.port());
        assertEquals(_test_host, _tsmi.host());

    }
}