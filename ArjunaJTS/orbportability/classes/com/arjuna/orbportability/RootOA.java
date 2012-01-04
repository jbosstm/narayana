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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RootOA.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability;

import java.util.Enumeration;

import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.SystemException;
import org.omg.PortableServer.Servant;

import com.arjuna.orbportability.event.EventManager;
import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.oa.PostShutdown;
import com.arjuna.orbportability.oa.PreShutdown;

/**
 * RootOA class which represents a RootPOA
 *
 * @author Richard Begg (richard_begg@hp.com)
 */
public class RootOA extends OA
{
    /**
     * Creates a RootOA class which represents the RootOA of the given ORB
     *
     * @param The ORB to create a root OA for
     */
    RootOA (com.arjuna.orbportability.ORB orb)
    {
        super(orb,DEFAULT_ROOT_NAME);
    }

    /**
     * Destroy this root POA and all of its children
     */
    public synchronized void destroy () throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::destroyRootPOA ()");
        }

        if (_oa.initialised())
        {
            if (!_preOAShutdown.isEmpty())
            {
                Enumeration elements = _preOAShutdown.elements();

                while (elements.hasMoreElements())
                {
                    PreShutdown c = (PreShutdown) elements.nextElement();

                    if (c != null)
                    {
                        c.work();
                        c = null;
                    }
                }

                //		_preOAShutdown.clear();
            }

            _oa.destroyRootPOA();

            if (!_postOAShutdown.isEmpty())
            {
                Enumeration elements = _postOAShutdown.elements();

                while (elements.hasMoreElements())
                {
                    PostShutdown c = (PostShutdown) elements.nextElement();

                    if (c != null)
                    {
                        c.work();
                        c = null;
                    }
                }

                //		_postOAShutdown.clear();
            }
        }
    }

public org.omg.CORBA.Object corbaReference (Servant obj)
    {
        org.omg.CORBA.Object objRef = null;

        try
        {
            objRef = corbaReference(obj, _oa.rootPoa());
        }
        catch (Exception e)
        {
            objRef = null;
        }

        return objRef;
    }

public boolean objectIsReady (Servant obj, byte[] id) throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("RootOA::objectIsReady (Servant, byte[], " + _oaName + ")");
        }

        try
        {
            _oa.rootPoa().activate_object_with_id(id, obj);
        }
        catch (Exception e)
        {
            opLogger.i18NLogger.warn_OA_exceptioncaughtforobj("objectIsReady", obj.toString(), e);

            return false;
        }

        return true;
    }

public boolean objectIsReady (Servant obj) throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("RootOA::objectIsReady (Servant)");
        }

        boolean result = true;

        try
        {
            boolean invalidPOA = false;

            if (_oa.initialised())
            {
                _oa.rootPoa().activate_object(obj);
            }
            else
                invalidPOA = true;

            if (invalidPOA)
            {
                opLogger.i18NLogger.warn_OA_invalidpoa("objectIsReady", "rootPOA");

                result = false;
            }

	    //??

            EventManager.getManager().connected(corbaReference(obj));
        }
        catch (Exception e)
        {
            opLogger.i18NLogger.warn_OA_exceptioncaughtforobj("objectIsReady", obj.toString(), e);

            result = false;
        }

        return result;
    }

public boolean shutdownObject (org.omg.CORBA.Object obj)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("RootOA::shutdownObject ()");
        }

        boolean result = true;

        try
        {
            EventManager.getManager().disconnected(obj);

            boolean invalidPOA = false;

            if (_oa.initialised())
                _oa.rootPoa().deactivate_object(_oa.rootPoa().reference_to_id(obj));
            else
                invalidPOA = true;

            if (invalidPOA)
            {
                opLogger.i18NLogger.warn_OA_invalidpoa("objectIsReady", "rootPOA");

                result = false;
            }
        }
        catch (Exception e)
        {
            opLogger.i18NLogger.warn_OA_caughtexception("objectIsReady", e);

            result = false;
        }

        return result;
    }

public boolean shutdownObject (Servant obj)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("RootOA::shutdownObject (Servant)");
        }

        boolean result = true;

        try
        {
            boolean invalidPOA = false;

            if (_oa.initialised())
                _oa.rootPoa().deactivate_object(_oa.rootPoa().servant_to_id(obj));
            else
                invalidPOA = true;

            if (invalidPOA)
            {
                opLogger.i18NLogger.warn_OA_invalidpoa("shutdownObject", "rootPOA");

                result = false;
            }
        }
	catch (NullPointerException ex)
	{
	    /*
	     * Ignore this as it means some other thread/process was sharing
	     * the POA and shut it down itself.
	     */
	}
        catch (Exception e)
        {
            if(e instanceof OBJECT_NOT_EXIST) {
                // ignore - probably something else shut down the POA already
            } else {
                opLogger.i18NLogger.warn_OA_caughtexception("shutdownObject", e);

                result = false;
            }
        }

        return result;
    }

public static final String DEFAULT_ROOT_NAME = "RootPOA";
}
