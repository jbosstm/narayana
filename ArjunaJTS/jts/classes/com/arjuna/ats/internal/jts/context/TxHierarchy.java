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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TxHierarchy.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.context;

import com.arjuna.orbportability.ORB;

import org.omg.CosTransactions.*;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.internal.arjuna.template.*;

import java.io.PrintStream;

/*
 * Need methods for adding to and removing from the
 * hierarchy as it changes over time. We only want to
 * maintain a single instance of this class for each
 * hierarchy, no matter how it evolves. This means
 * covering branches etc.
 */

/**
 * Maintains the OTS transaction hierarchy.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TxHierarchy.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class TxHierarchy
{
    
    public TxHierarchy ()
    {
	_hier = new SimpleList();
    }
    
    public void finalize ()
    {
	if (_hier != null)
	{
	    ListElementWrapper ptr = (ListElementWrapper) _hier.orderedPop();

	    while (ptr != null)
	    {
		ptr = null;
		ptr = (ListElementWrapper) _hier.orderedPop();
	    }

	    _hier = null;
	}
    }

    public final synchronized boolean present (Control control)
    {
	SimpleListIterator iter = new SimpleListIterator(_hier);
	ListElementWrapper wrapper = (ListElementWrapper) iter.iterate();
	Control act = ((wrapper != null) ? (Control) wrapper.value() : null);
	boolean found = false;
	Coordinator theCoordinator = null;
    
	try
	{
	    theCoordinator = control.get_coordinator();
	}
	catch (Exception e)
	{
	    theCoordinator = null;
	}

	if (theCoordinator != null)
	{
	    Coordinator coord = null;

	    while ((act != null) && (!found))
	    {
		try
		{
		    coord = act.get_coordinator();
		    found = theCoordinator.is_same_transaction(coord);
		}
		catch (Exception e)
		{
		}

		coord = null;

		if (!found)
		{
		    wrapper = (ListElementWrapper) iter.iterate();		    
		
		    act = ((wrapper != null) ? (Control) wrapper.value() : null);
		}
	    }

	    coord = null;
	}

	theCoordinator = null;
    
	return found;
    }

    public final synchronized void push (Control control)
    {
	_hier.push(new ListElementWrapper(control));
    }
    
    public final synchronized Control pop ()
    {
	ListElementWrapper wrapper = (ListElementWrapper) _hier.orderedPop();
	Control cont = ((wrapper != null) ? (Control) wrapper.value() : null);
	Control toReturn = null;
    
	if (wrapper != null)
	{
	    wrapper = null;

	    return cont;
	}
	else
	    return null;
    }

    SimpleList _hier;
 
}
