/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.orbportability.internal.orbspecific.orb.implementations;

import java.applet.Applet;
import java.util.Properties;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import com.arjuna.orbportability.common.OrbPortabilityEnvironmentBean;
import org.omg.CORBA.SystemException;

import com.arjuna.orbportability.orb.core.ORBImple;

/**
 * The base class from which all ORB implementations are derived. Each such
 * implementation may be responsible for ensuring that the right ORB specific
 * properties (such as org.omg.CORBA.ORBClass) are set.
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

    public synchronized void init (Applet a, Properties p)
            throws SystemException
    {
        if (!_init)
        {
            _orb = org.omg.CORBA.ORB.init(a, p);
            _init = true;
        }
    }

    public synchronized void init (String[] s, Properties p)
            throws SystemException
    {
        if (!_init)
        {
            _orb = org.omg.CORBA.ORB.init(s, p);
            _init = true;
        }
    }

    public synchronized void shutdown () throws SystemException
    {
        shutdown(false);
    }
    
    public synchronized void shutdown (boolean waitForCompletion) throws SystemException
    {
        if (_init)
        {
            OrbPortabilityEnvironmentBean env = BeanPopulator.getDefaultInstance(OrbPortabilityEnvironmentBean.class);
            if (env.isShutdownWrappedOrb()) {
                _orb.shutdown(waitForCompletion);
                _init = false;
            }
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

    protected ORBBase()
    {
    }

    protected org.omg.CORBA.ORB _orb = null;

    protected boolean _init = false;

}