/*
 * Copyright (C) 2005
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id$
 */
package com.arjuna.ats.internal.jta.resources.jts.orbspecific;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;

/**
 * XAResourceRecord implementing the Last Resource Commit Optimisation.
 * 
 * @author Kevin Conner (Kevin.Conner@arjuna.com)
 * @version $Id$
 * @since ATS 4.1
 */
public class LastResourceRecord extends XAResourceRecord
{
    /**
     * The Uid for all last xa resource records.
     */
    private static final String UID = Uid.maxUid().stringForm() ;
    
    /**
     * Construct the record for last resource commit optimisation. 
     * @param tx The current transaction.
     * @param xaResource The associated XA resource.
     * @param xid The X/Open transaction identifier.
     * @param params Additional parameters.
     */
    public LastResourceRecord(final TransactionImple tx, final XAResource xaResource, final Xid xid, final Object[] params)
    {
        super(tx, xaResource, xid, params) ;
    }
    
    /**
     * The type id for this record.
     */
    public int type_id()
        throws SystemException
    {
        return RecordType.LASTRESOURCE ;
    }
    
    /**
     * The UID for this resource.
     */
    public String uid()
        throws SystemException
    {
        return UID ;
    }
    
    /**
     * Commit this resource.
     */
    public void commit()
        throws SystemException, NotPrepared, HeuristicRollback, HeuristicMixed, HeuristicHazard
    {
    }
    
    /**
     * Prepare this resource.
     */
    public Vote prepare()
        throws HeuristicMixed, HeuristicHazard, SystemException
    {
    	try
    	{
	        commit_one_phase() ;
	        return Vote.VoteCommit ;
    	}
    	catch (final TRANSACTION_ROLLEDBACK tr)
    	{
    		return Vote.VoteRollback ;
    	}
    }
    
    /**
     * The type for saving state.
     */
    public String type()
    {
        return "/CosTransactions/LastXAResourceRecord" ;
    }

	public boolean saveRecord() throws SystemException
	{
		return true;
	}
}
