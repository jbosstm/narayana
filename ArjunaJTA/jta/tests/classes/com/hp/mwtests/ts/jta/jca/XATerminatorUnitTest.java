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
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinationManager;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.XATerminatorImple;
import com.arjuna.ats.jta.exceptions.UnexpectedConditionException;
import com.arjuna.ats.jta.xa.XidImple;

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
        term.recover(XAResource.TMENDRSCAN);
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
}
