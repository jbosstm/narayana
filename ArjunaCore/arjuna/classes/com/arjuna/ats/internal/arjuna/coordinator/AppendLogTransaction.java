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
 * Copyright (C) 2005,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: AppendLogTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.ats.arjuna.TopLevelAction;

/**
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AppendLogTransaction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 4.1.
 */

/**
 * Needs further consideration and then completion.
 */

// may no longer be needed due to TxLog work.

public class AppendLogTransaction extends TopLevelAction
{

    public final boolean setLoggedTransaction (AppendLogTransaction previous)
    {
	if (_previous == null)
	{
	    _previous = previous;

	    super.add(new com.arjuna.ats.internal.arjuna.abstractrecords.DisposeRecord(previous.getStore(), previous));

	    return true;
	}
	else
	    return false;
    }

    public String type ()
    {
	return "/StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction/AppendLogTransaction";
    }

    protected void updateState ()
    {
	if (_previous == null)
	    super.savedIntentionList = false;
	
	super.updateState();
    }
    
    private AppendLogTransaction _previous;
    
}
