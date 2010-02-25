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
 * $Id: TestException.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.txoj.basic;

import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.txoj.ConflictType;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;
import com.arjuna.ats.txoj.LockStatus;

import static org.junit.Assert.*;

public class TypesUnitTest
{
    @Test
    public void test()
    {
        assertEquals(ConflictType.stringForm(ConflictType.COMPATIBLE), "ConflictType.COMPATIBLE");
        assertEquals(ConflictType.stringForm(ConflictType.CONFLICT), "ConflictType.CONFLICT");
        assertEquals(ConflictType.stringForm(ConflictType.PRESENT), "ConflictType.PRESENT");
        assertEquals(ConflictType.stringForm(-1), "Unknown");
        
        ConflictType.print(new PrintWriter(System.err), ConflictType.COMPATIBLE);
        
        assertEquals(LockMode.stringForm(LockMode.INTENTION_READ), "LockMode.INTENTION_READ");
        assertEquals(LockMode.stringForm(LockMode.INTENTION_WRITE), "LockMode.INTENTION_WRITE");
        assertEquals(LockMode.stringForm(LockMode.READ), "LockMode.READ");
        assertEquals(LockMode.stringForm(LockMode.WRITE), "LockMode.WRITE");
        assertEquals(LockMode.stringForm(LockMode.UPGRADE), "LockMode.UPGRADE");
        assertEquals(LockMode.stringForm(-1), "Unknown");
        
        LockMode.print(new PrintWriter(System.err), LockMode.INTENTION_READ);
        
        assertEquals(LockResult.stringForm(LockResult.GRANTED), "LockResult.GRANTED");
        assertEquals(LockResult.stringForm(LockResult.REFUSED), "LockResult.REFUSED");
        assertEquals(LockResult.stringForm(LockResult.RELEASED), "LockResult.RELEASED");
        assertEquals(LockResult.stringForm(-1), "Unknown");
        
        LockResult.print(new PrintWriter(System.err), LockResult.GRANTED);
        
        assertEquals(LockStatus.printString(LockStatus.LOCKFREE), "LockStatus.LOCKFREE");
        assertEquals(LockStatus.printString(LockStatus.LOCKHELD), "LockStatus.LOCKHELD");
        assertEquals(LockStatus.printString(LockStatus.LOCKRETAINED), "LockStatus.LOCKRETAINED");
        assertEquals(LockStatus.printString(-1), "Unknown");
        
        LockStatus.print(new PrintWriter(System.err), LockStatus.LOCKFREE);
    }
}
