/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.stm.internal.optimistic;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.txoj.Lock;

/**
 * Instances of this class (or derived user classes) are used when trying to set
 * a lock. The default implementation provides a single-write/multiple-reader
 * policy. However, by overridding the appropriate methods, other, type-specific
 * concurrency control locks can be implemented.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: Lock.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

// todo different lock mode instead of WRITE and READ?

public class OptimisticLock extends Lock
{
    public OptimisticLock ()
    {
        super();
    }
    
    public OptimisticLock (int lm)
    {
        super(lm);
    }
    
    public OptimisticLock (final Uid storeId)
    {
        super(storeId);
    }

    /**
     * Implementation of Lock conflict check. Returns TRUE if there is conflict
     * FALSE otherwise. Does not take account of relationship in the atomic
     * action hierarchy since this is a function of LockManager.
     * 
     * @return <code>true</code> if this lock conflicts with the parameter,
     *         <code>false</code> otherwise.
     */

    public boolean conflictsWith (Lock otherLock)
    {
        return false; /* no conflict between these locks */
    }

    /**
     * Overrides StateManager.type()
     */

    public String type ()
    {
        return "/StateManager/Lock/OptimisticLock";
    }
}