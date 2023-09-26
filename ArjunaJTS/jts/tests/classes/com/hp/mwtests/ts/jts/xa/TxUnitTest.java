/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.xa;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.jts.tx.tx;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class TxUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        tx a = new tx();
        
        assertEquals(tx.tx_open(), tx.TX_OK);
        assertEquals(tx.tx_close(), tx.TX_OK);
        assertEquals(tx.tx_allow_nesting(), tx.TX_OK);
        assertEquals(tx.tx_disable_nesting(), tx.TX_OK);
        
        assertEquals(tx.tx_set_transaction_timeout(10), tx.TX_OK);
        assertEquals(tx.tx_begin(), tx.TX_OK);
        assertEquals(tx.tx_set_commit_return(1), tx.TX_OK);
        assertEquals(tx.tx_commit(), tx.TX_OK);
        
        assertEquals(tx.tx_begin(), tx.TX_OK);
        assertEquals(tx.tx_rollback(), tx.TX_OK);
        
        assertEquals(tx.tx_set_transaction_control(1), tx.TX_FAIL);
    }
    
    @Test
    public void testNestingSuccess () throws Exception
    {
        assertEquals(tx.tx_allow_nesting(), tx.TX_OK);
        assertEquals(tx.tx_open(), tx.TX_OK);
        assertEquals(tx.tx_begin(), tx.TX_OK);
        assertEquals(tx.tx_begin(), tx.TX_OK);
        assertEquals(tx.tx_commit(), tx.TX_OK);
        assertEquals(tx.tx_rollback(), tx.TX_OK);
        assertEquals(tx.tx_close(), tx.TX_OK);
    }
    
    @Test
    public void testNestingFail () throws Exception
    {
        assertEquals(tx.tx_disable_nesting(), tx.TX_OK);
        assertEquals(tx.tx_open(), tx.TX_OK);
        assertEquals(tx.tx_begin(), tx.TX_OK);
        assertEquals(tx.tx_begin(), tx.TX_PROTOCOL_ERROR);
        assertEquals(tx.tx_commit(), tx.TX_OK);
        assertEquals(tx.tx_close(), tx.TX_OK);
    }
}