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

package com.hp.mwtests.ts.jta.jts.recovery;

import java.util.ArrayList;

import javax.transaction.xa.XAException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceImple;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.hp.mwtests.ts.jta.jts.common.DummyXA;

import static org.junit.Assert.*;

class DummyXARecoveryResource extends XARecoveryResourceImple
{
    public DummyXARecoveryResource ()
    {
        super(new Uid());
    }
    
    public boolean notAProblem (XAException ex, boolean commit)
    {
        return super.notAProblem(ex, commit);
    }
}


public class XARecoveryResourceImpleUnitTest
{
    @Test
    public void test ()
    {
        XARecoveryResourceImple xares = new XARecoveryResourceImple(new Uid());
        
        assertEquals(xares.getXAResource(), null);
        assertEquals(xares.recoverable(), XARecoveryResource.INFLIGHT_TRANSACTION);
        
        xares = new XARecoveryResourceImple(new Uid(), new DummyXA(false));
        
        assertEquals(xares.recover(), XARecoveryResource.FAILED_TO_RECOVER);
        
        DummyXARecoveryResource dummy = new DummyXARecoveryResource();
        
        assertTrue(dummy.notAProblem(new XAException(XAException.XAER_NOTA), true));
        assertFalse(dummy.notAProblem(new XAException(XAException.XAER_DUPID), false));
    }
}
