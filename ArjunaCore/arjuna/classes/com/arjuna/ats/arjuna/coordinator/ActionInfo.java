/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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