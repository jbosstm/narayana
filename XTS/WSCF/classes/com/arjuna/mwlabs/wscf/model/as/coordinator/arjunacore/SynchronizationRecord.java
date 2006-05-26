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
 * $Id: SynchronizationRecord.java,v 1.5 2005/05/19 12:13:33 nmcl Exp $
 */

package com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore;

import com.arjuna.mw.wscf.logging.wscfLogger;

import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;

import com.arjuna.mw.wscf.model.as.coordinator.twophase.messages.*;
import com.arjuna.mw.wscf.model.as.coordinator.Participant;

import com.arjuna.mw.wscf.model.twophase.outcomes.*;

import com.arjuna.mw.wsas.activity.Outcome;

import com.arjuna.mw.wsas.completionstatus.*;

import com.arjuna.mw.wscf.common.Qualifier;

/**
 * An implementation of the ArjunaCore synchronization interface.
 * Synchronizations take part in the pre- and post- two-phase protocol and
 * are not persistent.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SynchronizationRecord.java,v 1.5 2005/05/19 12:13:33 nmcl Exp $
 */

public class SynchronizationRecord implements com.arjuna.ats.arjuna.coordinator.SynchronizationRecord
{
    
    /**
     * Constructor.
     *
     * @param theResource is the proxy that allows us to call out to the
     * object.
     *
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_1 [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_1] - SynchronizationRecord {0} - null participant provided!
     */

    public SynchronizationRecord (Participant theResource, Uid id, int priority, Qualifier[] quals)
    {
	_resourceHandle = theResource;
	_priority = priority;
	_quals = quals;
	_id = new CoordinatorIdImple(id);
	
	if (_resourceHandle == null)
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_1",
					  new Object[]{_id});
    }

    public void finalize () throws Throwable
    {
        _resourceHandle = null;
    }

    /**
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_2 [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_2] - SynchronizationRecord.beforeCompletion for {0} called without a resource!
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_3 [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_3] - SynchronizationRecord.beforeCompletion {0} caught exception: {1}
     */

    public boolean beforeCompletion ()
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new BeforeCompletion(_id));
		
		if (res != null)
		{
		    if (res instanceof CoordinationOutcome)
		    {
			return true;
		    }
		    else
			return false;
		}
		else
		    return true;
            }
            else
		wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_2",
					      new Object[]{_id});
        }
        catch (Exception e)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_3",
					  new Object[]{_id, e});

	    e.printStackTrace();
        }

        return false;
    }

    /**
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_4 [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_4] - SynchronizationRecord.afterCompletion for {0} called without a resource!
     * @message com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_5 [com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_5] - SynchronizationRecord.afterCompletion {0} caught exception: {1}
     */

    public boolean afterCompletion (int status)
    {
        try
        {
            if (_resourceHandle != null)
            {
		Outcome res = _resourceHandle.processMessage(new AfterCompletion(_id, convertStatus(status)));
		
		if (res != null)
		{
		    if (res instanceof CoordinationOutcome)
		    {
			return true;
		    }
		    else
			return false;
		}
		else
		    return true;
            }
            else
		wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_4",
					      new Object[]{_id});
        }
        catch (Exception e)
        {
	    wscfLogger.arjLoggerI18N.warn("com.arjuna.mwlabs.wscf.model.as.coordinator.arjunacore.SynchronizationRecord_5",
					  new Object[]{_id, e});

	    e.printStackTrace();
        }

        return false;
    }

    public Uid get_uid ()
    {
	return _id;
    }
    
    private final CompletionStatus convertStatus (int result)
    {
	switch (result)
	{
	case ActionStatus.COMMITTED:
	case ActionStatus.COMMITTING:
	    return Success.instance();
	case ActionStatus.ABORTED:
	case ActionStatus.ABORTING:
	default:
	    return Failure.instance();
	}
    }
    
    private Participant        _resourceHandle;
    private int                _priority;
    private Qualifier[]        _quals;
    private CoordinatorIdImple _id;
    
}
