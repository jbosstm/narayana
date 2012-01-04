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




import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;

import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;

/**
 * An instance of this class provides access to the ORB specific Object Adapter
 * class.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: OA.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 2.1.
 */

public class OA
{
    public OA(com.arjuna.orbportability.orb.core.ORB theORB)
    {
        _theORB = theORB;

        try
        {
            Class<? extends POAImple> clazz = opPropertyManager.getOrbPortabilityEnvironmentBean().getPoaImpleClass();

            if (opLogger.logger.isTraceEnabled()) {
                opLogger.logger.trace("OA.initialise() - using OA Implementation " + clazz.getCanonicalName());
            }

            _theOA = clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError( e );
        }
    }

    public boolean initialised ()
    {
        return _theOA.initialised();
    }

    public void init () throws InvalidName, AdapterInactive, SystemException
    {
        _theOA.init(_theORB);
    }

    public void createPOA (String adapterName, Policy[] policies)
            throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive,
            SystemException
    {
        _theOA.createPOA(adapterName, policies);
    }

    public void destroyRootPOA () throws SystemException
    {
        _theOA.destroyRootPOA();
    }

    public void destroyPOA (String adapterName) throws SystemException
    {
        _theOA.destroyPOA(adapterName);
    }

    public org.omg.PortableServer.POA rootPoa () throws SystemException
    {
        return _theOA.rootPoa();
    }

    public void rootPoa (org.omg.PortableServer.POA thePOA)
            throws SystemException
    {
        _theOA.rootPoa(thePOA);
    }

    public org.omg.PortableServer.POA poa (String adapterName)
            throws SystemException
    {
        return _theOA.poa(adapterName);
    }

    public void poa (String adapterName, org.omg.PortableServer.POA thePOA)
            throws SystemException
    {
        _theOA.poa(adapterName, thePOA);
    }

    public void run (String name) throws SystemException
    {
        _theOA.run(_theORB, name);
    }

    public void run () throws SystemException
    {
        _theOA.run(_theORB);
    }

    private final com.arjuna.orbportability.orb.core.ORB _theORB;

    private final com.arjuna.orbportability.oa.core.POAImple _theOA;

}
