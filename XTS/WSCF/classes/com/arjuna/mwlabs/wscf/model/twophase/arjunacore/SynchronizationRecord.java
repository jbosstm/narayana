/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
     */

    public SynchronizationRecord (Synchronization theResource, Uid id)
    {
	_resourceHandle = theResource;
	_id = new CoordinatorIdImple(id);

        if (_resourceHandle == null)
            wscfLogger.i18NLogger.warn_model_twophase_arjunacore_SynchronizationRecord_1(_id);
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
		wscfLogger.i18NLogger.warn_model_twophase_arjunacore_SynchronizationRecord_2(ex);
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

    @Override
    public boolean isInterposed() {
        return false;
    }

    public Uid get_uid ()
    {
	return _id;
    }

    public int compareTo(Object o) {
        SynchronizationRecord sr = (SynchronizationRecord)o;
        if(_id.equals(sr.get_uid())) {
            return 0;
        } else {
            return _id.lessThan(sr.get_uid()) ? -1 : 1;
        }
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