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
 * $Id: ORB.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability.orb.core;



import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.*;

import java.util.*;
import java.applet.Applet;

import org.omg.CORBA.SystemException;

/**
 * An instance of this class provides access to the ORB specific
 * ORB implementation object.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ORB.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.1.
 */

public class ORB
{

public ORB ()
    {
	initialise();
    }

public boolean initialised ()
    {
	return _theORB.initialised();
    }

public void init () throws SystemException
    {
	_theORB.init();
    }

public void init (Applet a, Properties p) throws SystemException
    {
	_theORB.init(a, p);
    }

public void init (String[] s, Properties p) throws SystemException
    {
	_theORB.init(s, p);
    }

public void shutdown () throws SystemException
    {
	_theORB.shutdown();
    }

public void destroy () throws SystemException
    {
	_theORB.destroy();
    }

public org.omg.CORBA.ORB orb () throws SystemException
    {
	return _theORB.orb();
    }

public void orb (org.omg.CORBA.ORB o) throws SystemException
    {
	_theORB.orb(o);
    }

    /**
     * @message com.arjuna.orbportability.orb.core.ORB.unsupportedorb ORB specific class creation failed - unable to find supported ORB
     * @message com.arjuna.orbportability.orb.core.ORB.caughtexception ORB specific class creation failed with: {0}
     */
private final void initialise ()
    {
	/*
	 * Let the application provide its own ORB implementation.
	 */

	String className = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbImplementation();

        if (className == null)
        {
            try
            {
                Thread.currentThread().getContextClassLoader().loadClass("org.jacorb.orb.ORB");

                className = "com.arjuna.orbportability.internal.orbspecific.jacorb.orb.implementations.jacorb_2_0";
            }
            catch (ClassNotFoundException ce)
            {
                //			ce.printStackTrace();

                try
                {
                    Thread.currentThread().getContextClassLoader().loadClass("com.sun.corba.se.internal.corba.ORB");

                    className = "com.arjuna.orbportability.internal.orbspecific.javaidl.orb.implementations.javaidl_1_4";
                }
                catch (ClassNotFoundException je)
                {
                    //			    je.printStackTrace();

                    if (opLogger.loggerI18N.isFatalEnabled())
                    {
                        opLogger.loggerI18N.fatal( "com.arjuna.orbportability.orb.core.ORB.unsupportedorb", je );
                    }

                    throw new ExceptionInInitializerError(opLogger.loggerI18N.getString("com.arjuna.orbportability.orb.core.ORB.unsupportedorb"));
                }
            }
        }

        if (opLogger.logger.isDebugEnabled()) {
            opLogger.logger.debug("ORB.initialise() - using ORB Implementation " + className);
        }

	try
	{
	    Class c = Thread.currentThread().getContextClassLoader().loadClass(className);

	    _theORB = (ORBImple) c.newInstance();
	}
	catch (Exception e)
	{
            if (opLogger.loggerI18N.isFatalEnabled())
            {
                opLogger.loggerI18N.fatal( "com.arjuna.orbportability.orb.core.ORB.caughtexception",
                                new Object[] { e }, e );
            }

	    throw new ExceptionInInitializerError( opLogger.loggerI18N.getString("com.arjuna.orbportability.orb.core.ORB.caughtexception") );
	}
    }

private ORBImple _theORB;

}

