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
 * Copyright (C) 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts.subordinate.jca;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jta.logging.*;
import com.arjuna.ats.internal.jta.transaction.arjunacore.jca.SubordinateTransaction;

import javax.transaction.xa.Xid;

public class TransactionImple extends
		com.arjuna.ats.internal.jta.transaction.jts.subordinate.TransactionImple implements SubordinateTransaction
{

	/**
	 * Create a new transaction with the specified timeout.
	 */

	public TransactionImple (int timeout)
	{
		this(timeout, null);
	}
	
	public TransactionImple (int timeout, Xid importedXid)
	{
		super(new SubordinateAtomicTransaction(new Uid(), importedXid, timeout));

		TransactionImple.putTransaction(this);
	}

	/**
	 * For crash recovery purposes.
	 * 
	 * @param actId the transaction to recover.
	 */
	
	public TransactionImple (Uid actId)
	{
		super(new SubordinateAtomicTransaction(actId));
	}
	
	/**
	 * Only to be used by crash recovery. Should not be called directly by any
	 * other classes.
	 */
	
	public final void recordTransaction ()
	{
		TransactionImple.putTransaction(this);
	}
	
	/**
	 * Overloads Object.equals()
	 */

	public boolean equals (Object obj)
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA, "TransactionImple.equals");
		}

		if (obj == null)
			return false;

		if (obj == this)
			return true;

		if (obj instanceof TransactionImple)
		{
			return super.equals(obj);
		}

		return false;
	}

	public String toString ()
	{
		if (super._theTransaction == null)
			return "TransactionImple < jca-subordinate, NoTransaction >";
		else
		{
			return "TransactionImple < jca-subordinate, "
					+ super._theTransaction + " >";
		}
	}

	/**
	 * If this is an imported transaction (via JCA) then this will be the Xid
	 * we are pretending to be. Otherwise, it will be null.
	 * 
	 * @return null if we are a local transaction, a valid Xid if we have been
	 * imported.
	 */
	
	public Xid baseXid ()
	{
		return ((SubordinateAtomicTransaction) _theTransaction).getXid();
	}
    
    public void recover() {
        getControlWrapper().getImple().getImplHandle().activate();
    }
    
    public boolean activated() {
        return true; // TODO: more sensible implementation.
    }
	
}
