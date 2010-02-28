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

package com.hp.mwtests.ts.jta.recovery;
import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.arjunacore.RecoveryXids;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.TestResource;

import static org.junit.Assert.*;


public class RecoveryXidsUnitTest
{
    @Test
    public void test()
    {
        TestResource tr = new TestResource();
        RecoveryXids rxids = new RecoveryXids(tr);
        Xid[] xids = new XidImple[2];
        
        xids[0] = new XidImple(new Uid());
        xids[1] = new XidImple(new Uid());
        
        RecoveryXids dup1 = new RecoveryXids(new DummyXA(false));
        RecoveryXids dup2 = new RecoveryXids(tr);
        
        assertFalse(rxids.equals(dup1));
        assertTrue(rxids.equals(dup2));
        
        rxids.nextScan(xids);
        rxids.nextScan(xids);
        
        xids[1] = new XidImple(new Uid());
        
        rxids.nextScan(xids);
        
        Object[] trans = rxids.toRecover();
        
        assertEquals(trans.length, 2);
        assertEquals(trans[0], xids[0]);
        
        assertTrue(rxids.contains(xids[0]));
        
        assertFalse(rxids.updateIfEquivalentRM(new TestResource(), null));
        assertTrue(rxids.updateIfEquivalentRM(new TestResource(), xids));
        
        assertFalse(rxids.isSameRM(new TestResource()));
    }
}
