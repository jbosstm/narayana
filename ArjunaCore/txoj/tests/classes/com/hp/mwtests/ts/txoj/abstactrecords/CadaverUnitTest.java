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
package com.hp.mwtests.ts.txoj.abstactrecords;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.internal.txoj.abstractrecords.CadaverLockRecord;
import com.arjuna.ats.internal.txoj.abstractrecords.LockRecord;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

import static org.junit.Assert.*;

public class CadaverUnitTest
{
    @Test
    public void testCommit () throws Exception
    {
	AtomicAction A = new AtomicAction();
	AtomicObject B = new AtomicObject(ObjectModel.MULTIPLE);
	Uid u = B.get_uid();
	
	A.begin();

	B.set(1234);
	 
	A.commit();
	
	A = new AtomicAction();	
	B = new AtomicObject(u, ObjectModel.MULTIPLE);
	
	A.begin();
	
	AtomicAction C = new AtomicAction();
	
	C.begin();
	
	assertEquals(B.get(), 1234);
	
	B.set(5678);
	
	B.terminate();

	C.commit();
	
	assertEquals(A.commit(), ActionStatus.COMMITTED);
    }
    
    @Test
    public void testAbort () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicObject B = new AtomicObject(ObjectModel.MULTIPLE);
        Uid u = B.get_uid();
        
        A.begin();

        B.set(1234);
         
        A.commit();
        
        A = new AtomicAction();
        
        B = new AtomicObject(u, ObjectModel.MULTIPLE);
        
        A.begin();
        
        AtomicAction C = new AtomicAction();
        
        C.begin();
        
        assertEquals(B.get(), 1234);
        
        B.set(5678);
        
        B.terminate();

        C.commit();
        
        assertEquals(A.abort(), ActionStatus.ABORTED);
    }
 
    @Test
    public void testMultipleNestedCommit () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicObject B = new AtomicObject(ObjectModel.MULTIPLE);
        Uid u = B.get_uid();
        
        A.begin();
        
        B.set(1234);
         
        A.commit();
        
        A = new AtomicAction();
        B = new AtomicObject(u, ObjectModel.MULTIPLE);
        
        A.begin();
        
        AtomicAction C = new AtomicAction();
        
        C.begin();
        
        assertEquals(B.get(), 1234);

        B.set(5678);

        B.terminate();

        C.commit();
        
        assertEquals(A.commit(), ActionStatus.COMMITTED);
    }
    
    @Test
    public void testMultipleNestedAbort () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicObject B = new AtomicObject(ObjectModel.MULTIPLE);
        Uid u = B.get_uid();
        
        A.begin();

        B.set(1234);
         
        A.commit();
        
        A = new AtomicAction();
        B = new AtomicObject(u, ObjectModel.MULTIPLE);
        
        A.begin();

        AtomicAction C = new AtomicAction();
        
        C.begin();

        assertEquals(B.get(), 1234);
        
        B.set(5678);

        B.terminate();

        C.abort();
        
        assertEquals(A.commit(), ActionStatus.COMMITTED);
    }
    
    @Test
    public void testBasic () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicObject B = new AtomicObject();
        
        A.begin();
        
        CadaverLockRecord clr = new CadaverLockRecord(null, B, A);
        LockRecord lr = new LockRecord(B, A);
        
        assertTrue(clr.type() != null);
        
        clr.print(new PrintWriter(new ByteArrayOutputStream()));
        
        clr.replace(lr);
        
        A.abort();
    }
}
