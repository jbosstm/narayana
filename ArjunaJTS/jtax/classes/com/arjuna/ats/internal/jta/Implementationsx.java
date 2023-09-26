/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.jts.resources.ExtendedResourceRecord;

class ExtendedXAResourceRecordMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return ExtendedResourceRecord.class;//XAResourceRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.JTAX_RECORD;
    }
}

public class Implementationsx
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

	    RecordTypeManager.manager().add(new ExtendedXAResourceRecordMap());

	    _added = true;
	}
    }

    private Implementationsx ()
    {
    }

    private static boolean _added = false;

    /**
     * Static block triggers initialization of ExtendedXAResourceRecordMap.
     */
    static
    {
	initialise();
    }
    
}