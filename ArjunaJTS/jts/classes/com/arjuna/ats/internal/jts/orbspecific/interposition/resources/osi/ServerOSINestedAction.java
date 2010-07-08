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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ServerOSINestedAction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.interposition.resources.osi;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.ats.jts.exceptions.ExceptionCodes;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.strict.*;
import com.arjuna.ats.internal.jts.interposition.resources.osi.*;

import org.omg.CosTransactions.*;
import org.omg.CORBA.CompletionStatus;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.UNKNOWN;

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
