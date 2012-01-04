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

package com.hp.mwtests.ts.jta.jts.jca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CosTransactions.WrongTransaction;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.SubordinateAtomicTransaction;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.TransactionImple;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.jts.common.TestBase;

class DummySubordinateAtomicTransaction extends SubordinateAtomicTransaction
{
    public DummySubordinateAtomicTransaction ()
    {
        super(new Uid());
    }
    
    public boolean checkForCurrent ()
    {
        return super.checkForCurrent();
    }
}


public class SubordinateTxUnitTest extends TestBase
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
    public void testAtomicTransaction () throws Exception
    {
        XidImple xid = new XidImple(new Uid());
        SubordinateAtomicTransaction saa1 = new SubordinateAtomicTransaction(new Uid());
        SubordinateAtomicTransaction saa2 = new SubordinateAtomicTransaction(new Uid(), xid, 0);
        
        assertEquals(saa2.getXid(), xid);
        
        try
        {
            saa2.end(true);
            
            fail();
        }
        catch (final WrongTransaction ex)
        {
        }
        
        try
        {
            saa2.abort();
            
            fail();
        }
        catch (final WrongTransaction ex)
        {
        }
        
        DummySubordinateAtomicTransaction dsat = new DummySubordinateAtomicTransaction();
        
        assertFalse(dsat.checkForCurrent());
    }
}
