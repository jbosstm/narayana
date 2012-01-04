/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * $Id: demosync.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.orbspecific.resources;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTransactions.Synchronization;
import org.omg.CosTransactions.SynchronizationHelper;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.utils.Utility;

public class demosync extends org.omg.CosTransactions.SynchronizationPOA
{
    
    public demosync ()
    {
	this(true);
    }

    public demosync (boolean errors)
    {
	ORBManager.getPOA().objectIsReady(this);
	
	ref = SynchronizationHelper.narrow(ORBManager.getPOA().corbaReference(this));

	_errors = errors;
    }

    public Synchronization getReference ()
    {
	return ref;
    }
 
    public void before_completion () throws SystemException
    {
	if (_errors)
	{
	    System.out.println("DEMOSYNC : BEFORE_COMPLETION");
	    System.out.println("Synchronization throwing exception.");
	
	    throw new UNKNOWN();
	}
    }

    public void after_completion (org.omg.CosTransactions.Status status) throws SystemException
    {
	if (_errors)
	{
	    System.out.println("DEMOSYNC : AFTER_COMPLETION ( "+Utility.stringStatus(status)+" )");

	    System.out.println("Synchronization throwing exception.");
	
	    throw new UNKNOWN(); // should not cause any affect!
	}
    }

    private Synchronization ref;
    private boolean _errors;
    
}

