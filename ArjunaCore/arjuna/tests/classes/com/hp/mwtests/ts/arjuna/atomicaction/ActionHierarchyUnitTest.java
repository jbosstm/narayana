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

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.TopLevelAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionHierarchy;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.ActionType;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.OutputBuffer;

import org.junit.Test;
import static org.junit.Assert.*;

public class ActionHierarchyUnitTest
{
    @Test
    public void test() throws Exception
    {
        ActionHierarchy ah = new ActionHierarchy(5);
        Uid[] tx = new Uid[5];
        
        assertEquals(ah.getDeepestActionUid(), Uid.nullUid());
        
        for (int i = 0; i < tx.length; i++)
        {
            tx[i] = new Uid();
            
            ah.add(tx[i]);
        }
        
        assertEquals(ah.depth(), tx.length);
        
        assertEquals(ah.getActionUid(0), tx[0]);
        
        Uid deepest = new Uid();
        
        ah.add(deepest, ActionType.TOP_LEVEL);
        
        PrintWriter pw = new PrintWriter(System.err);
        
        ah.print(pw);
        
        assertEquals(ah.getDeepestActionUid(), deepest);
        
        ActionHierarchy cp = new ActionHierarchy(ah);
        
        assertTrue(cp.equals(ah));
        
        cp.copy(ah);
        
        ah.copy(ah);
        
        assertTrue(cp.equals(ah));
        
        OutputBuffer out = new OutputBuffer();
        
        cp.pack(out);
        
        InputBuffer in = new InputBuffer(out.buffer());
        
        ah.unpack(in);
        
        assertTrue(ah.equals(cp));
        
        assertTrue(ah.isAncestor(deepest));
        
        ah.forgetDeepest();
        
        assertTrue(ah.findCommonPrefix(cp) != 0);
    }
}
