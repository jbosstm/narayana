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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RebindFactory.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.recovery;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.common.opPropertyManager;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.SystemException;

/**
 * Rebind factory takes a stringified IOR or an object name and
 * returns the relevant object recovery class to use. If none
 * has been provided via java properties, then the default one
 * will be returned.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: RebindFactory.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public class RebindFactory
{

    /**
     * Given the name of the object (some application specific identifier) or
     * the object reference, we look up a recovery mechanism for the object.
     * It is valid for either the name or the object reference to be null in
     * this call, but not both.
     */

public static IORRecovery getRecovery (ORB orb, String name, org.omg.CORBA.Object obj) throws SystemException
    {
	if ((name == null) && (obj == null))
	    throw new BAD_PARAM();
	
	String recoveryClassName = null;
	
	if (name != null)
	{
	    recoveryClassName = opPropertyManager.getPropertyManager().getProperty(name);
	    
	    if (recoveryClassName != null)
		return createRecoveryClass(recoveryClassName);
	}
	
	if (obj != null)
	{
	    try
	    {
		String ior = orb.orb().object_to_string(obj);

		recoveryClassName = opPropertyManager.getPropertyManager().getProperty(ior);
		
		if (recoveryClassName != null)
		    return createRecoveryClass(recoveryClassName);
	    }
	    catch (SystemException e1)
	    {
                if (opLogger.logger.isWarnEnabled())
                {
                    opLogger.logger.warn(e1.toString());
                }

		throw e1;
	    }
	    catch (Exception e2)
	    {
                if (opLogger.logger.isWarnEnabled())
                {
                    opLogger.logger.warn(e2.toString());
                }

		throw new BAD_OPERATION(e2.toString());
	    }
	}
	
	return new DefaultIORRecovery();
    }

private static final IORRecovery createRecoveryClass (String className) throws SystemException
    {
	try
	{
	    Class c = Thread.currentThread().getContextClassLoader().loadClass(className);

	    return (IORRecovery) c.newInstance();
	}
	catch (Exception e)
	{
	    throw new BAD_OPERATION(e.toString());
	}
    }

};
