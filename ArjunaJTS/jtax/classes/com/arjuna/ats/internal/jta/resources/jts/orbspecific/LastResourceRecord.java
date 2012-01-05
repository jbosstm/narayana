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

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Vote;

import com.arjuna.ArjunaOTS.OTSAbstractRecord;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.utils.jtaxLogger;

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
    private static final String UID = Uid.lastResourceUid().stringForm() ;
    
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
		return false;
	}
    
    public boolean shouldAdd(OTSAbstractRecord record) throws SystemException
    {
        if( record.type_id() == type_id() )
        {
            if(ALLOW_MULTIPLE_LAST_RESOURCES)
            {
                if (!_disableMLRWarning || (_disableMLRWarning && !_issuedWarning))
                {
                    jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_lastResource_multipleWarning(record.toString());
                    _issuedWarning = true;
                }

                return true;
            }
            else
            {
                jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_lastResource_disallow(record.toString());

                return false;
            }
        }
        else
        {
            return true;
        }
    }

    private static final boolean ALLOW_MULTIPLE_LAST_RESOURCES;

    private static boolean _disableMLRWarning = false;
    private static boolean _issuedWarning = false;

    static
    {
        ALLOW_MULTIPLE_LAST_RESOURCES = arjPropertyManager.getCoreEnvironmentBean().isAllowMultipleLastResources();

        if (ALLOW_MULTIPLE_LAST_RESOURCES)
        {
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_lastResource_startupWarning();
        }

        if (arjPropertyManager.getCoreEnvironmentBean().isDisableMultipleLastResourcesWarning())
        {
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_lastResource_disableWarning();

            _disableMLRWarning = true;
        }
    }

}
