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

package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CORBA.SystemException;

import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.jts.extensions.AtomicTransaction;
import com.arjuna.ats.jts.extensions.ThreadAssociations;
import com.arjuna.ats.jts.extensions.TxAssociation;
import com.hp.mwtests.ts.jts.resources.TestBase;

class SampleAssociation implements TxAssociation
{
    public void begin (ControlWrapper tx) throws SystemException
    {
        beginCalled = true;
    }

    public void commit (ControlWrapper tx) throws SystemException
    {
        commitCalled = true;
    }

    public void rollback (ControlWrapper tx) throws SystemException
    {
        rollbackCalled = true;
    }

    public void suspend (ControlWrapper tx) throws SystemException
    {
        suspendCalled = true;
    }

    public void resume (ControlWrapper tx) throws SystemException
    {
        resumeCalled = true;
    }

    public String name ()
    {
        return "SampleAssociation";
    }
    
    public boolean beginCalled = false;
    public boolean commitCalled = false;
    public boolean rollbackCalled = false;
    public boolean suspendCalled = false;
    public boolean resumeCalled = false;
}


public class ThreadAssociationsUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        AtomicTransaction tx = new AtomicTransaction();
        SampleAssociation assoc = new SampleAssociation();
        
        ThreadAssociations.addGlobal(assoc);
        
        tx.begin();
        
        assertTrue(assoc.beginCalled);
        
        tx.commit(false);
        
        assertTrue(assoc.commitCalled);
        
        tx = new AtomicTransaction();
        
        tx.begin();
        
        tx.suspend();
        
        assertTrue(assoc.suspendCalled);
        
        tx.resume();
        
        assertTrue(assoc.resumeCalled);
        
        tx.rollback();
        
        assertTrue(assoc.rollbackCalled);
    }
}
