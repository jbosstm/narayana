/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.arjuna.ats.arjuna.common.Uid;

/*
 * @author Mark Little (mark_little@hp.com)
 * 
 * @version $Id: ActionManager.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 3.0
 */

public class ActionManager
{
	
	public static ActionManager manager()
	{
		return _theManager;
	}

	public void put(BasicAction act)
	{
		_allActions.put(act.get_uid(), act);
	}

	public BasicAction get(Uid id)
	{
		return _allActions.get(id);
	}
	
	public void remove(Uid id)
	{
		_allActions.remove(id);
	}

    public int getNumberOfInflightTransactions()
    {
        return _allActions.size();
    }
	
	private ActionManager()
	{
	}

	private static final ActionManager _theManager = new ActionManager();

	private final Map<Uid, BasicAction> _allActions = new ConcurrentHashMap<>();
}