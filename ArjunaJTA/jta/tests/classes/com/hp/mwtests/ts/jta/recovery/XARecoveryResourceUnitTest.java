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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceImple;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceManagerImple;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.hp.mwtests.ts.jta.common.DummyXA;

class DummyImple extends XARecoveryResourceImple
{
    public DummyImple ()
    {
        super(new Uid());
    }
    
    public boolean notAProblem (XAException ex, boolean commit)
    {
        return super.notAProblem(ex, commit);
    }
}

public class XARecoveryResourceUnitTest
{
    @Test
    public void test()
    {
        XARecoveryResourceManagerImple xarr = new XARecoveryResourceManagerImple();
        
        assertTrue(xarr.getResource(new Uid()) != null);
        assertTrue(xarr.getResource(new Uid(), null) != null);
        assertTrue(xarr.type() != null);
    }
    
    @Test
    public void testRecoveryResource ()
    {
        XARecoveryResourceImple res = new XARecoveryResourceImple(new Uid());
        
        assertEquals(res.getXAResource(), null);
        assertEquals(res.recoverable(), XARecoveryResource.INCOMPLETE_STATE);
        
        res = new XARecoveryResourceImple(new Uid(), new DummyXA(false));
        
        assertEquals(res.recoverable(), XARecoveryResource.RECOVERY_REQUIRED);
        assertEquals(res.recover(), XARecoveryResource.WAITING_FOR_RECOVERY);
    }
    
    @Test
    public void testNotAProblem ()
    {
        DummyImple impl = new DummyImple();
        
        assertTrue(impl.notAProblem(new XAException(XAException.XAER_NOTA), true));
        assertFalse(impl.notAProblem(new XAException(XAException.XA_HEURHAZ), true));
    }
}
