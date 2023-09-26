/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.coordinator.abstractrecord;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;

/**
 * This allows users to define a mapping between record type integers
 * and specific Class-es. This replaces Gandiva from previous releases.
 */

public interface RecordTypeMap
{ 
    public Class<? extends AbstractRecord> getRecordClass ();
    
    public int getType ();
}