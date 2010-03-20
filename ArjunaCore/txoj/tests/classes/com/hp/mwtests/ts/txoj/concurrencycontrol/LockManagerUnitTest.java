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
 * $Id: RecoveryTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.txoj.concurrencycontrol;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockResult;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

import static org.junit.Assert.*;

class DummyLockManager extends LockManager
{
    public DummyLockManager ()
    {
        super(new Uid(), ObjectType.ANDPERSISTENT);
    }
}


public class LockManagerUnitTest
{
    @Test
    public void test () throws Throwable
    {
        AtomicObject obj = new AtomicObject();
        
        obj.finalize();
        
        obj = new AtomicObject();
        
        assertTrue(obj.releaselock(new Uid()));
        assertEquals(obj.setlock(null), LockResult.REFUSED);
        
        obj.print(new PrintWriter(new ByteArrayOutputStream()));
        obj.printState(new PrintWriter(new ByteArrayOutputStream()));
        
        assertEquals(new DummyLockManager().type(), "StateManager/LockManager");
    }
}
