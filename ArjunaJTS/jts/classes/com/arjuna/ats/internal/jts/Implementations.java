/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;
import com.arjuna.ats.internal.jts.resources.ResourceRecord;

class ResourceRecordMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return ResourceRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.OTS_RECORD;
    }
}

class ExtendedResourceRecordMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return ExtendedResourceRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.OTS_ABSTRACTRECORD;
    }
}

public class Implementations
{

    public static synchronized boolean added ()
    {
	return _added;
    }
    
    public static synchronized void initialise ()
    {
	if (!_added)
	{
	    /*
	     * Now add various abstract records which crash recovery needs.
	     */

	    RecordTypeManager.manager().add(new ResourceRecordMap());
	    RecordTypeManager.manager().add(new ExtendedResourceRecordMap());

	    _added = true;
	}
    }

    private Implementations ()
    {
    }

    private static boolean _added = false;

    /**
     * Static block triggers initialization of ResourceRecordMap and ExtendedResourceRecordMap.
     */
    static
    {
	initialise();
    }
    
}