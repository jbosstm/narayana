/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.setup;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.BasicAbstractRecord;

class UserDefFirst0Map implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return BasicAbstractRecord.class;
    }
    
    public int getType ()
    {
        return RecordType.USER_DEF_FIRST0;
    }
}

public class AddUserDefFirst0
{
	public AddUserDefFirst0()
	{
	    RecordTypeManager.manager().add(new UserDefFirst0Map());
	}
}