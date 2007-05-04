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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ReaperElement.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.logging.FacilityCode;

public class ReaperElement implements Comparable
{

	/*
	 * Currently, once created the reaper object and thread stay around forever.
	 * We could destroy both once the list of transactions is null. Depends upon
	 * the relative cost of recreating them over keeping them around.
	 */

	public ReaperElement(Reapable control, int timeout)
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS,
					VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					"ReaperElement::ReaperElement ( " + control + ", "
							+ timeout + " )");
		}

		_control = control;
		_timeout = timeout;

		/*
		 * Given a timeout period in seconds, calculate its absolute value from
		 * the current time of day in milliseconds.
		 */

		_absoluteTimeout = timeout * 1000 + System.currentTimeMillis();
	}

	public void finalize()
	{
		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.DESTRUCTORS,
					VisibilityLevel.VIS_PUBLIC, FacilityCode.FAC_ATOMIC_ACTION,
					"ReaperElement.finalize ()");
		}

		_control = null;
	}

	/**
	 * Order by absoluteTimeout first, then by Uid.
	 * This is required so that the set maintained by the TransactionReaper
	 * is in timeout order for efficient processing.
	 *
	 * @param o
	 * @return
	 */
	public int compareTo(Object o)
	{
		ReaperElement other = (ReaperElement)o;

                if(_absoluteTimeout == other._absoluteTimeout) {
                    if(_control.get_uid().equals(other._control.get_uid())) {
                        return 0;
			} else if (_control.get_uid().greaterThan(other._control.get_uid())) {
				return 1;
                    } else {
				return -1;
                    }
		} else {
			return (_absoluteTimeout > other._absoluteTimeout) ? 1 : -1;
		}
	}

	public Reapable _control;

	public long _absoluteTimeout;

	public int _timeout;
}
