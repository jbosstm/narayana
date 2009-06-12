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
import java.util.Comparator;

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

public class SynchronizationImple implements SynchronizationRecord, Comparable
{

    public SynchronizationImple (javax.transaction.Synchronization ptr)
    {
	_theSynch = ptr;
	_theUid = new Uid();
    }

	public SynchronizationImple (javax.transaction.Synchronization ptr, boolean isInterposed) {
		_theSynch = ptr;
		_theUid = new Uid();
		_isInterposed = isInterposed;
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
		_theSynch.beforeCompletion();
		return true;
		// Don't catch and swallow unchecked exceptions here, they may be useful to the caller.
	}
	else
	    return false;
    }

    /**
     * @message com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple SynchronizationImple.afterCompletion - failed for {0} with exception {1}
     */
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
                jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple",
                        new Object[] { _theSynch, e }, e);

                return false; // should not cause any affect!
            }
        }
        else
            return false; // should not cause any affect!
    }

	/*
	 * SyncronizationsImples are ordered first according to the isInterposed property and then by Uid.
	 * Interposed synchronizations must come after non-interposed ones  (see TwoPhaseCoordinator and
	 * TransactionSynchronizationRegistry)  The ordering within the interposed/non-interposed categories
	 * is arbitrary but must be defined for correct functioning of the sort. Just don't rely on it in
	 * application level code.
	 *
	 * @param object
	 * @return
	 */
	public int compareTo(Object object)
	{
		SynchronizationImple other = (SynchronizationImple)object;

		if(this._isInterposed && (!other._isInterposed))
		{
			return 1;
		}
		else if((!this._isInterposed) && other._isInterposed)
		{
			return -1;
		}
		else if(this._theUid.equals(other._theUid))
		{
			return 0;
		}
		else
		{
			return this._theUid.lessThan(other._theUid) ? -1 : 1;
		}
	}

    public String toString() {
        return "SynchronizationImple< "+_theUid.stringForm()+", "+_theSynch+" >";
    }

    private javax.transaction.Synchronization _theSynch;
    private Uid _theUid;
	private boolean _isInterposed;
}
