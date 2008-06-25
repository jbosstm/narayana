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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ResourceCompletor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.recoverycoordinators;

import com.arjuna.ats.arjuna.common.*;
import org.omg.CosTransactions.*;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

/**
 * Threaded object used to terminate a prepared Resource after a
 * failure.  Normally failed resources are terminated by replaying the
 * transaction in which the Resource was participating. However, if
 * the transaction rolled back then we won't have any persistent
 * record of the transaction so it can't be reactivated. Resources (and
 * subordinate coordinators) may use the replay_completion response to
 * trigger their own rollback.
 * 
 * @message com.arjuna.ats.internal.jts.recovery.recoverycoordinators.ResourceCompletor_1 [com.arjuna.ats.internal.jts.recovery.recoverycoordinators.ResourceCompletor_1] - ResourceCompletor.rollback() - rollback failed: {0}
 */

public class ResourceCompletor extends Thread
{
    public static final int ROLLBACK = 0;
    public static final int COMMIT = 1;

    public ResourceCompletor( Resource res, int action )
    {
	_res = res;
	_action = action;
    }
    
    public void finalize () throws Throwable
    {
	super.finalize();
	_res = null;
    }

    public final void run ()
    {
	if (_action == ROLLBACK)
	    rollback();
    }

    private final void rollback ()
    {
	try
	{
	    if (jtsLogger.logger.isDebugEnabled())
		{
		    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, 
					   VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "ResourceCompletor.rollback()");
		}
	    
	    _res.rollback();
	}
	catch (Exception e)
	{
	    if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
					       VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.recoverycoordinators.ResourceCompletor_1", new Object[]{e});
		}
	}
    }

    private Resource _res = null;
    private int      _action = 0;
}

    
    
