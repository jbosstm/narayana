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
 * $Id: OA.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.oa.core;

import com.arjuna.common.util.logging.VisibilityLevel;
import com.arjuna.common.util.logging.DebugLevel;

import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.*;

import org.omg.CORBA.Policy;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.SystemException;

import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

/**
 * An instance of this class provides access to the ORB specific
 * Object Adapter class.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: OA.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class OA
{

public OA (com.arjuna.orbportability.orb.core.ORB theORB)
    {
	initialise();

	_theORB = theORB;
    }

public boolean initialised ()
    {
	return _theOA.initialised();
    }

public void init () throws InvalidName, AdapterInactive, SystemException
    {
	((POAImple) _theOA).init(_theORB);
    }

public void createPOA (String adapterName,
		       Policy[] policies) throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive, SystemException
    {
	((POAImple) _theOA).createPOA(adapterName, policies);
    }

public void destroyRootPOA () throws SystemException
    {
	((POAImple) _theOA).destroyRootPOA();
    }

public void destroyPOA (String adapterName) throws SystemException
    {
	((POAImple) _theOA).destroyPOA(adapterName);
    }

public org.omg.PortableServer.POA rootPoa () throws SystemException
    {
	return ((POAImple) _theOA).rootPoa();
    }

public void rootPoa (org.omg.PortableServer.POA thePOA) throws SystemException
    {
	((POAImple) _theOA).rootPoa(thePOA);
    }

public org.omg.PortableServer.POA poa (String adapterName) throws SystemException
    {
	return ((POAImple) _theOA).poa(adapterName);
    }

public void poa (String adapterName, org.omg.PortableServer.POA thePOA) throws SystemException
    {
	((POAImple) _theOA).poa(adapterName, thePOA);
    }

public void run (String name) throws SystemException
    {
	_theOA.run(_theORB, name);
    }

public void run () throws SystemException
    {
	_theOA.run(_theORB);
    }

    /**
     * @message com.arjuna.orbportability.oa.core.OA.nosupportedorb OA ORB specific class creation failed - unable to find supported ORB
     * @message com.arjuna.orbportability.oa.core.OA.caughtexception OA ORB specific class creation failed with: {0}
     */
private final void initialise ()
{
    String className = opPropertyManager.getPropertyManager().getProperty(com.arjuna.orbportability.common.Environment.OA_IMPLEMENTATION);

    if (className == null)
    {

        try
        {
            Thread.currentThread().getContextClassLoader().loadClass("org.jacorb.orb.ORB");

            className = "com.arjuna.orbportability.internal.orbspecific.jacorb.oa.implementations.jacorb_2_0";
        }
        catch (ClassNotFoundException ce)
        {
            try
            {
                Thread.currentThread().getContextClassLoader().loadClass("com.sun.corba.se.internal.corba.ORB");

                className = "com.arjuna.orbportability.internal.orbspecific.javaidl.oa.implementations.javaidl_1_4";
            }
            catch (ClassNotFoundException je)
            {
                if (opLogger.loggerI18N.isFatalEnabled())
                {
                    opLogger.loggerI18N.fatal( "com.arjuna.orbportability.oa.core.OA.nosupportedorb", je );
                }
                throw new ExceptionInInitializerError( opLogger.logMesg.getString("com.arjuna.orbportability.oa.core.OA.nosupportedorb") );
            }
        }
    }

    if (opLogger.logger.isDebugEnabled())
    {
        opLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
                FacilityCode.FAC_ORB_PORTABILITY, "OA.initialise() - using OA Implementation "+className);
    }

    try
    {
        Class c = Thread.currentThread().getContextClassLoader().loadClass(className);

        _theOA = (POAImple) c.newInstance();
    }
    catch (Exception e)
    {
        if (opLogger.loggerI18N.isFatalEnabled())
        {
            opLogger.loggerI18N.fatal( "com.arjuna.orbportability.oa.core.OA.caughtexception",
                    new Object[] { e } , e);
        }

        throw new ExceptionInInitializerError( opLogger.logMesg.getString("com.arjuna.orbportability.oa.core.OA.caughtexception") );
    }
}

private com.arjuna.orbportability.orb.core.ORB     _theORB;
private com.arjuna.orbportability.oa.core.POAImple _theOA;

}

