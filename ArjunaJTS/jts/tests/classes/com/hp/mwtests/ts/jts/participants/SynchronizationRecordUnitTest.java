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
 * $Id$
 */

package com.hp.mwtests.ts.jts.participants;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.resources.SynchronizationRecord;
import com.hp.mwtests.ts.jts.orbspecific.resources.demosync;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class SynchronizationRecordUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        demosync theSync = new demosync();
        SynchronizationRecord sync = new SynchronizationRecord(theSync.getReference(), false);
        
        assertTrue(sync.contents() == theSync.getReference());
        assertTrue(sync.get_uid() != Uid.nullUid());
    }
    
    @Test
    public void testCompare () throws Exception
    {
        demosync theSync = new demosync();
        SynchronizationRecord sync1 = new SynchronizationRecord(theSync.getReference(), false);
        SynchronizationRecord sync2 = new SynchronizationRecord(theSync.getReference(), true);
        
        assertEquals(sync1.compareTo(sync2), -1);    
        assertEquals(sync2.compareTo(sync1), 1);
        assertEquals(sync1.compareTo(sync1), 0);
        
        sync2 = new SynchronizationRecord(theSync.getReference(), false);
        
        assertEquals(sync1.compareTo(sync2), -1);
    }
}
