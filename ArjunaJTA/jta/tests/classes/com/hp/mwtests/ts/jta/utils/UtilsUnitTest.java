/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.hp.mwtests.ts.jta.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.jta.utils.arjunacore.StatusConverter;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.utils.JTAHelper;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XidImple;

class DummyXid implements Xid
{
    public byte[] getBranchQualifier ()
    {
        return _branch;
    }

    public int getFormatId ()
    {
        return 0;
    }

    public byte[] getGlobalTransactionId ()
    {
        return _gtid;
    }
    
    private byte[] _branch = {1, 2};
    private byte[] _gtid = {3, 4};
}


public class UtilsUnitTest
{
    @Test
    public void testJTAHelper () throws Exception
    {
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_ACTIVE), "jakarta.transaction.Status.STATUS_ACTIVE");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_COMMITTED), "jakarta.transaction.Status.STATUS_COMMITTED");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_MARKED_ROLLBACK), "jakarta.transaction.Status.STATUS_MARKED_ROLLBACK");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_NO_TRANSACTION), "jakarta.transaction.Status.STATUS_NO_TRANSACTION");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_PREPARED), "jakarta.transaction.Status.STATUS_PREPARED");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_PREPARING), "jakarta.transaction.Status.STATUS_PREPARING");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_ROLLEDBACK), "jakarta.transaction.Status.STATUS_ROLLEDBACK");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_ROLLING_BACK), "jakarta.transaction.Status.STATUS_ROLLING_BACK");
        assertEquals(JTAHelper.stringForm(jakarta.transaction.Status.STATUS_UNKNOWN), "jakarta.transaction.Status.STATUS_UNKNOWN");
    }
    
    @Test
    public void testStatusConverter () throws Exception
    {
        StatusConverter sc = new StatusConverter();
        
        assertEquals(StatusConverter.convert(ActionStatus.ABORT_ONLY), Status.STATUS_MARKED_ROLLBACK);
        assertEquals(StatusConverter.convert(ActionStatus.ABORTED), Status.STATUS_ROLLEDBACK);
        assertEquals(StatusConverter.convert(ActionStatus.ABORTING), Status.STATUS_ROLLING_BACK);
        assertEquals(StatusConverter.convert(ActionStatus.CLEANUP), Status.STATUS_UNKNOWN);
        assertEquals(StatusConverter.convert(ActionStatus.COMMITTED), Status.STATUS_COMMITTED);
        assertEquals(StatusConverter.convert(ActionStatus.COMMITTING), Status.STATUS_COMMITTING);
        assertEquals(StatusConverter.convert(ActionStatus.CREATED), Status.STATUS_UNKNOWN);
        assertEquals(StatusConverter.convert(ActionStatus.DISABLED), Status.STATUS_UNKNOWN);
        assertEquals(StatusConverter.convert(ActionStatus.H_COMMIT), Status.STATUS_COMMITTED);
        assertEquals(StatusConverter.convert(ActionStatus.H_HAZARD), Status.STATUS_COMMITTED);
        assertEquals(StatusConverter.convert(ActionStatus.H_MIXED), Status.STATUS_COMMITTED);
        assertEquals(StatusConverter.convert(ActionStatus.H_ROLLBACK), Status.STATUS_ROLLEDBACK);
        assertEquals(StatusConverter.convert(ActionStatus.INVALID), Status.STATUS_UNKNOWN);
        assertEquals(StatusConverter.convert(ActionStatus.NO_ACTION), Status.STATUS_NO_TRANSACTION);
        assertEquals(StatusConverter.convert(ActionStatus.PREPARED), Status.STATUS_PREPARED);
        assertEquals(StatusConverter.convert(ActionStatus.PREPARING), Status.STATUS_PREPARING);
        assertEquals(StatusConverter.convert(ActionStatus.RUNNING), Status.STATUS_ACTIVE);
    }
    
    @Test
    public void testXAHelper () throws Exception
    {   
        assertTrue(XAHelper.printXAErrorCode(null) != null);
        
        XAException ex = new XAException(XAException.XA_HEURCOM);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_HEURCOM");
        
        ex = new XAException(XAException.XA_HEURHAZ);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_HEURHAZ");
        
        ex = new XAException(XAException.XA_HEURMIX);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_HEURMIX");
        
        ex = new XAException(XAException.XA_HEURRB);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_HEURRB");
        
        ex = new XAException(XAException.XA_NOMIGRATE);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_NOMIGRATE");
        
        ex = new XAException(XAException.XA_RBCOMMFAIL);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBCOMMFAIL");
        
        ex = new XAException(XAException.XA_RBDEADLOCK);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBDEADLOCK");
        
        ex = new XAException(XAException.XA_RBINTEGRITY);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBINTEGRITY");
        
        ex = new XAException(XAException.XA_RBOTHER);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBOTHER");
        
        ex = new XAException(XAException.XA_RBPROTO);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBPROTO");
        
        ex = new XAException(XAException.XA_RBROLLBACK);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBROLLBACK");
        
        ex = new XAException(XAException.XA_RBTIMEOUT);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBTIMEOUT");
        
        ex = new XAException(XAException.XA_RBTRANSIENT);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RBTRANSIENT");
        
        ex = new XAException(XAException.XA_RDONLY);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RDONLY");
        
        ex = new XAException(XAException.XA_RETRY);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XA_RETRY");
        
        ex = new XAException(XAException.XAER_RMERR);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_RMERR");
        
        ex = new XAException(XAException.XAER_ASYNC);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_ASYNC");
        
        ex = new XAException(XAException.XAER_DUPID);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_DUPID");       
        
        ex = new XAException(XAException.XAER_INVAL);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_INVAL");
        
        ex = new XAException(XAException.XAER_NOTA);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_NOTA");
        
        ex = new XAException(XAException.XAER_OUTSIDE);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_OUTSIDE");
        
        ex = new XAException(XAException.XAER_PROTO);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_PROTO");
        
        ex = new XAException(XAException.XAER_RMFAIL);
        assertEquals(XAHelper.printXAErrorCode(ex), "XAException.XAER_RMFAIL");
        
        assertTrue(XAHelper.printXAErrorCode(new XAException(-1)) != null);
        
        XidImple xid1 = new XidImple(new Uid());
        XidImple xid2 = new XidImple(new Uid());
        XidImple xid3 = new XidImple(xid1);
        
        assertFalse(XAHelper.sameXID(xid1, xid2));
        assertTrue(XAHelper.sameXID(xid1, xid3));
        
        assertTrue(XAHelper.sameTransaction(xid1, xid1));
        assertTrue(XAHelper.sameTransaction(xid1, xid3));
        
        assertTrue(XAHelper.xidToString(xid1) != null);
        
        XID x = new XID();
        x.bqual_length = 1;
        x.gtrid_length = 1;
        x.data = new byte[] { '1', '2' };
        
        assertTrue(x.toString() != null);
        
        XAHelper.xidToString(new DummyXid());
    }
}