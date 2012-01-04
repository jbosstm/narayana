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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.TopLevelAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

public class TopLevelActionUnitTest
{
    @Test
    public void test() throws Exception
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();
        TopLevelAction tl = new TopLevelAction();
        
        A.begin();  // top level
        B.begin();  // nested
        
        tl.begin(); // nested top level
        
        A.abort();  // not recommended in practice!
        
        assertEquals(A.status(), ActionStatus.ABORTED);
        assertEquals(B.status(), ActionStatus.ABORTED);
        
        assertEquals(tl.status(), ActionStatus.RUNNING);
        
        tl.abort();
    }
}
