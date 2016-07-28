/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
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
}
