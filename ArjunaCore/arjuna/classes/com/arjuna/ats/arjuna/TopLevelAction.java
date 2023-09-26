/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna;

import com.arjuna.ats.arjuna.coordinator.ActionType;

/**
 * This class provides a (nested) top-level transaction. So, no matter how
 * deeply nested a thread may be within a transaction hierarchy, creating an
 * instance of this class will always start a new top-level transaction. Derived
 * from AtomicAction so we can get the action-to-thread tracking.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: TopLevelAction.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public class TopLevelAction extends AtomicAction
{

    public TopLevelAction()
    {
        super(ActionType.TOP_LEVEL);
    }

}