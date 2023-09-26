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

	class Lifetime
	{
		public Lifetime (BasicAction act)
		{
			theAction = act;
			timeAdded = System.currentTimeMillis();
		}
		
		public BasicAction getAction ()
		{
			return theAction;
		}
		
		public long getTimeAdded ()
		{
			return timeAdded;
		}
		
		private BasicAction theAction;
		private long timeAdded;
	}
	
	public static final ActionManager manager()
	{
		return _theManager;
	}

	public void put(BasicAction act)
	{
		_allActions.put(act.get_uid(), new Lifetime(act));
	}

	public BasicAction get(Uid id)
	{
		Lifetime lt = _allActions.get(id);
		
		if (lt != null)
			return lt.getAction();
		else
			return null;
	}

    /**
     * @deprecated this method no longer serves any useful purpose and will be removed in a future release
     * @param id the uid of the action for which the time when it was added is required
     * @return the time that the action was begun
     */
    @Deprecated
	public long getTimeAdded (Uid id)
	{
		Lifetime lt = _allActions.get(id);
		
		if (lt != null)
			return lt.getTimeAdded();
		else
			return 0;
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

	private static final Map<Uid, Lifetime> _allActions = new ConcurrentHashMap<Uid, Lifetime>();
}