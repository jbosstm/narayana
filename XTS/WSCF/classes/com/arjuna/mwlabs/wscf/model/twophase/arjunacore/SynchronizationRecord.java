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
 * $Id: SynchronizationRecord.java,v 1.5 2005/05/19 12:13:38 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.twophase.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.mw.wscf.model.twophase.common.*;
import com.arjuna.mw.wscf.model.twophase.participants.Synchronization;

/**
 * An implementation of the ArjunaCore synchronization interface.
 * Synchronizations take part in the pre- and post- two-phase protocol and
 * are not persistent.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SynchronizationRecord.java,v 1.5 2005/05/19 12:13:38 nmcl Exp $
 */

public class SynchronizationRecord implements com.arjuna.ats.arjuna.coordinator.SynchronizationRecord
{
    
    /**
     * Constructor.
     *
     * @param theResource is the proxy that allows us to call out to the
     * object.
     *
     * @message com.arjuna.mwlabs.wscf.model.twophase.arjunacore.SynchronizationRecord_1 [com.arjuna.mwlabs.wscf.model.twophase.arjunacore.SynchronizationRecord_1] - SynchronizationRecord {0} - null participant provided!
     */

    public SynchronizationRecord (Synchronization theResource, Uid id)
    {
	_resourceHandle = theResource;
	_id = new CoordinatorIdImple(id);
	
	if (_resourceHandle == null)
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ParticipantRecord_1",
					  new Object[]{_id});
    }

    public void finalize () throws Throwable
    {
        _resourceHandle = null;
    }

    public boolean beforeCompletion ()
    {
	if (_resourceHandle != null)
	{
	    try
	    {
		_resourceHandle.beforeCompletion();
		
		return true;
	    }
	    catch (Exception ex)
	    {
		return false;
	    }
	}
	else
	    return false;
    }

    public boolean afterCompletion (int status)
    {
	if (_resourceHandle != null)
	{
	    try
	    {
		_resourceHandle.afterCompletion(convertStatus(status));
	    }
	    catch (Exception ex)
	    {
	    }
	    
	    return true;
        }

        return false;
    }

    public Uid get_uid ()
    {
	return _id;
    }
    
    private final int convertStatus (int result)
    {
	switch (result)
	{
	case ActionStatus.COMMITTED:
	case ActionStatus.COMMITTING:
	    return CoordinationResult.CONFIRMED;
	case ActionStatus.ABORTED:
	case ActionStatus.ABORTING:
	default:
	    return CoordinationResult.CANCELLED;
	}
    }
    
    private Synchronization    _resourceHandle;
    private CoordinatorIdImple _id;
    
}
