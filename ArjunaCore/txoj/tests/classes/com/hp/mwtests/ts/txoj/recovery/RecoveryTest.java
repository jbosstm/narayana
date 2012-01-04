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

package com.hp.mwtests.ts.txoj.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;

public class RecoveryTest
{
    @Test
    public void testCommit () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        AtomicObject obj = new AtomicObject();
        OutputObjectState os = new OutputObjectState();
        Uid u = new Uid();
        
        assertTrue(obj.save_state(os, ObjectType.ANDPERSISTENT));
        
        assertTrue(StoreManager.getParticipantStore().write_uncommitted(u, obj.type(), os));
        
        MyRecoveredTO rto = new MyRecoveredTO(u, obj.type(), StoreManager.getParticipantStore());
        
        rto.replay();
        
        A.abort();
    }
    
    @Test
    public void testAbort () throws Exception
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        AtomicObject obj = new AtomicObject();
        OutputObjectState os = new OutputObjectState();
        Uid u = new Uid();
        
        assertTrue(obj.save_state(os, ObjectType.ANDPERSISTENT));
        
        assertTrue(StoreManager.getParticipantStore().write_uncommitted(u, obj.type(), os));
        
        MyRecoveredTO rto = new MyRecoveredTO(u, obj.type(), StoreManager.getParticipantStore());
        
        A.abort();
        
        rto.replay();
    }
}
