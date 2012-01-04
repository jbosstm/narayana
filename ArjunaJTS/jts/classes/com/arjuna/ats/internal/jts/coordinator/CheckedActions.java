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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: CheckedActions.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.coordinator;

import java.util.Hashtable;

import org.omg.CORBA.SystemException;

import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.utils.ThreadUtil;

public class CheckedActions
{

    public static final synchronized void remove () throws SystemException
    {
        otsCheckedAction.remove(ThreadUtil.getThreadId());
    }
    
    public static final synchronized CheckedAction get () throws SystemException
    {
    	if (otsCheckedAction != null)
        {
    	    return (CheckedAction) otsCheckedAction.get(ThreadUtil.getThreadId());
        }
    	else
    	    return null;
    }

    public static final synchronized void set (CheckedAction ca) throws SystemException
    {
    	if (otsCheckedAction == null)
    	    otsCheckedAction = new Hashtable<String, CheckedAction>();
    
    	otsCheckedAction.put(ThreadUtil.getThreadId(), ca);
    }

    private static Hashtable<String, CheckedAction> otsCheckedAction = null;

}
