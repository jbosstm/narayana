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
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ActionManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
