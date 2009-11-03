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
 * $Id: LockStore.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.txoj.lockstore;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.internal.txoj.Implementations;

import com.arjuna.ats.txoj.exceptions.LockStoreException;

/**
 * The lock store interface is the application's route to using a specific lock
 * store implementation. The interface dynamically binds to an implementation of
 * the right type.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LockStore.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.0.
 */

public abstract class LockStore
{
    public abstract InputObjectState read_state (Uid u, String tName)
            throws LockStoreException;

    /**
     * Remove the state from the lock store.
     */

    public abstract boolean remove_state (Uid u, String tname);

    /**
     * Write the state to the lock store.
     */

    public abstract boolean write_committed (Uid u, String tName,
            OutputObjectState state);

    protected LockStore ()
    {       
    }

    static
    {
        /*
         * Make sure the possible implementations are in the inventory.
         * Otherwise this is going to be a very short ride!
         */

        if (!Implementations.added())
            Implementations.initialise();
    }

}
