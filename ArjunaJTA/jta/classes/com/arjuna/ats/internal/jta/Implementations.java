/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;

class CommitMarkableResourceRecordMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return CommitMarkableResourceRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.COMMITMARKABLERESOURCE;
    }
}

class XAResourceRecordMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return XAResourceRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.JTA_RECORD;
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

	    RecordTypeManager.manager().add(new CommitMarkableResourceRecordMap());
	    RecordTypeManager.manager().add(new XAResourceRecordMap());
	    
	    _added = true;
	}
    }

    private Implementations ()
    {
    }

    private static boolean _added = false;

    /**
     * Static block triggers initialization of XAResourceRecordMap.
     */
    static
    {
	initialise();
    }
    
}