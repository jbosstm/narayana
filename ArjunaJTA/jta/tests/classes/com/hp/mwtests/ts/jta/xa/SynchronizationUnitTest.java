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

import javax.transaction.Status;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple;
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.xa.XidImple;
import com.hp.mwtests.ts.jta.common.DummyXA;
import com.hp.mwtests.ts.jta.common.Synchronization;

import static org.junit.Assert.*;

public class SynchronizationUnitTest
{
    @Test
    public void testInvalid()
    {
        SynchronizationImple sync = new SynchronizationImple(null);
        
        assertTrue(sync.get_uid().notEquals(Uid.nullUid()));
        
        assertFalse(sync.beforeCompletion());
        assertFalse(sync.afterCompletion(Status.STATUS_COMMITTED));
    }
    
    @Test
    public void testValid()
    {
        SynchronizationImple sync = new SynchronizationImple(new Synchronization());
        
        assertTrue(sync.get_uid().notEquals(Uid.nullUid()));
        
        assertTrue(sync.beforeCompletion());
        assertTrue(sync.afterCompletion(Status.STATUS_COMMITTED));
        
        SynchronizationImple comp = new SynchronizationImple(new Synchronization());
        
        assertTrue(comp.compareTo(sync) != 0);
        assertTrue(sync.toString() != null);
    }
}
