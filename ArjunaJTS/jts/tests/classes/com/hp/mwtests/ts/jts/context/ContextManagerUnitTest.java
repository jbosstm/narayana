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

package com.hp.mwtests.ts.jts.context;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ArjunaOTS.ActionControl;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.context.ContextManager;
import com.arjuna.ats.internal.jts.context.ContextPropagationManager;
import com.hp.mwtests.ts.jts.resources.TestBase;

import static org.junit.Assert.*;

public class ContextManagerUnitTest extends TestBase
{
    @Test
    public void testContextPropagationManager () throws Exception
    {
        ContextPropagationManager manager = new ContextPropagationManager();
    }
    
    @Test
    public void testContextManager () throws Exception
    {
        ContextManager manager = new ContextManager();
        
        assertEquals(manager.current(Thread.currentThread().getName()), null);
        assertEquals(manager.current(), null);
        
        OTSImpleManager.current().begin();
        
        manager.associate();
        
        OTSImpleManager.current().suspend();
        
        OTSImpleManager.current().begin();
        
        Control ct = OTSImpleManager.current().suspend();
        
        manager.addRemoteHierarchy(ct);

        manager.popAction();
        
        OTSImpleManager.current().suspend();
        
        OTSImpleManager.current().begin();
        
        ActionControl cont = (ActionControl) OTSImpleManager.current().getControlWrapper().getImple().getControl();
        
        manager.addActionControlHierarchy(cont);
        
        manager.purgeActions();
        
        OTSImpleManager.current().suspend();
        
        OTSImpleManager.current().begin();
        
        manager.addControlImpleHierarchy(OTSImpleManager.current().getControlWrapper().getImple());
        
        manager.purgeActions();
        
        OTSImpleManager.current().suspend();
    }
}
