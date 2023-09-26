/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.internal.orbspecific.oa.implementations;

import java.util.Hashtable;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

import com.arjuna.orbportability.oa.core.POAImple;

public abstract class POABase implements POAImple
{

    public boolean initialised ()
    {
        return _init;
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