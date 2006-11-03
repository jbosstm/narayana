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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryCreator.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.recovery;

import com.arjuna.ats.jts.logging.*;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;

import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.SystemException;

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

    /**
     * @message com.arjuna.ats.internal.jts.recovery.rcnull {0} - being passed a null reference. Will ignore!
     */

public static final void setCreator (RecoveryCreator c)
    {
	if (c == null)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.rcnull",
					  new Object[] { "RecoveryCreator.setCreator"} );
	    }
	}
	else
	    _theCreator = c;
	
    }

public static final RecoveryCoordinator createRecoveryCoordinator (Resource res, Object[] params) throws SystemException
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
				   com.arjuna.ats.arjuna.logging.FacilityCode.FAC_CRASH_RECOVERY,
				   "RecoveryCreator::createRecoveryCoordinator");
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
