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

package com.hp.mwtests.ts.jta.xa;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.xa.XidImple;

import org.junit.Test;
import static org.junit.Assert.*;

public class xidcheck
{
    @Test
    public void test()
    {
        Uid test = new Uid();
        XidImple xidImple = new XidImple(test, true);

        System.err.println("Uid is: "+test);
        System.err.println("Xid is: "+xidImple);

        Uid convertedUid = xidImple.getTransactionUid();

        assertEquals(test, convertedUid);
    }
    
    @Test
    public void testBasic ()
    {
        XidImple xid1 = new XidImple();
        AtomicAction A = new AtomicAction();
        
        assertEquals(xid1.getFormatId(), -1);
        
        xid1 = new XidImple(A);
        
        XidImple xid2 = new XidImple(new Uid());
        
        assertFalse(xid1.isSameTransaction(xid2));
        
        XidImple xid3 = new XidImple(xid1);
        
        assertTrue(xid3.isSameTransaction(xid1));
        
        assertTrue(xid1.getFormatId() != -1);
        
        assertTrue(xid1.getBranchQualifier().length > 0);
        assertTrue(xid1.getGlobalTransactionId().length > 0);
        
        assertEquals(xid1.getTransactionUid(), A.get_uid());
        
        assertTrue(xid1.getNodeName() != null);
        
        assertTrue(xid1.getXID() != null);
        
        assertTrue(xid1.equals(xid3));
        
        XID x = new XID();
        
        assertFalse(xid1.equals(x));
        
        xid1 = new XidImple(x);
    }
    
    @Test
    public void testPackUnpack () throws Exception
    {
        XidImple xid1 = new XidImple(new Uid());
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(xid1.packInto(os));
        
        InputObjectState is = new InputObjectState(os);
        
        XidImple xid2 = new XidImple();
        
        assertTrue(xid2.unpackFrom(is));
        
        assertTrue(xid1.equals(xid2));
        
        os = new OutputObjectState();
        
        XidImple.pack(os, xid1);
        
        is = new InputObjectState(os);
        
        xid2 = (XidImple) XidImple.unpack(is);
        
        assertTrue(xid1.equals(xid2));
    }
}
