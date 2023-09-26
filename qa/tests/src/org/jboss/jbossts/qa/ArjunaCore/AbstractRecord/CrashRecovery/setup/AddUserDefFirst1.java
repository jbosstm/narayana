/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.setup;

import org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.impl.CrashAbstractRecord02;

import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeManager;
import com.arjuna.ats.arjuna.coordinator.abstractrecord.RecordTypeMap;

class UserDefFirst1Map implements RecordTypeMap
{
    @SuppressWarnings("unchecked")
    public Class getRecordClass ()
    {
        return CrashAbstractRecord02.class;
    }
    
    public int getType ()
    {
        return RecordType.USER_DEF_FIRST1;
    }
}

public class AddUserDefFirst1
{
	public AddUserDefFirst1()
	{
	    RecordTypeManager.manager().add(new UserDefFirst1Map());
	}
}