/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: Lock.java 2342 2006-03-30 13:06:17Z  $
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
