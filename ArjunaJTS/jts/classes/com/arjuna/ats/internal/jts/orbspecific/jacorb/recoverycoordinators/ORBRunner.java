/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ORBRunner.java 2342 2006-03-30 13:06:17Z  $
 *
 */

package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;

import com.arjuna.ats.arjuna.recovery.RecoveryActivator;
import com.arjuna.ats.jts.logging.*;
import com.arjuna.common.util.logging.*;
import com.arjuna.orbportability.common.Environment;


import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.SystemException;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.orbportability.debug.OAAttribute;

import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.orbportability.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

public class ORBRunner extends Thread 
{
    
    public ORBRunner ()
    {
	setDaemon(true);
	
	start();
    }

    public void run()
    {
	try
	{
	    JacOrbRCServiceInit._orb.orb().run();
	} 
	catch (Throwable e)
	{
	    e.printStackTrace();
	}
	
	try
	{
	    if (JacOrbRCServiceInit._oa != null)
		JacOrbRCServiceInit._oa.destroy();
	
	    if (JacOrbRCServiceInit._orb != null)
		JacOrbRCServiceInit._orb.shutdown();
	}
	catch (Exception ex)
	{
	}
    }
    
}

