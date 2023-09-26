/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.txoj;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;
import com.arjuna.ats.internal.txoj.abstractrecords.LockRecord;

/**
 * Module specific class that is responsible for adding any implementations to
 * the inventory.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Implementations.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

class LockRecordTypeMap implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return LockRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.LOCK;
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
            RecordTypeManager.manager().add(new LockRecordTypeMap());
            _added = true;
        }
    }

    private Implementations()
    {
    }

    private static boolean _added = false;

    /**
     * Static block triggers initialisation of lock record type map.
     */
    static
    {
        initialise();
    }

}