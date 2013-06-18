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
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.interposition;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerControlWrapperUnitTest extends TestBase
{
    @Test
    public void testEquality () throws Exception
    {
        TransactionFactoryImple imple = new TransactionFactoryImple("test");
        ControlImple tx = imple.createLocal(1000);
        
        ServerControlWrapper wrap1 = new ServerControlWrapper(tx);
        ServerControlWrapper wrap2 = new ServerControlWrapper(tx.getControl());
        
        assertTrue(wrap1.get_uid().equals(wrap2.get_uid()));
        
        wrap1 = new ServerControlWrapper(tx.getControl(), tx);
        wrap2 = new ServerControlWrapper(tx.getControl(), tx.get_uid());
        
        assertTrue(wrap1.get_uid().equals(wrap2.get_uid()));
    }
    
    @Test
    public void testNested () throws Exception
    {
        TransactionFactoryImple imple = new TransactionFactoryImple("test");
        ControlImple tx = imple.createLocal(1000);
        
        ServerControlWrapper wrap = new ServerControlWrapper(tx);
        ControlWrapper sub = wrap.create_subtransaction();
        
        assertTrue(sub != null);
        
        assertEquals(sub.cancel(), ActionStatus.ABORTED);
    }
}
