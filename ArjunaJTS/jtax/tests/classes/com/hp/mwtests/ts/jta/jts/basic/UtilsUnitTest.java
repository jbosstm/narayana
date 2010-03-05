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

package com.hp.mwtests.ts.jta.jts.basic;

import javax.transaction.Status;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction;
import com.arjuna.ats.internal.jta.utils.jts.StatusConverter;
import com.arjuna.ats.internal.jta.utils.jts.XidUtils;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

import org.omg.CosTransactions.*;

import static org.junit.Assert.*;

public class UtilsUnitTest
{
    @Test
    public void testXidUtils () throws Exception
    {
        assertTrue(XidUtils.getXid(new Uid(), true) != null);
        
        OTSImpleManager.current().begin();
        
        AtomicTransaction tx = new AtomicTransaction(OTSImpleManager.current().getControlWrapper());
        
        assertTrue(XidUtils.getXid(tx.getControlWrapper().get_control(), false) != null);
        
        try
        {
            XidUtils.getXid((Control) null, true);
            
            fail();
        }
        catch (final IllegalStateException ex)
        {
        }
        
        assertTrue(XidUtils.getXid(OTSImpleManager.current().get_control(), true) != null);
        
        OTSImpleManager.current().commit(true);
    }
    
    @Test
    public void testStatusConverter () throws Exception
    {
        StatusConverter sc = new StatusConverter();
        
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusActive), Status.STATUS_ACTIVE);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusCommitted), Status.STATUS_COMMITTED);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusCommitting), Status.STATUS_COMMITTING);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusMarkedRollback), Status.STATUS_MARKED_ROLLBACK);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusNoTransaction), Status.STATUS_NO_TRANSACTION);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusPrepared), Status.STATUS_PREPARED);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusPreparing), Status.STATUS_PREPARING);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusRolledBack), Status.STATUS_ROLLEDBACK);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusRollingBack), Status.STATUS_ROLLING_BACK);
        assertEquals(StatusConverter.convert(org.omg.CosTransactions.Status.StatusUnknown), Status.STATUS_UNKNOWN);
    }
    
    @Before
    public void setUp () throws Exception
    {
        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);
    }
    
    @After
    public void tearDown () throws Exception
    {
        myOA.destroy();
        myORB.shutdown();
    }
    
    private ORB myORB = null;
    private RootOA myOA = null;
}
