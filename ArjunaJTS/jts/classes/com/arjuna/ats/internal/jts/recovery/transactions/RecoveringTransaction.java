/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id $
 */

package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.arjuna.common.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.SystemException;
import java.util.Date;

/**
 * interface for cachable recoveredtransactions
 * 
 * Needed because the different types of recovered transaction inherit from
 * (non-recovery) base types by different routes
 *
 * used by @{link TransactionCache}
 *
 * Some methods are present only in the Recovered[*]Transactions.
 * Some are present in all the base types
 */

interface RecoveringTransaction
{
    /** only in Recovered{Server}Transaction */
    public void replayPhase2();
    
    /** only in Recovered{Server}Transaction */
    public int getRecoveryStatus ();

    public void addResourceRecord (Uid rcUid, Resource r);
    
    /** present in both base classes OTS_Transaction and OTS_ServerTransaction */
    public Status get_status () throws SystemException;
    public Status getOriginalStatus ();
    
    public boolean allCompleted();
    /**
     * Tell transaction it is assumed to be complete and should convert itself
     * to the appropriate assumed complete type.
     *
     * @returns true if a change is made, false if already assumed complete
     */
    public boolean assumeComplete();
    
    public void removeOldStoreEntry();
    public String type();
    
    /**
     * When was the transaction last attempted. Only used for assumed complete
     * transactions (so perhaps it ought to be in another interface)
     */
    public Date getLastActiveTime();

}
