/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jca;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;
import org.junit.Test;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubordinateTxUnitTest
{
    @Test
    public void testTransactionImple () throws Exception
    {
        TransactionImple tx = new TransactionImple(new Uid());
        TransactionImple dummy = new TransactionImple(new Uid());
        
        tx.recordTransaction();
        
        assertFalse(tx.equals(dummy));
        
        assertTrue(tx.toString() != null);
        
        tx.recover();
    }
    
    @Test
    public void testAtomicAction () throws Exception
    {
        SubordinateAtomicAction saa1 = new SubordinateAtomicAction();
        SubordinateAtomicAction saa2 = new SubordinateAtomicAction(new Uid());
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(saa1.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(saa2.restore_state(is, ObjectType.ANDPERSISTENT));
    }

    @Test
    public void testSAA() throws Exception {
        SubordinateAtomicAction saa = new SubordinateAtomicAction();

        saa.add(new XAResourceRecord(null, new XAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {

            }

            @Override
            public void end(Xid xid, int i) throws XAException {

            }

            @Override
            public void forget(Xid xid) throws XAException {

            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return false;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                return 0;
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[0];
            }

            @Override
            public void rollback(Xid xid) throws XAException {

            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {

            }
        }, new XidImple(new Xid() {

            @Override
            public int getFormatId() {
                return 0;
            }

            @Override
            public byte[] getGlobalTransactionId() {
                return new byte[0];
            }

            @Override
            public byte[] getBranchQualifier() {
                return new byte[0];
            }
        }), null));

        saa.doPrepare();
        SubordinateAtomicAction saa2 = new SubordinateAtomicAction(saa.get_uid(), true);
        assertTrue(saa2.getXid() != null);

        saa.doCommit();
        SubordinateAtomicAction saa3 = new SubordinateAtomicAction(saa.get_uid(), true);
        assertTrue(saa3.getXid() == null); // Since the SAA was committed the transaction log record will have been removed so the xid returned from getXid() should no longer be available and the intention is the SAA creator would disregard this instance
    }


    @Test
    public void testSAADeferred() throws Exception {
        SubordinateAtomicAction saa = new SubordinateAtomicAction();

        saa.add(new XAResourceRecord(null, new XAResource() {
            @Override
            public void commit(Xid xid, boolean b) throws XAException {

            }

            @Override
            public void end(Xid xid, int i) throws XAException {

            }

            @Override
            public void forget(Xid xid) throws XAException {

            }

            @Override
            public int getTransactionTimeout() throws XAException {
                return 0;
            }

            @Override
            public boolean isSameRM(XAResource xaResource) throws XAException {
                return false;
            }

            @Override
            public int prepare(Xid xid) throws XAException {
                XAException xae = new XAException (XAException.XAER_INVAL);
                xae.initCause(new Throwable("test message"));
                throw xae;
            }

            @Override
            public Xid[] recover(int i) throws XAException {
                return new Xid[0];
            }

            @Override
            public void rollback(Xid xid) throws XAException {

            }

            @Override
            public boolean setTransactionTimeout(int i) throws XAException {
                return false;
            }

            @Override
            public void start(Xid xid, int i) throws XAException {

            }
        }, new XidImple(new Xid() {

            @Override
            public int getFormatId() {
                return 0;
            }

            @Override
            public byte[] getGlobalTransactionId() {
                return new byte[0];
            }

            @Override
            public byte[] getBranchQualifier() {
                return new byte[0];
            }
        }), null));

        saa.doPrepare();
        List<Throwable> deferredThrowables = saa.getDeferredThrowables();
        assertEquals("test message", deferredThrowables.get(0).getCause().getMessage());
    }
}