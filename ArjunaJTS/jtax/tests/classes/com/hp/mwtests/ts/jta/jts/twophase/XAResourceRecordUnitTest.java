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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SimpleTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.twophase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.junit.Test;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyRecoverableXAConnection;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.TestResource;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

class DummyXAResourceRecord extends XAResourceRecord
{
    public DummyXAResourceRecord (TransactionImple tx, XAResource res, Xid xid, Object[] params)
    {
        super(tx, res, xid, params);
    }
    
    public void setXAResource (XAResource res)
    {
        super.setXAResource(res);
    }
    
    public int recover ()
    {
        return super.recover();
    }
}


public class XAResourceRecordUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        XAResourceRecord xares = new XAResourceRecord();
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];
        
        params[XAResourceRecord.XACONNECTION] = rc;
        
        xares = new XAResourceRecord(new TransactionImple(), new DummyXA(false), new XidImple(new Uid()), params);
        
        xares.merge(null);
        xares.alter(null);
        
        assertTrue(xares.type() != null);
        
        assertTrue(xares.toString() != null);
        
        assertTrue(xares.get_uid().notEquals(Uid.nullUid()));
    }
    
    @Test
    public void testRecovery () throws Exception
    {
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];
        
        params[XAResourceRecord.XACONNECTION] = rc;
        
        DummyXAResourceRecord xares = new DummyXAResourceRecord(new TransactionImple(), new DummyXA(false), new XidImple(new Uid()), params);
        
        assertEquals(xares.getRecoveryCoordinator(), null);
        
        assertEquals(xares.recover(), XARecoveryResource.FAILED_TO_RECOVER);
        
        xares.setXAResource(null);
    }
    
    @Test
    public void testPackUnpack () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        XAResourceRecord xares;      
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];
        
        params[XAResourceRecord.XACONNECTION] = rc;
        
        xares = new XAResourceRecord(new TransactionImple(), new DummyXA(false), new XidImple(new Uid()), params);
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(xares.saveState(os));
        
        xares = new XAResourceRecord();
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(xares.restoreState(is));
    }
    
    @Test
    public void testReadonly () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        XAResourceRecord xares;
        
        DummyRecoverableXAConnection rc = new DummyRecoverableXAConnection();
        Object[] params = new Object[1];
        
        params[XAResourceRecord.XACONNECTION] = rc;
        
        xares = new XAResourceRecord(new TransactionImple(), new TestResource(true), new XidImple(new Uid()), params);
        
        try
        {
            xares.commit();
            
            fail();
        }
        catch (final NotPrepared ex)
        {
        }
        
        assertEquals(xares.prepare(), Vote.VoteReadOnly);
    }
    
    @Test
    public void testCommitFailure () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        FailureXAResource fxa = new FailureXAResource(FailureXAResource.FailLocation.commit);
        TransactionImple tx = new TransactionImple();
        XAResourceRecord xares = new XAResourceRecord(tx, fxa, tx.getTxId(), null);
        
        assertEquals(xares.prepare(), Vote.VoteCommit);
        
        try
        {
            xares.commit();
            
            fail();
        }
        catch (final HeuristicMixed ex)
        {      
        }
        
        xares.forget();
    }
    
    @Test
    public void testRollbackFailure () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        FailureXAResource fxa = new FailureXAResource(FailureXAResource.FailLocation.rollback);
        TransactionImple tx = new TransactionImple();
        XAResourceRecord xares = new XAResourceRecord(tx, fxa, tx.getTxId(), null);
        
        assertEquals(xares.prepare(), Vote.VoteCommit);
        
        try
        {
            xares.rollback();
            
            fail();
        }
        catch (final HeuristicMixed ex)
        {      
        }
        
        xares.forget();
    }
    
    @Test
    public void testValid2PC () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        TransactionImple tx = new TransactionImple();
        DummyXA res = new DummyXA(false);
        XAResourceRecord xares = new XAResourceRecord(tx, res, tx.getTxId(), null);
        
        assertEquals(xares.prepare(), Vote.VoteCommit);
        
        xares.commit();
    }
    
    @Test
    public void testValid1PC () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        TransactionImple tx = new TransactionImple();
        DummyXA res = new DummyXA(false);
        XAResourceRecord xares = new XAResourceRecord(tx, res, tx.getTxId(), null);
        
        xares.commit_one_phase();
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        XAResourceRecord xares = new XAResourceRecord();
        
        assertEquals(xares.getXid(), null);
        assertTrue(xares.uid() != null);
        
        try
        {
            xares.commit_one_phase();
            
            fail();
        }
        catch (final TRANSACTION_ROLLEDBACK ex)
        {
        }
        
        assertEquals(xares.prepare(), Vote.VoteRollback);
        
        xares.rollback();
        xares.commit();
    }
    
    @Test
    public void testNested () throws Exception
    {
        ThreadActionData.purgeActions();        
        OTSImpleManager.current().contextManager().purgeActions();

        XAResourceRecord xares = new XAResourceRecord();
        
        assertEquals(xares.prepare_subtransaction(), Vote.VoteRollback);
        
        try
        {
            xares.commit_subtransaction(null);
            
            fail();
        }
        catch (final UNKNOWN ex)
        {       
        }
        
        try
        {
            xares.rollback_subtransaction();
            
            fail();
        }
        catch (final UNKNOWN ex)
        {       
        }
        
        assertFalse(xares.propagateOnAbort());
        assertFalse(xares.propagateOnCommit());
    }
}
