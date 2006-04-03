/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * $Id: SynchronizationImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.resources.arjunacore;

import com.arjuna.ats.internal.jta.utils.arjunacore.StatusConverter;

import com.arjuna.ats.jta.logging.*;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseCoordinator;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.SynchronizationRecord;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.common.util.logging.*;

import java.util.Hashtable;

import javax.transaction.*;

import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.SystemException;
import java.lang.SecurityException;
import java.lang.IllegalStateException;
import java.lang.NullPointerException;

/**
 * Whenever a synchronization is registered, an instance of this class
 * is used to wrap it.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: SynchronizationImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public class SynchronizationImple implements SynchronizationRecord
{
    
    public SynchronizationImple (javax.transaction.Synchronization ptr)
    {
	_theSynch = ptr;
	_theUid = new Uid();
    }

    public void finalize ()
    {
	_theSynch = null;

	try
	{
	    super.finalize();
	}
	catch (Throwable e)
	{
	}
    }

    public Uid get_uid ()
    {
	return _theUid;
    }
    
    public boolean beforeCompletion ()
    {
	if (jtaLogger.logger.isDebugEnabled())
	{
	    jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
				  "SynchronizationImple.beforeCompletion");
	}

	if (_theSynch != null)
	{
	    try
	    {
		_theSynch.beforeCompletion();

		return true;
	    }
	    catch (Exception e)
	    {
		return false;
	    }
	}
	else
	    return false;
    }

    public boolean afterCompletion (int status)
    {
	if (jtaLogger.logger.isDebugEnabled())
	{
	    jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
				  "SynchronizationImple.afterCompletion");
	}

	if (_theSynch != null)
	{
	    int s = StatusConverter.convert(status);

	    try
	    {
		_theSynch.afterCompletion(s);

		return true;
	    }
	    catch (Exception e)
	    {
		return false; // should not cause any affect!
	    }
	}
	else
	    return false; // should not cause any affect!
    }

    private javax.transaction.Synchronization _theSynch;
    private Uid _theUid;
    
}
