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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ORBBase.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.internal.orbspecific.orb.implementations;

import com.arjuna.orbportability.orb.core.ORBImple;

import java.util.*;
import java.applet.Applet;
import java.io.*;

import org.omg.CORBA.SystemException;

/**
 * The base class from which all ORB implementations are derived. Each
 * such implementation may be responsible for ensuring that the right
 * ORB specific properties (such as org.omg.CORBA.ORBClass) are set.
 */

public class ORBBase implements ORBImple
{

public synchronized boolean initialised ()
    {
	return _init;
    }

public synchronized void init () throws SystemException
    {
	if (!_init)
	{
	    _orb = org.omg.CORBA.ORB.init();
	    _init = true;
	}
    }
 
public synchronized void init (Applet a, Properties p) throws SystemException
    {
	if (!_init)
	{
	    _orb = org.omg.CORBA.ORB.init(a, p);
	    _init = true;
	}
    }
 
public synchronized void init (String[] s, Properties p) throws SystemException
    {
	if (!_init)
	{
	    _orb = org.omg.CORBA.ORB.init(s, p);
	    _init = true;
	}
    }

public synchronized void shutdown () throws SystemException
    {
	if (_init)
	{
	    _orb.shutdown(false);
	    _init = false;
	}
    }

public synchronized void destroy () throws SystemException
    {
	shutdown();
    }
 
public synchronized org.omg.CORBA.ORB orb () throws SystemException
    {
	return _orb;
    }

public synchronized void orb (org.omg.CORBA.ORB o) throws SystemException
    {
	_orb = o;
	_init = true;
    }

protected ORBBase ()
    {
    }

protected org.omg.CORBA.ORB _orb = null;
protected boolean           _init = false;

}
