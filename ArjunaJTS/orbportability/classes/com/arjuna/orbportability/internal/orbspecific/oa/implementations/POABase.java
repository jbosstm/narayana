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
 * $Id: POABase.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.internal.orbspecific.oa.implementations;

import com.arjuna.orbportability.oa.core.POAImple;

import org.omg.PortableServer.*;
import java.util.*;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

public abstract class POABase implements POAImple
{

    public boolean initialised ()
    {
        return _init;
    }

    public boolean supportsBOA ()
    {
        return false;
    }

    public boolean supportsPOA ()
    {
        return true;
    }

    public void init (com.arjuna.orbportability.orb.core.ORB orb)
            throws InvalidName, AdapterInactive, SystemException
    {
        if (!_init)
        {
            _poa = POAHelper.narrow(orb.orb().resolve_initial_references(
                    "RootPOA"));

            _poa.the_POAManager().activate();
            _init = true;
        }
    }

    public void destroyRootPOA () throws SystemException
    {
        _poa.destroy(true, true);
        _init = false;
    }

    public void destroyPOA (String adapterName) throws SystemException
    {
        if (adapterName == null)
            throw new BAD_PARAM();

        org.omg.PortableServer.POA childPoa = (org.omg.PortableServer.POA) _poas
                .remove(adapterName);

        if (childPoa != null)
        {
            childPoa.destroy(true, true);
            childPoa = null;
        }
        else
            throw new BAD_OPERATION();
    }

    public org.omg.PortableServer.POA rootPoa () throws SystemException
    {
        return _poa;
    }

    public void rootPoa (org.omg.PortableServer.POA thePOA)
            throws SystemException
    {
        _poa = thePOA;

        _init = true;
    }

    public org.omg.PortableServer.POA poa (String adapterName)
            throws SystemException
    {
        return (org.omg.PortableServer.POA) _poas.get(adapterName);
    }

    public void poa (String adapterName, org.omg.PortableServer.POA thePOA)
            throws SystemException
    {
        _poas.put(adapterName, thePOA);
    }

    public void run (com.arjuna.orbportability.orb.core.ORB orb, String name)
            throws SystemException
    {
        orb.orb().run();
    }

    public void run (com.arjuna.orbportability.orb.core.ORB orb)
            throws SystemException
    {
        orb.orb().run();
    }

    protected org.omg.PortableServer.POA _poa = null;

    protected Hashtable _poas = new Hashtable();

    protected boolean _init = false;

}
