/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.xa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

public class xidcheck
{
    @Test
    public void test()
    {
        Uid test = new Uid();
        XidImple xidImple = new XidImple(test, true, null);

        System.err.println("Uid is: "+test);
        System.err.println("Xid is: "+xidImple);

        Uid convertedUid = xidImple.getTransactionUid();

        assertEquals(test, convertedUid);
    }
    
    @Test
    public void testBasic ()
    {
        XidImple xid1 = new XidImple();
        AtomicAction A = new AtomicAction();
        
        assertEquals(xid1.getFormatId(), -1);
        
        xid1 = new XidImple(A);
        
        XidImple xid2 = new XidImple(new Uid());
        
        assertFalse(xid1.isSameTransaction(xid2));
        
        XidImple xid3 = new XidImple(xid1);
        
        assertTrue(xid3.isSameTransaction(xid1));
        
        assertTrue(xid1.getFormatId() != -1);
        
        assertTrue(xid1.getBranchQualifier().length > 0);
        assertTrue(xid1.getGlobalTransactionId().length > 0);
        
        assertEquals(xid1.getTransactionUid(), A.get_uid());
        
        assertTrue(XATxConverter.getNodeName(xid1.getXID()).equals(TxControl.getXANodeName()));
        
        assertTrue(xid1.getXID() != null);
        
        assertTrue(xid1.equals(xid3));
        
        XID x = new XID();
        
        assertFalse(xid1.equals(x));
        
        xid1 = new XidImple(x);
    }
    
    @Test
    public void testPackUnpack () throws Exception
    {
        XidImple xid1 = new XidImple(new Uid());
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(xid1.packInto(os));
        
        InputObjectState is = new InputObjectState(os);
        
        XidImple xid2 = new XidImple();
        
        assertTrue(xid2.unpackFrom(is));
        
        assertTrue(xid1.equals(xid2));
        
        os = new OutputObjectState();
        
        XidImple.pack(os, xid1);
        
        is = new InputObjectState(os);
        
        xid2 = (XidImple) XidImple.unpack(is);
        
        assertTrue(xid1.equals(xid2));
    }
}