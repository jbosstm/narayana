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
 * $Id: CheckedAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;
import java.util.Hashtable;

import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * If an action attempts to terminate with threads still active we
 * call an instance of this class to determine what to do. The default
 * simply prints a warning and relies upon the outstanding threads to find
 * out the state of the action later. However, this can be overridden, e.g.,
 * the thread attempting to terminate the action may be made to block.
 *
 * WARNING: watch out for deadlock!
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: CheckedAction.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class CheckedAction
{

public CheckedAction ()
    {
    }

    /**
     * Called during transaction termination if more than one thread
     * is associated with the transaction. The supplied information
     * should be sufficient for application specific implementations to
     * do useful work (such as synchronizing on the threads).
     *
     * @message com.arjuna.ats.arjuna.coordinator.CheckedAction_1 [com.arjuna.ats.arjuna.coordinator.CheckedAction_1] - CheckedAction::check - atomic action {0} commiting with {1} threads active!
     * @message com.arjuna.ats.arjuna.coordinator.CheckedAction_2 [com.arjuna.ats.arjuna.coordinator.CheckedAction_2] - CheckedAction::check - atomic action {0} aborting with {1} threads active!
     */

public synchronized void check (boolean isCommit, Uid actUid, Hashtable list)
    {
	if (tsLogger.arjLoggerI18N.isWarnEnabled())
	{
	    if (isCommit)
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.CheckedAction_1", 
					    new Object[]{actUid,Integer.toString(list.size())});
	    else
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.coordinator.CheckedAction_2", 
					    new Object[]{actUid,Integer.toString(list.size())});
	}
    }    

}
