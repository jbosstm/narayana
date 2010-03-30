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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: GenericRecoveryCreator.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery.recoverycoordinators;

import org.omg.CosTransactions.*;
import org.omg.CORBA.SystemException;

import com.arjuna.ats.jts.logging.jtsLogger;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.jts.utils.Utility;

import com.arjuna.orbportability.orb.*;
import com.arjuna.orbportability.*;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

import com.arjuna.ats.jts.*;
import com.arjuna.ats.internal.jts.recovery.RecoveryCreator;

import com.arjuna.ats.internal.jts.recovery.*;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.*;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.*;
import com.arjuna.ArjunaOTS.*;

import java.lang.ClassCastException;

/**
 * Implementation of {@link com.arjuna.ats.internal.jts.recovery.RecoveryCreator}.
 * Orb-specific aspects, especially the specific construction of the 
 * RecoveryCoordinator IOR are delegated to an implementation of {@link RcvCoManager}.
 * The RCs may be created locally (depends on the orb-specific mechanisms) but
 * will be recreated in the RecoveryManager if called there (possibly following
 * a crash of this process).
 * 
 * @message com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCreator_1 [com.arjuna.ats.internal.jts.recovery.RecoveryCoordinator.GenericRecoveryCreator_1] - GenericRecoveryCreator: Missing params to create
 */

public class GenericRecoveryCreator extends RecoveryCreator
{

private GenericRecoveryCreator (RcvCoManager specificManager)
    {
	super();
	_orbSpecificManager = specificManager;
    }

/**
 * Create an instance of this class, which will delegate the orb-specific
 * aspects to the supplied RcvCoManager instance and register it with 
 * the transaction service.
 */
public static void register(RcvCoManager theManager)
    {
	RecoveryCreator theCreator = new GenericRecoveryCreator(theManager);
	
	RecoveryCreator.setCreator(theCreator);
    }
    

    /**
     * Create a new RecoveryCoordinator for Resource res. The params
     * array is used to pass additional data. Currently params[0] is
     * the ArjunaTransactionImple ref. When create returns additional data is
     * passed back using params. Currently returned params[0] is the
     * RecoveryCoordinator Uid.
     */

    public RecoveryCoordinator create (Resource res, Object[] params) throws SystemException
    {
	RecoveryCoordinator recoveryCoordinator = null;

	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("GenericRecoveryCreator.create()");
    }
	
	// we dont use the res parameter in this version
	if ((params != null) && (params[0] != null) )
	{
	    int index = 0;

	    ArjunaTransactionImple otsTransaction = (ArjunaTransactionImple) params[index++];

	    // Get the Uid of the top-level transaction. This will be
	    // the top-level interposed transaction in the case of
	    // interposition.
	    BasicAction rootAction = otsTransaction;

	    while ((rootAction.parent()) != null)
		rootAction = rootAction.parent();

	    Uid rootActionUid = rootAction.getSavingUid();

	    //Uid processUid = Utility.getProcessUid(); 
	    Uid processUid = com.arjuna.ats.arjuna.utils.Utility.getProcessUid();
	    
	    // Create a Uid for the new RecoveryCoordinator
	    Uid RCUid = new Uid();

	    // Is this transaction a ServerTransaction?
	    boolean isServerTransaction = (otsTransaction instanceof ServerTransaction);


	    // Now ask the orb-specific bit to make the RecoveryCoordinator IOR
	    //  (it may or may not actually make the RC itself)
	    recoveryCoordinator = _orbSpecificManager.makeRC (RCUid, rootActionUid, processUid, isServerTransaction);

	    // Tidy up
	    otsTransaction = null;
	    rootAction = null;

	    // Pass the RecoveryCoordinator Uid back
	    params[0] = RCUid;
	}
	else
	{

	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCreator_1");
	    
	}
	return recoveryCoordinator;
    }

    /**
     * The RC instance is not longer needed by the application. This is a null-op
     * for orb environments that do not actually create RC objects in the original 
     * process.
     */
public void destroy (RecoveryCoordinator rc) throws SystemException
    {
	// this depends on the orb - perhaps it isn't here anyway
	_orbSpecificManager.destroy(rc);
    }

    /**
     * Destroy all RC instances for the transactions identified in params.
     * This is a null-op for orb environments that do not actually create 
     * RC objects in the original process.
     */
public void destroyAll (Object[] params) throws SystemException
    {
	// this depends on the orb - perhaps it isn't here anyway
	_orbSpecificManager.destroyAll(params);
    }

    /**
     * Get the service name. This ties the recoverycoordinators whose IOR's 
     * are created here with the RecoveryManager that will recreate them
     * in recovery.
     */
public static String getRecCoordServiceName ()
    {
	// The following will be deleted
	String tag = com.arjuna.ats.internal.jts.orbspecific.recovery.RecoveryEnablement.getRecoveryManagerTag();
	if (tag != null) {
	    return new String(_RecCoordServiceBaseName + tag);
	} else {
	    return null;
	}
    }

private RcvCoManager _orbSpecificManager;

private static final char   _RecCoordServiceObjKeyDelimiter = '*';
private static final String _RecCoordServiceBaseName = "RecCoService_";

    
};
