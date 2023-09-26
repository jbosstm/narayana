/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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