/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator.abstractrecord;

import java.util.ArrayList;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.RecordType;

/**
 * This allows users to define a mapping between record type integers
 * and specific Class-es. This replaces Gandiva from previous releases.
 */

public class RecordTypeManager
{
    /*
     * We can afford to synchronize on the instance because this shouldn't
     * be a performance problem for recovery.
     */
    
    public synchronized Class<? extends AbstractRecord> getClass (int type)
    {
        /*
         * Stop at first hit.
         */
        
        for (int i = 0; i < _map.size(); i++)
        {
            if (_map.get(i).getType() == type)
                return _map.get(i).getRecordClass();
        }
        
        return AbstractRecord.class;
    }
    
    public synchronized int getType (Class<? extends AbstractRecord> c)
    {
        for (int i = 0; i < _map.size(); i++)
        {
            Class<? extends AbstractRecord> recordClass = _map.get(i).getRecordClass();
            if (recordClass != null && recordClass.equals(c))
                return _map.get(i).getType();
        }
        
        return RecordType.UNTYPED;
    }
    
    public synchronized void add (RecordTypeMap map)
    {
        _map.add(map);
    }
    
    public synchronized void remove (RecordTypeMap map)
    {
        _map.remove(map);
    }
    
    public static RecordTypeManager manager ()
    {
        return _instance;
    }
    
    private RecordTypeManager ()
    {
        _map = new ArrayList<RecordTypeMap>();
    }
    
    private ArrayList<RecordTypeMap> _map;
    
    private static final RecordTypeManager _instance = new RecordTypeManager();
    
    static
    {
        /*
         * TODO
         * At present all record types that we need are known at compile time or can be
         * added programmatically. We may want to allow them to be specified dynamically,
         * e.g., on the command line or in a configuration file, but when that requirement
         * happens we can fill in this block ...
         */
    }
}