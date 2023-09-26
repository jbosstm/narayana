/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.oa.core;

import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;

/**
 * The Portable Object Adapter interface.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: POAImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public interface POAImple
{

    /**
     * Has the Object Adapter been initialised?
     */

    public boolean initialised ();

    /**
     * run is a way of starting a server listening for invocations.
     * For historical reasons we do this via the Object Adapter
     * interface, though POA ORBs may implement this via the ORB.
     */
 
    public void run (com.arjuna.orbportability.orb.core.ORB o, String name) throws SystemException;

    /**
     * run is a way of starting a server listening for invocations.
     * For historical reasons we do this via the Object Adapter
     * interface, though POA ORBs may implement this via the ORB.
     */

    public void run (com.arjuna.orbportability.orb.core.ORB o) throws SystemException;

    /**
     * Initialise the root POA.
     */

    public void init (com.arjuna.orbportability.orb.core.ORB o) throws InvalidName, AdapterInactive, SystemException;

    /**
     * Create a child POA of the root POA.
     */

    public void createPOA (String adapterName,
			   Policy[] policies) throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive, SystemException;

    /**
     * Destroy the root POA.
     */

    public void destroyRootPOA () throws SystemException;
    
    /**
     * Destroy the child POA.
     */

    public void destroyPOA (String adapterName) throws SystemException;
    
    /**
     * @return a reference to the root POA.
     */

    public org.omg.PortableServer.POA rootPoa () throws SystemException;

    /**
     * Provide a reference to the root POA. Used if the application must
     * initialise the POA separately.
     */

    public void rootPoa (org.omg.PortableServer.POA thePOA) throws SystemException;
    
    /**
     * @return a reference to the child POA.
     */
 
    public org.omg.PortableServer.POA poa (String adapterName) throws SystemException;

    /**
     * Provide a reference to the child POA. Used if the application must
     * initialise the POA separately.
     */

    public void poa (String adapterName, org.omg.PortableServer.POA thePOA) throws SystemException;
 
}