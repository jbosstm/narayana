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

package com.hp.mwtests.ts.jts.orbspecific.interposition;

import org.junit.Test;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.ServerSynchronization;
import com.hp.mwtests.ts.jts.resources.TestBase;

import static org.junit.Assert.*;

public class ServerSynchronizationUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);
        ServerSynchronization sync = new ServerSynchronization(sc);
        
        sync.before_completion();
        sync.after_completion(Status.StatusCommitted);
        
        assertTrue(sync.getSynchronization() != null);
        
        sync.destroy();
    }
    
    @Test
    public void testNull () throws Exception
    {
        ServerSynchronization sync = new ServerSynchronization(null);
        
        try
        {
            sync.before_completion();
            
            fail();
        }
        catch (final BAD_OPERATION ex)
        {
        }
        
        try
        {
            sync.after_completion(Status.StatusCommitted);
            
            fail();
        }
        catch (final BAD_OPERATION ex)
        {
        }
    }
}
