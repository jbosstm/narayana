/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DemoSynchronization.java,v 1.1 2002/11/25 11:00:54 nmcl Exp $
 */

package com.arjuna.mwtests.wstx.common;

import com.arjuna.mw.wstx.resource.Synchronization;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wstx.common.TxId;

import com.arjuna.mw.wsas.completionstatus.CompletionStatus;

import com.arjuna.mw.wsas.status.Status;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DemoSynchronization.java,v 1.1 2002/11/25 11:00:54 nmcl Exp $
 * @since 1.0.
 */

public class DemoSynchronization implements Synchronization
{

    public DemoSynchronization (TxId id)
    {
	_tid = id;
    }
    
    public void beforeCompletion () throws SystemException
    {
	System.out.println("DemoSynchronization.beforeCompletion ( "+_tid+" )");
    }
    
    public void afterCompletion (CompletionStatus cs) throws SystemException
    {
	System.out.println("DemoSynchronization.afterCompletion ( "+_tid+", "+cs+" )");
    }

    /**
     * @return the name of this inferior.
     */

    public String name ()
    {
	return "DemoSynchronization";
    }
    
    private TxId _tid;
    
}

