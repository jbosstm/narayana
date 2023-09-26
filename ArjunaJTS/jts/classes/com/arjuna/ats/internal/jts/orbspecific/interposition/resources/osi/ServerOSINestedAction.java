/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.osi;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.Coordinator;

import com.arjuna.ats.internal.jts.interposition.resources.osi.OTIDMap;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.strict.ServerStrictNestedAction;
import com.arjuna.ats.jts.logging.jtsLogger;

public class ServerOSINestedAction extends ServerStrictNestedAction
{
    
    /*
     * Create local transactions with same ids as remote.
     * The base class is responsible for registering this resource
     * with its parent.
     */
    
public ServerOSINestedAction (ServerControl control,
				   boolean doRegister)
    {
	super(control, doRegister);

	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerOSINestedAction::ServerOSINestedAction ( " + _theUid + " )");
    }
    }

/*
 * Since we may be called multiple times if we are nested and are propagated
 * to our parents, we remember the initial response and return it subsequently.
 */

public void commit_subtransaction (Coordinator parent) throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerOSINestedAction::commit_subtransaction :" + _theUid);
    }

	/*
	 * First remove entry for this transaction otid
	 * from map. Have to do it here as we are going
	 * to be deleted by the base class!
	 */
    
	OTIDMap.remove(get_uid());
    
	super.commit_subtransaction(parent);
    }

public void rollback_subtransaction () throws SystemException
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("ServerOSINestedAction::rollback_subtransaction :" + _theUid);
    }

	OTIDMap.remove(get_uid());
    
	super.rollback_subtransaction();
    }
 
}