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

package com.hp.mwtests.ts.jta.jts.jca;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.TransactionImporter;
import com.arjuna.ats.internal.jta.transaction.jts.jca.XATerminatorImple;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

import static org.junit.Assert.*;

public class XATerminatorImpleUnitTest extends TestBase
{
    @Test
    public void testPrepareCommit () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();
        
        assertTrue(xa.beforeCompletion(xid));
        
        assertEquals(xa.prepare(xid), XAResource.XA_RDONLY);
        
        try
        {
            xa.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        imp.importTransaction(xid);
        
        xa.commit(xid, false);
    }
    
    @Test
    public void testOnePhaseCommit () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.commit(xid, true);
    }
    
    @Test
    public void testPrepareAbort () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();
        
        assertEquals(xa.prepare(xid), XAResource.XA_RDONLY);
        
        try
        {
            xa.rollback(xid);
        }
        catch (final XAException ex)
        {
        }
    }
    
    @Test
    public void testAbort () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.rollback(xid);
    }
    
    @Test
    public void testForget () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.forget(xid);
    }
    
    @Test
    public void testRecover () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();
        
        imp.importTransaction(xid);
        
        XATerminatorImple xa = new XATerminatorImple();

        xa.recover(XAResource.TMSTARTRSCAN);
        
        try
        {
            xa.recover(XAResource.TMSTARTRSCAN);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        xa.recover(XAResource.TMENDRSCAN);
    }
    
    @Test
    public void testNull () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        TransactionImporter imp = SubordinationManager.getTransactionImporter();    
        XATerminatorImple xa = new XATerminatorImple();

        try
        {
            xa.prepare(xid);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            xa.commit(xid, false);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            xa.commit(xid, true);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
        
        try
        {
            xa.rollback(xid);
            
            fail();
        }
        catch (final Exception ex)
        {
        }
    }
}
