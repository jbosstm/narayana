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

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.thread.ThreadActionData;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.FailureXAResource;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailLocation;
import com.hp.mwtests.ts.jta.common.FailureXAResource.FailType;

import static org.junit.Assert.*;

public class XATerminatorUnitTest
{
    @Test
    public void test () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        assertTrue(term.beforeCompletion(xid));
        assertEquals(term.prepare(xid), XAResource.XA_RDONLY);
        
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        term.commit(xid, true);
        
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        term.rollback(xid);
        
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        term.recover(XAResource.TMSTARTRSCAN);
        
        try
        {
            term.recover(XAResource.TMSTARTRSCAN);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        term.recover(XAResource.TMENDRSCAN);
        
        term.forget(xid);
    }
    
    @Test
    public void testFail () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        
        SubordinateTransaction tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.rollback));
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.heurcom));
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.heurcom));
        
        term.prepare(xid);
        
        try
        {
            term.commit(xid, false);
        }
        catch (final XAException ex)
        {
            fail();
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.commit, FailType.normal));
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.rollback, FailType.rollback));
        
        try
        {
            term.rollback(xid);
        }
        catch (final XAException ex)
        {
            fail();
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.rollback, FailType.heurcom));
        
        term.prepare(xid);
        
        try
        {
            term.rollback(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.rollback, FailType.normal));
        
        term.prepare(xid);
        
        try
        {
            term.rollback(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.prepare_and_rollback, FailType.normal));

        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.prepare_and_rollback, FailType.heurcom));

        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        xid = new XidImple(new Uid());
        SubordinationManager.getTransactionImporter().importTransaction(xid);
        tx = SubordinationManager.getTransactionImporter().getImportedTransaction(xid);
        
        tx.enlistResource(new FailureXAResource(FailLocation.prepare_and_rollback, FailType.rollback));

        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
    }
    
    @Test
    public void testUnknownTransaction () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        
        try
        {
            term.beforeCompletion(xid);
            
            fail();
        }
        catch (final UnexpectedConditionException ex)
        {
        }
        
        try
        {
            term.prepare(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        try
        {
            term.commit(xid, false);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        try
        {
            term.rollback(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
        
        try
        {
            term.forget(xid);
            
            fail();
        }
        catch (final XAException ex)
        {
        }
    }
    
    @Test
    public void testInvalid () throws Exception
    {
        XATerminatorImple term = new XATerminatorImple();
        XidImple xid = new XidImple(new Uid());
        
        try
        {
            SubordinationManager.getTransactionImporter().importTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            SubordinationManager.getTransactionImporter().recoverTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            SubordinationManager.getTransactionImporter().getImportedTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        try
        {
            SubordinationManager.getTransactionImporter().removeImportedTransaction(null);
            
            fail();
        }
        catch (final IllegalArgumentException ex)
        {
        }
        
        Uid uid = new Uid();
        
        try
        {
            Object obj = SubordinationManager.getTransactionImporter().recoverTransaction(uid);
        
            fail();
        }
        catch (IllegalArgumentException ex)
        {
        }
    }
}
