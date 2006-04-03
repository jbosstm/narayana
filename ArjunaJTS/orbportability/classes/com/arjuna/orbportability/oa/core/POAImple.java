/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: POAImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.oa.core;

import org.omg.PortableServer.*;
import org.omg.CORBA.Policy;
import java.util.*;
import java.applet.Applet;
import java.io.*;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.SystemException;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

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
