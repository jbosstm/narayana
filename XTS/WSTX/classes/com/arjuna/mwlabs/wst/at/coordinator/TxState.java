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
 * $Id: TxState.java,v 1.1 2003/04/04 15:00:56 nmcl Exp $
 */

package com.arjuna.mwlabs.wst.at.coordinator;

import com.arjuna.mw.wsas.activity.*;

import java.util.Hashtable;

public class TxState
{

    public static final int COMMITTED = 0;
    public static final int ABORTED = 1;
    public static final int UNKNOWN = 2;
    public static final int HEURISTIC = 3;
    
    public static final void setState (ActivityHierarchy hier, int state)
    {
	_states.put(hier, new Integer(state));
    }
    
    public static final int getState (ActivityHierarchy hier)
    {
	Integer i = (Integer) _states.get(hier);
	
	if (i != null)
	    return i.intValue();
	else
	    return TxState.UNKNOWN;
    }
    
    public static final void removeState (ActivityHierarchy hier)
    {
	_states.remove(hier);
    }

    private static Hashtable _states = new Hashtable();
    
}
