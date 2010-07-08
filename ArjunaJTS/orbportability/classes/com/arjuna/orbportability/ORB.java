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
 * $Id: ORB.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability;

import com.arjuna.orbportability.orb.*;
import com.arjuna.orbportability.common.opPropertyManager;
import com.arjuna.orbportability.logging.opLogger;

import com.arjuna.orbportability.internal.utils.*;

import com.arjuna.orbportability.logging.*;



import java.util.*;
import java.applet.Applet;

import org.omg.CORBA.SystemException;

/**
 * An attempt at some ORB portable ways of interacting with the ORB.
 *
 * NOTE: initORB *must* be called if you want to use the
 * pre- and post- initialisation mechanisms.
 *
 * @author Mark Little (mark@arjuna.com), Richard Begg (richard.begg@arjuna.com)
 * @version $Id: ORB.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ORB
{
    /**
     * Initialise the default ORB.
     */

public synchronized void initORB () throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::initORB ()");
        }

	/*
	 * Since an ORB can be initialised multiple times we currently
	 * allow the initialisation code to be activated multiple times
	 * as well. Does this make sense?
	 */

	if (!_orb.initialised())
	{
	    // null op - just skip it loadProperties(null);

            /**
             * Perform pre-initialisation classes for all ORBs
             */
	    PreInitLoader preInit = new PreInitLoader(PreInitLoader.generateORBPropertyName(ORB_INITIALISER_NS), this);

            /**
             * Perform pre-initialisation classes for this ORB only
             */
	    preInit = new PreInitLoader(PreInitLoader.generateORBPropertyName(ORB_INITIALISER_NS,_orbName), this);
	    preInit = null;

	    parseProperties(null, false);

	    _orb.init();

	    parseProperties(null, true);

            /**
             * Perform post-initialisation classes for all ORBs
             */
	    PostInitLoader postInit = new PostInitLoader(PostInitLoader.generateORBPropertyName(ORB_INITIALISER_NS), this);

            /**
             * Perform post-initialisation classes for this ORB only
             */
	    postInit = new PostInitLoader(PostInitLoader.generateORBPropertyName(ORB_INITIALISER_NS,_orbName), this);
	    postInit = null;
	}
    }

    /**
     * Initialise the ORB.
     */

public synchronized void initORB (Applet a, Properties p) throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::initORB (Applet, Properties)");
        }

	if (!_orb.initialised())
	{
	    loadProperties(p);

            /**
             * Perform pre-initialisation classes for all ORBs
             */
	    PreInitLoader preInit = new PreInitLoader(PreInitLoader.generateORBPropertyName(ORB_INITIALISER_NS), this);

            /**
             * Perform pre-initialisation classes for this ORB only
             */
	    preInit = new PreInitLoader(PreInitLoader.generateORBPropertyName(ORB_INITIALISER_NS,_orbName), this);
	    preInit = null;

	    parseProperties(null, false);

	    _orb.init(a, p);

	    parseProperties(null, true);

            /**
             * Perform post-initialisation classes for all ORBs
             */
	    PostInitLoader postInit = new PostInitLoader(PostInitLoader.generateORBPropertyName(ORB_INITIALISER_NS), this);

            /**
             * Perform post-initialisation classes for this ORB only
             */
	    postInit = new PostInitLoader(PostInitLoader.generateORBPropertyName(ORB_INITIALISER_NS,_orbName), this);
	    postInit = null;
	}
    }

    /**
     * Initialise the ORB.
     */

public synchronized void initORB (String[] s, Properties p) throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::initORB (String[], Properties)");
        }

	if (!_orb.initialised())
	{
	    loadProperties(p);

            /**
             * Perform pre-initialisation classes for all ORBs
             */
	    PreInitLoader preInit = new PreInitLoader(PreInitLoader.generateORBPropertyName(ORB_INITIALISER_NS), this);

            /**
             * Perform pre-initialisation classes for this ORB only
             */
	    preInit = new PreInitLoader(PreInitLoader.generateORBPropertyName(ORB_INITIALISER_NS,_orbName), this);
	    preInit = null;

	    parseProperties(s, false);

	    _orb.init(s, p);

	    parseProperties(s, true);

            /**
             * Perform post-initialisation classes for all ORBs
             */
	    PostInitLoader postInit = new PostInitLoader(PostInitLoader.generateORBPropertyName(ORB_INITIALISER_NS), this);

            /**
             * Perform post-initialisation classes for this ORB only
             */
	    postInit = new PostInitLoader(PostInitLoader.generateORBPropertyName(ORB_INITIALISER_NS,_orbName), this);
	    postInit = null;
	}
    }

public synchronized boolean addAttribute (Attribute p)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::addAttribute (" + p + ")");
        }

	if (_orb.initialised())  // orb already set up!
	    return false;

	if (p.postORBInit())
	    _postORBInitProperty.put(p, p);
	else
	    _preORBInitProperty.put(p, p);

	return true;
    }

    /**
     * Shutdown the ORB.
     */

public synchronized void shutdown ()
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::shutdown ()");
        }

	/*
	 * Do the cleanups first!
	 */

	if (!_preORBShutdown.isEmpty())
	{
	    Enumeration elements = _preORBShutdown.elements();

	    while (elements.hasMoreElements())
	    {
		PreShutdown c = (PreShutdown) elements.nextElement();

		if (c != null)
		{
                    if (opLogger.logger.isTraceEnabled()) {
                        opLogger.logger.trace("ORB - pre-orb shutdown on " + c.name());
                    }

		    c.work();
		    c = null;
		}
	    }

	    //	    _preORBShutdown.clear();
	}

	if (_orb.initialised())
	    _orb.shutdown();

	if (!_postORBShutdown.isEmpty())
	{
	    Enumeration elements = _postORBShutdown.elements();

	    while (elements.hasMoreElements())
	    {
		PostShutdown c = (PostShutdown) elements.nextElement();

		if (c != null)
		{
                    if (opLogger.logger.isTraceEnabled()) {
                        opLogger.logger.trace("ORB - post-orb shutdown on " + c.name());
                    }

		    c.work();
		    c = null;
		}
	    }

	    //	    _postORBShutdown.clear();
	}
    }

    /**
     * Obtain a reference to the current ORB.
     */

public synchronized org.omg.CORBA.ORB orb ()
    {
	return _orb.orb();
    }

public synchronized boolean setOrb (org.omg.CORBA.ORB theORB)
    {
	if (!_orb.initialised())
	{
	    _orb.orb(theORB);

		/** Perform post-set operations configured for all ORBs **/
		new PostSetLoader(PostSetLoader.generateORBPropertyName(ORB_INITIALISER_NS), this);

		/**
		 * Perform post-set operations for this ORB only
		 */
		new PostSetLoader(PostSetLoader.generateORBPropertyName(ORB_INITIALISER_NS,_orbName), this);

	    return true;
	}
	else
	    return false;
    }

public synchronized void addPreShutdown (PreShutdown c)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::addPreShutdown (" + c + ")");
        }

	_preORBShutdown.put(c, c);
    }

public synchronized void addPostShutdown (PostShutdown c)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::addPostShutdown (" + c + ")");
        }

	_postORBShutdown.put(c, c);
    }

public synchronized void destroy() throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::destroyORB ()");
        }

	_orb.destroy();
    }

    protected ORB (String orbName)
    {
        _orbName = orbName;
    }

private void loadProperties (Properties p)
    {
        /**
         * If properties were passed in and the map contains data
         */
        if ( (p != null) && (!p.isEmpty()) )
        {
            /**
             * For each property passed in the initialiser only set those which
             * are intended for post or pre initialisation routines
             */
            Enumeration properties = p.keys();
            while (properties.hasMoreElements())
            {
                String o = (String) properties.nextElement();

                if ( PreInitLoader.isPreInitProperty(o) || PostInitLoader.isPostInitProperty(o) )
                {
                    if ( opLogger.logger.isTraceEnabled() ) {
                        opLogger.logger.trace("Adding property '" + o + "' to the ORB portability properties");
                    }

                    synchronized (ORB.class) {
                        Map<String, String> globalProperties = opPropertyManager.getOrbPortabilityEnvironmentBean().getOrbInitializationProperties();
                        globalProperties.put(o, p.getProperty(o));
                        opPropertyManager.getOrbPortabilityEnvironmentBean().setOrbInitializationProperties(globalProperties);
                    }
                }
            }
        }
    }

private void parseProperties (String[] params, boolean postInit)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("ORB::parseProperties (String[], " + postInit + ")");
        }

	Hashtable work = ((postInit) ? _postORBInitProperty : _preORBInitProperty);

	if (!work.isEmpty())
	{
	    Enumeration elements = work.elements();

	    while (elements.hasMoreElements())
	    {
		Attribute p = (Attribute) elements.nextElement();

		if (p != null)
		{
                    if (opLogger.logger.isTraceEnabled()) {
                        opLogger.logger.trace("Attribute " + p + " initialising.");
                    }

		    p.initialise(params);
		    p = null;
		}
	    }

	    //	    work.clear();
	}
    }

/**
 * Retrieve an ORB instance given a unique name, if an ORB instance with this name
 * doesn't exist then create it.
 *
 * @param uniqueId The name of the ORB instance to retrieve.
 * @return The ORB instance refered to by the name given.
 */
public synchronized static ORB getInstance(String uniqueId)
    {
    	/**
         * Try and find this ORB in the hashmap first if
         * its not there then create one and add it
         */
        ORB orb = (ORB)_orbMap.get(uniqueId);

        if (orb == null)
        {
            orb = new ORB(uniqueId);

            _orbMap.put(uniqueId, orb);
        }

        return(orb);
    }

String getName()
    {
        return(_orbName);
    }

com.arjuna.orbportability.orb.core.ORB _orb = new com.arjuna.orbportability.orb.core.ORB();

private Hashtable        _preORBShutdown = new Hashtable();
private Hashtable        _postORBShutdown = new Hashtable();
private Hashtable        _preORBInitProperty = new Hashtable();
private Hashtable        _postORBInitProperty = new Hashtable();

private String           _orbName = null;

private static HashMap	 _orbMap = new HashMap();

static final String ORB_INITIALISER_NS = "com.arjuna.orbportability.orb";

}
