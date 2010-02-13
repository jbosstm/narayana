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
package com.hp.mwtests.ts.arjuna.atomicaction;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.RecordList;
import com.arjuna.ats.internal.arjuna.abstractrecords.ActivationRecord;
import com.arjuna.ats.internal.arjuna.abstractrecords.DisposeRecord;

import static org.junit.Assert.*;

public class RecordListUnitTest
{
    @Test
    public void test () throws Exception
    {
        RecordList rl = new RecordList();
        DisposeRecord dr = new DisposeRecord();
        
        rl.insert(dr);
        
        assertEquals(rl.getFront(), dr);
        
        rl.insert(dr);
        
        assertEquals(rl.getRear(), dr);
        
        RecordList copy = new RecordList(rl);
        ActivationRecord ar = new ActivationRecord();
        
        rl.insert(ar);
        
        rl.print(new PrintWriter(new ByteArrayOutputStream()));
        
        assertTrue(rl.toString() != null);
        
        assertEquals(rl.getNext(dr), null);
        
        assertTrue(rl.peekFront() != null);
        assertTrue(rl.peekRear() != null);
        assertEquals(rl.peekNext(dr), null);
        
        assertTrue(rl.remove(dr));
    }
}
