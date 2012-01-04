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



import java.applet.Applet;
import java.util.Properties;

import org.omg.CORBA.SystemException;

import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;

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
        try
        {
            Class<? extends ORBImple> clazz = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbImpleClass();

            if (opLogger.logger.isTraceEnabled()) {
                opLogger.logger.trace("ORB.initialise() - using ORB Implementation " + clazz.getCanonicalName());
            }

            _theORB = clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError( e );
        }
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

    private final ORBImple _theORB;
}

