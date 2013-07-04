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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.resources.ResourceRecord;

import com.hp.mwtests.ts.jts.orbspecific.resources.DemoResource;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ResourceRecordUnitTest extends TestBase
{
    @Test
    public void testDefault () throws Exception
    {
        ResourceRecord rec = new ResourceRecord();
        
        rec.setValue(null);
        
        assertEquals(rec.getRCUid(), Uid.nullUid());
        assertEquals(rec.value(), null);
        assertTrue(rec.type().length() > 0);
        assertEquals(rec.typeIs(), RecordType.OTS_RECORD);
        assertEquals(rec.resourceHandle(), null);
        
        rec.alter(null);
        rec.merge(null);
        
        ResourceRecord.remove(rec);
    }
    
    @Test
    public void test () throws Exception
    {
        DemoResource res = new DemoResource();
        ResourceRecord rec = new ResourceRecord(false, res.getResource(), new Uid());
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream());
        
        rec.print(writer);
        
        assertTrue(rec.resourceHandle() != null);
        
        OutputObjectState os = new OutputObjectState();
        
        assertTrue(rec.save_state(os, ObjectType.ANDPERSISTENT));
        
        InputObjectState is = new InputObjectState(os);
        
        assertTrue(rec.restore_state(is, ObjectType.ANDPERSISTENT));  
    }
}
