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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: SynchronizationRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.resources;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.internal.arjuna.template.*;

import org.omg.CosTransactions.Synchronization;

/**
 * CosTransactions::Synchronizations are maintained in a separate list
 * from the standard transaction intentions list.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: SynchronizationRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class SynchronizationRecord implements ListElement
{
    
    public SynchronizationRecord (Synchronization ptr)
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "SynchronizationRecord::SynchronizationRecord ( "+ptr+" )");
	}

	_ptr = ptr;
	_uid = new Uid();
    }

    public void finalize ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.DESTRUCTORS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "SynchronizationRecord.finalize ()");
	}

	_ptr = null;
	_uid = null;
    }

    public final Synchronization contents ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS, "SynchronizationRecord::contents - for "+_ptr);
	}

	return _ptr;
    }

    public final Uid get_uid ()
    {
	return _uid;
    }

    private Uid             _uid;
    private Synchronization _ptr;

}
