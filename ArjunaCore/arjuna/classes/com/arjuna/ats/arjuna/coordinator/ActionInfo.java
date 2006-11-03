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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ActionInfo.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

/*
 * Default visibility.
 */

class ActionInfo
{

    public ActionInfo ()
    {
	actionUid = new Uid(Uid.nullUid());
	actionType = ActionType.NESTED;
    }
    
    public ActionInfo (ActionInfo other)
    {
	actionUid = new Uid(other.actionUid);
	actionType = other.actionType;
    }

    public ActionInfo (Uid actionId, int at)
    {
	actionUid = new Uid(actionId);
	actionType = at;
    }    

    public synchronized void copy (ActionInfo other)
    {
	if (this != other)
	{
	    actionUid = new Uid(other.actionUid);
	    actionType = other.actionType;
	}
    }
    
    public final boolean equals (ActionInfo other)
    {
	if ((actionUid.equals(other.actionUid)) &&
	    (actionType == other.actionType))
	{
	    return true;
	}
	else
	    return false;
    }

    public final boolean notEquals (ActionInfo other)
    {
	if ((actionUid.notEquals(other.actionUid)) ||
	    (actionType != other.actionType))
	{
	    return true;
	}
	else
	    return false;
    }

    public Uid actionUid;
    public int actionType;
    
}
