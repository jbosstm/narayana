/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2003,
 * 
 * Hewlett-Packard Arjuna Labs, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: SubordinateAtomicTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;

/**
 * A subordinate JTA transaction; used when importing another
 * transaction context.
 * 
 * @author mcl
 */

public class SubordinateAtomicTransaction extends com.arjuna.ats.internal.jta.transaction.jts.subordinate.SubordinateAtomicTransaction
{

	public SubordinateAtomicTransaction (Uid actId, Xid xid, int timeout)
	{
		super(new ServerControlWrapper(new ServerControl(new ServerTransaction(actId, xid))));
		
		// add this transaction to the reaper list.
		
		if (timeout > 0)
		{
			TransactionReaper reaper = TransactionReaper.transactionReaper(true);
			
			reaper.insert(super.getControlWrapper(), timeout);
		}
	}
	
	/**
	 * Failure recovery constructor.
	 * 
	 * @param actId transaction to be recovered.
	 */
	
	public SubordinateAtomicTransaction (Uid actId)
	{
		super(new ServerControlWrapper(new ServerControl(new ServerTransaction(actId))));
	}
	
	public final Xid getXid ()
	{
		try
		{
			ServerTransaction tx = (ServerTransaction) super._theAction.getImple().getImplHandle();
			
			return tx.getXid();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
	}

}
