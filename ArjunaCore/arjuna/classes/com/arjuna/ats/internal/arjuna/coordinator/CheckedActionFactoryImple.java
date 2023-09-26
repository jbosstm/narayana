/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;

/**
 * Return a checked action instance. The factory 
 */

public class CheckedActionFactoryImple implements CheckedActionFactory
{
    /**
     * This implementation returns the same CheckedAction instance for every transaction.
     * Since the check method of the instance returned is stateless this works. But it
     * is not guaranteed to be correct for other implementations of CheckedAction so only
     * follow this pattern if you understand the implications.
     */
    
    public CheckedAction getCheckedAction (final Uid txId, final String actionType)
    {
        return _theAction;
    }
    
    private final CheckedAction _theAction = new CheckedAction();
}