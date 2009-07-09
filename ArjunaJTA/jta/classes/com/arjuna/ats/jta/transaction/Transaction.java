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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Transaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.transaction;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jta.xa.TxInfo;

import javax.transaction.xa.*;

import javax.transaction.RollbackException;

import java.lang.IllegalStateException;
import java.util.Map;

/*
 * Extended methods.
 */

public interface Transaction extends javax.transaction.Transaction
{

    public static final int XACONNECTION = 0;
    public static final int XAMODIFIER = 1;

    public boolean enlistResource (XAResource xaRes, Object[] params) throws RollbackException, IllegalStateException, javax.transaction.SystemException;

    public int getXAResourceState (XAResource xaRes);

    // Methods used to support JTA 1.1 TransactionSynchronizationRegistry implementation
	public Object getTxLocalResource(Object key);
	public void putTxLocalResource(Object key, Object value);
    public boolean isAlive();

    Map<Uid, String> getSynchronizations();
    Map<XAResource, TxInfo> getResources();
    int getTimeout(); // total lifetime set, in seconds
    long getRemainingTimeoutMills(); // time remaining until possible expire, in ms. 0 if unknown.
    
    public Uid get_uid(); // get the tx id.
    
    public Xid getTxId ();  // get the global Xid (no branch qualifier).
}
