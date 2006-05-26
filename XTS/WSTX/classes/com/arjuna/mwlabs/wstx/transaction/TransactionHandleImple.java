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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionHandleImple.java,v 1.1 2002/11/25 11:00:54 nmcl Exp $
 */

package com.arjuna.mwlabs.wstx.transaction;

import com.arjuna.mw.wstx.transaction.TransactionHandle;

import com.arjuna.mw.wsas.activity.ActivityHandle;
import com.arjuna.mw.wsas.activity.ActivityHierarchy;

/**
 * TransactionHandle is used as a representation of a single transaction
 * when it is suspended from a running thread and may be later
 * resumed. The implementation of the token can be as lightweight
 * as required by the underlying implementation in order that it
 * can uniquely represent all activity instances.
 *
 * Since this is a client-facing class, it is unlikely that the
 * application user will typically want to see the entire activity
 * context in order to simply suspend it from the thread.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: TransactionHandleImple.java,v 1.1 2002/11/25 11:00:54 nmcl Exp $
 * @since 1.0.
 */

public class TransactionHandleImple implements TransactionHandle
{

    public TransactionHandleImple (ActivityHandle handle)
    {
	_handle = handle;
    }
    
    /**
     * Although users won't typically care what the underlying implementation
     * of a context is, they will need to do comparisons.
     * So, although this method is provided by Java.Object we have it here
     * to ensure that we don't forget to implement it!
     *
     * Two instances are equal if the refer to the same transaction.
     */

    public boolean equals (Object obj)
    {
	if (obj == null)
	    return false;
	else
	{
	    if (obj instanceof TransactionHandleImple)
		return _handle.equals(((TransactionHandleImple) obj)._handle);
	    else
		return false;
	}
    }
    
    /**
     * Although users won't typically care what the underlying implementation
     * of a context is, they will need to do comparisons.
     * So, although this method is provided by Java.Object we have it here
     * to ensure that we don't forget to implement it!
     */

    public int hashCode ()
    {
	return _handle.hashCode();
    }

    /**
     * @return whether or not this is a valid handle.
     */

    public boolean valid ()
    {
	return _handle.valid();
    }

    /**
     * @return the activity identifier.
     */

    public String tid ()
    {
	return _handle.tid();
    }

    private ActivityHandle _handle;
    
}
