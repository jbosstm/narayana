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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ORBImple.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.orb.core;

import java.util.*;
import java.applet.Applet;
import java.io.*;

import org.omg.CORBA.SystemException;

/*
 * Some ORBs either don't support a shutdown operation or
 * don't have one that waits until all outstanding references
 * to the OA/ORB have been released. So, we can't simply
 * null out our reference when our shutdown methods are called.
 * This is more of an issue for multi-threaded applications, where
 * one thread "shuts" the system down, while other threads are still
 * active. So, we have state variables to allow us to tell whether
 * the system is supposed to be shutdown. This then allows us to
 * support multiple initialisations.
 */

/**
 * The Object Request Broker interface.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ORBImple.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public interface ORBImple
{

    /**
     * Is the ORB initialised?
     */

public boolean initialised ();

/**
 * Initialise the ORB.
 */
 
public void init () throws SystemException;
public void init (Applet a, Properties p) throws SystemException;
public void init (String[] s, Properties p) throws SystemException;

/**
 * Shutdown the ORB.
 */

public void shutdown () throws SystemException;

/**
 * Destroy the ORB.
 */

public void destroy () throws SystemException;

/**
 * Return a reference to the ORB.
 */

public org.omg.CORBA.ORB orb () throws SystemException;

/**
 * Provide a reference to the ORB. Used if the application must
 * initialise the ORB separately.
 */

public void orb (org.omg.CORBA.ORB o) throws SystemException;

}
