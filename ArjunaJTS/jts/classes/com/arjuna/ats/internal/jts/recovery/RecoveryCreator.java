/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.recovery;

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.Resource;

import com.arjuna.ats.jts.logging.jtsLogger;

/*
 * Default visibility.
 */

class DummyCreator extends RecoveryCreator
{
    
public RecoveryCoordinator create (Resource res, Object[] params) throws SystemException
    {
	throw new NO_IMPLEMENT();
    }

public void destroy (RecoveryCoordinator rc) throws SystemException
    {
    }

public void destroyAll (Object[] params) throws SystemException
    {
    }
    
};

/**
 * This abstract class is used to allow dynamic registration of creators
 * for RecoveryCoordinators. This means that we can provide ORB specific
 * implementations without having to have implementation specific
 * information within the transaction service.
 */

public abstract class RecoveryCreator
{
    
public static final RecoveryCreator getCreator ()
    {
	if (_theCreator == null)
	    _theCreator = new DummyCreator();
    
	return _theCreator;
    }


public static final void setCreator (RecoveryCreator c)
    {
	if (c == null) {
        jtsLogger.i18NLogger.warn_recovery_rcnull("RecoveryCreator.setCreator");
    }
	else
	    _theCreator = c;
	
    }

public static final RecoveryCoordinator createRecoveryCoordinator (Resource res, Object[] params) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled()) {
        jtsLogger.logger.debug("RecoveryCreator::createRecoveryCoordinator");
    }

	return getCreator().create(res, params);
    }

public static final void destroyRecoveryCoordinator (RecoveryCoordinator rc) throws SystemException
    {
	getCreator().destroy(rc);
    }

public static final void destroyAllRecoveryCoordinators (Object[] params) throws SystemException
    {
	getCreator().destroyAll(params);
    }
    
protected abstract RecoveryCoordinator create (Resource res, Object[] params) throws SystemException;
protected abstract void destroy (RecoveryCoordinator rc) throws SystemException;
protected abstract void destroyAll (Object[] params) throws SystemException;
    
private static RecoveryCreator _theCreator = null;
 
}