/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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

    public void shutdown (boolean waitForCompletion) throws SystemException
    {
        _theORB.shutdown(waitForCompletion);
    }
    
    public void shutdown () throws SystemException
    {
        _theORB.shutdown(false);
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