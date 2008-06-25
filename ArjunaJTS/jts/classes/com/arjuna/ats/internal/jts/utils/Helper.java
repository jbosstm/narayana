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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Helper.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.utils;

import com.arjuna.orbportability.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.ActionManager;

import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

import org.omg.CosTransactions.*;
import com.arjuna.ArjunaOTS.*;

import java.io.PrintStream;
import org.omg.CORBA.CompletionStatus;

import org.omg.CosTransactions.Unavailable;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.NO_MEMORY;
import org.omg.CORBA.SystemException;

public class Helper
{

    /**
     * Given a Control_ptr determine if this is a reference to a local
     * action, and if so return the raw BasicAction pointer.
     */
    
    public static final BasicAction localAction (org.omg.CosTransactions.Control control)
    {
	if (control == null)
	    return null;

	if (control instanceof ControlImple)
	{
	    try
	    {
		ControlImple c = (ControlImple) control;
		
		return (BasicAction) c.getImplHandle();
	    }
	    catch (Exception e)
	    {
	    }
	}

	/*
	 * Can we not use is_local within visibroker?
	 */
	
	try
	{
	    UidCoordinator coord = Helper.getUidCoordinator(control);

	    if (coord != null)
	    {
		Uid u = Helper.getUid(coord);
		
		coord = null;

		return ActionManager.manager().get(u);
	    }
	    else
		throw new BAD_PARAM();
	}
	catch (Exception e)
	{
	    /*
	     * Can't be an Arjuna action, so ignore.
	     */
	}

	return null;
    }

    public static final ControlImple localControl (org.omg.CosTransactions.Control control)
    {
	if (control == null)
	    return null;

	if (control instanceof ControlImple)
	    return (ControlImple) control;

	try
	{
	    UidCoordinator uidCoord = Helper.getUidCoordinator(control);

	    if (uidCoord != null)
	    {
		Uid u = Helper.getUid(uidCoord);
		ControlImple toReturn = null;

		uidCoord = null;

		/*
 		 * allControls only contains local controls.
 		 */

		if (ControlImple.allControls != null)
		{
		    synchronized (ControlImple.allControls)
			{
			    toReturn = (ControlImple) ControlImple.allControls.get(u);
			}
		}
		
		if (toReturn == null)
		{
		    if (ServerControl.allServerControls != null)
		    {
			synchronized (ServerControl.allServerControls)
			    {
				toReturn = (ControlImple) ServerControl.allServerControls.get(u);				
			    }
		    }
		}

		u = null;

		return toReturn;
	    }
	    else
		throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
	}
	catch (Exception e)
	{
	    /*
	     * Can't be an Arjuna action, so ignore.
	     */
	}

	return null;
    }

    public static final UidCoordinator getUidCoordinator (org.omg.CosTransactions.Control control)
    {
	if (control == null)
	    return null;
    
	UidCoordinator toReturn = null;
    
	try
	{
	    Coordinator coord = control.get_coordinator();

	    if (coord != null)
	    {
		toReturn = getUidCoordinator(coord);

		coord = null;
	    }
	    else
		throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
	}
	catch (Exception e)
	{
	    /*
	     * Can't be an Arjuna action, so ignore.
	     */

	    toReturn = null;
	}
    
	return toReturn;
    }

    public static final UidCoordinator getUidCoordinator (Coordinator coord)
    {
	if (coord == null)
	    return null;
    
	UidCoordinator toReturn = null;

	try
	{
	    toReturn = com.arjuna.ArjunaOTS.UidCoordinatorHelper.narrow(coord);

	    if (toReturn == null)
		throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
	}
	catch (Exception e)
	{
	    /*
	     * Can't be an Arjuna action, so ignore.
	     */

	    toReturn = null;
	}

	return toReturn;
    }

    public static final boolean isUidCoordinator (Coordinator ref)
    {
	UidCoordinator ptr = getUidCoordinator(ref);

	return ((ptr == null) ? false : true);
    }
    
    public static final Uid getUid (UidCoordinator coord)
    {
	if (coord == null)
	    return Uid.nullUid();

	String theUid = null;
	Uid uid = null;

	/*
	 * Try twice, and if this still fails then
	 * throw NO_MEMORY.
	 */

	for (int i = 0; i < 1; i++)
	{
	    try
	    {
		theUid = coord.uid();
		uid = new Uid(theUid);

		theUid = null;

		return uid;
	    }
	    catch (OutOfMemoryError e)
	    {
		System.gc();
	    }
	}

	throw new NO_MEMORY(0, CompletionStatus.COMPLETED_NO);
    }

}
