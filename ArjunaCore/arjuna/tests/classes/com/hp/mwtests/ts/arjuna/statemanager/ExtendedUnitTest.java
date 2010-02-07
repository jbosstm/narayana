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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: BasicTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.arjuna.statemanager;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectStatus;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;

import com.hp.mwtests.ts.arjuna.resources.*;

import org.junit.Test;
import static org.junit.Assert.*;

public class ExtendedUnitTest
{
    @Test
    public void test() throws Exception
    {
        AtomicAction A = new AtomicAction();
        ExtendedObject bo = new ExtendedObject();
        Uid u = bo.get_uid();
        
        A.begin();

        bo.set(2);
       
        bo.toggle();
        
        A.commit();
        
        bo.terminate();
        
        bo = new ExtendedObject(u);
        
        assertEquals(bo.status(), ObjectStatus.PASSIVE);
        assertTrue(bo.getStore() != null);
        assertTrue(bo.getStoreRoot() != null);
        
        assertEquals(bo.objectType(), ObjectType.ANDPERSISTENT);
        
        PrintWriter pw = new PrintWriter(new StringWriter());
        
        bo.print(pw);
    }

    @Test
    public void testCadaver () throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();
        
        A.begin();
        B.begin();
        
        ExtendedObject bo = new ExtendedObject();
        
        bo.set(2);
       
        bo.terminate();
        
        B.commit();
        A.commit();
    }
    
    @Test
    public void tryLock () throws Exception
    {
        ExtendedObject bo = new ExtendedObject();
        
        assertTrue(bo.lock());
        assertTrue(bo.unlock());
    }
}
