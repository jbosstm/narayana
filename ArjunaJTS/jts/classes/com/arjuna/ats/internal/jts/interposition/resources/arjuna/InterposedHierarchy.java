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
 * $Id: InterposedHierarchy.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition.resources.arjuna;

import org.omg.CosTransactions.*;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.internal.arjuna.template.*;

import com.arjuna.ats.jts.*;

import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.*;

import java.io.PrintWriter;

public class InterposedHierarchy implements ListElement
{
    
    public InterposedHierarchy (ServerTopLevelAction action)
    {
	_action = action;
    }

    public final ServerTopLevelAction action ()
    {
	return _action;
    }

    public Uid get_uid ()
    {
	return ((_action != null) ? _action.get_uid() : Uid.nullUid());
    }
    
    public final String hierarchy ()
    {
	String hier = "InterposedHierarchy:";

	if (_action != null)
	{
	    hier += _action.get_uid();

	    HashList children = _action.getChildren();

	    if (children != null)
	    {
		HashListIterator iter = new HashListIterator(children);
		ServerNestedAction childPtr = (ServerNestedAction) iter.iterate();

		while (childPtr != null)
		{
		    hier += "\n"+childPtr.get_uid();

		    hier += childPtr.getChildren(2);
		    childPtr = (ServerNestedAction) iter.iterate();
		}

		iter = null;
	    }
	}
	else
	    hier += "EMPTY";

	return hier;
    }

    private ServerTopLevelAction _action;

}
