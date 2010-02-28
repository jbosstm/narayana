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
 * (C) 2005-2010,
 * @author JBoss Inc.
 */

package com.hp.mwtests.ts.jta.basic;

import javax.transaction.Status;
import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.jta.utils.arjunacore.StatusConverter;
import com.arjuna.ats.jta.utils.JTAHelper;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XidImple;

import static org.junit.Assert.*;

public class UtilsUnitTest
{
    @Test
    public void testJTAHelper () throws Exception
    {
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_ACTIVE), "javax.transaction.Status.STATUS_ACTIVE");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_COMMITTED), "javax.transaction.Status.STATUS_COMMITTED");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_MARKED_ROLLBACK), "javax.transaction.Status.STATUS_MARKED_ROLLBACK");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_NO_TRANSACTION), "javax.transaction.Status.STATUS_NO_TRANSACTION");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_PREPARED), "javax.transaction.Status.STATUS_PREPARED");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_PREPARING), "javax.transaction.Status.STATUS_PREPARING");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_ROLLEDBACK), "javax.transaction.Status.STATUS_ROLLEDBACK");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_ROLLING_BACK), "javax.transaction.Status.STATUS_ROLLING_BACK");
        assertEquals(JTAHelper.stringForm(javax.transaction.Status.STATUS_UNKNOWN), "javax.transaction.Status.STATUS_UNKNOWN");
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
        
        XidImple xid1 = new XidImple(new Uid());
        XidImple xid2 = new XidImple(new Uid());
        XidImple xid3 = new XidImple(xid1);
        
        assertFalse(XAHelper.sameXID(xid1, xid2));
        assertTrue(XAHelper.sameXID(xid1, xid3));
        
        assertTrue(XAHelper.sameTransaction(xid1, xid1));
        assertTrue(XAHelper.sameTransaction(xid1, xid3));
        
        assertTrue(XAHelper.xidToString(xid1) != null);
    }
}
