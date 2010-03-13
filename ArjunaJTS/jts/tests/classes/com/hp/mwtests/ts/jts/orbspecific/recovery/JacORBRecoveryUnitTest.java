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

package com.hp.mwtests.ts.jts.orbspecific.recovery;

import org.junit.Test;
import org.omg.CosTransactions.RecoveryCoordinator;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCManager;
import com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCServiceInit;
import com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRCShutdown;
import com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.JacOrbRecoveryInit;
import com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.RecoverIOR;
import com.hp.mwtests.ts.jts.resources.TestBase;

import static org.junit.Assert.*;

public class JacORBRecoveryUnitTest extends TestBase
{
    @Test
    public void testORBRCManager () throws Exception
    {
        JacOrbRCManager manager = new JacOrbRCManager();
        RecoveryCoordinator rc = manager.makeRC(new Uid(), new Uid(), new Uid(), false);
        
        assertTrue(rc == null);
        
        manager.destroy(rc);
        manager.destroyAll(null);
    }
    
    @Test
    public void testORBRCShutdown () throws Exception
    {
        JacOrbRCShutdown shutdown = new JacOrbRCShutdown();
        
        shutdown.work();
    }
    
    @Test
    public void testInit () throws Exception
    {
        JacOrbRCServiceInit init = new JacOrbRCServiceInit();
        
        assertFalse(init.startRCservice());
        
        JacOrbRCServiceInit.shutdownRCService();
        
        assertTrue(JacOrbRCServiceInit.type() != null);
    }
    
    @Test
    public void testRecoveryInit () throws Exception
    {
        JacOrbRCServiceInit init = new JacOrbRCServiceInit();
        
        assertFalse(init.startRCservice());
        
        JacOrbRecoveryInit rinit = new JacOrbRecoveryInit();
        
        JacOrbRCServiceInit.shutdownRCService();
    }
    
    @Test
    public void testRecoverIOR () throws Exception
    {
        RecoverIOR ior = new RecoverIOR();
        
        try
        {
            String iorString = RecoverIOR.newObjectKey("foo", "bar");
            
            assertTrue(iorString != null);
            
            RecoverIOR.printIORinfo(iorString);
        }
        catch (final Exception ex)
        {
        }
    }
}
