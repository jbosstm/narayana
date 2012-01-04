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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: OA.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.orbportability;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.omg.CORBA.Policy;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;

import com.arjuna.orbportability.exceptions.FatalError;
import com.arjuna.orbportability.internal.utils.PostInitLoader;
import com.arjuna.orbportability.internal.utils.PostSetLoader;
import com.arjuna.orbportability.internal.utils.PreInitLoader;
import com.arjuna.orbportability.logging.opLogger;
import com.arjuna.orbportability.oa.Attribute;
import com.arjuna.orbportability.oa.PostShutdown;
import com.arjuna.orbportability.oa.PreShutdown;

/**
 * An attempt at some ORB portable ways of interacting with the OA. NOTE:
 * initPOA *must* be called if you want to use the pre- and post- initialisation
 * mechanisms.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: OA.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 2.1.
 */

public abstract class OA
{

    /**
     * Ensure that all OA specific initialisation is done even if the programmer
     * uses the OA specific init routines. This method does not need to be
     * called if using initOA methods.
     * 
     * @since JTS 2.1.1.
     */

    public synchronized void init () throws SystemException
    {
        // null op - just to ensure we create the OA object!
    }

    public synchronized void initPOA () throws InvalidName, SystemException
    {
        initPOA(null);
    }

    public ORB getAssociatedORB ()
    {
        return _associatedORB;
    }

    public void setPOA (org.omg.PortableServer.POA p) throws SystemException
    {
        if (!_oa.initialised())
        {
            _oa.rootPoa(p);

            performPostSet(this._oaName);
        }
    }

    /**
     * Loads and runs the configured pre-initialisation classes
     */

    protected void performPreInit (String oaName)
    {
        /**
         * Perform pre-initialisation classes for all OAs
         */
        PreInitLoader preInit = new PreInitLoader(PreInitLoader
                .generateOAPropertyName(ORB.ORB_INITIALISER_NS), this);

        /**
         * Perform pre-initialisation classes for all OAs
         */
        preInit = new PreInitLoader(PreInitLoader.generateOAPropertyName(
                ORB.ORB_INITIALISER_NS, _associatedORB.getName()), this);

        /**
         * Perform pre-initialisation classes for this OA only
         */
        preInit = new PreInitLoader(PreInitLoader.generateOAPropertyName(
                ORB.ORB_INITIALISER_NS, _associatedORB.getName(), oaName), this);
        preInit = null;
    }

    protected void performPostSet (String oaName)
    {
        /**
         * Perform post-set operations for all OAs
         */
        new PostSetLoader(PostSetLoader
                .generateOAPropertyName(ORB.ORB_INITIALISER_NS), this);

        /**
         * Perform post-set operations for all OAs
         */
        new PostSetLoader(PostSetLoader.generateOAPropertyName(
                ORB.ORB_INITIALISER_NS, _associatedORB.getName()), this);

        /**
         * Perform post-set operations for this OA only
         */
        new PreInitLoader(PostSetLoader.generateOAPropertyName(
                ORB.ORB_INITIALISER_NS, _associatedORB.getName(), oaName), this);
    }

    /**
     * Loads and runs the configured post-initialisation classes
     */

    protected void performPostInit (String oaName)
    {
        /**
         * Perform post-initialisation classes for all OAs
         */
        PostInitLoader postInit = new PostInitLoader(PostInitLoader
                .generateOAPropertyName(ORB.ORB_INITIALISER_NS), this);

        /**
         * Perform post-initialisation classes for all OAs
         */
        postInit = new PostInitLoader(PostInitLoader.generateOAPropertyName(
                ORB.ORB_INITIALISER_NS, _associatedORB.getName()), this);

        /**
         * Perform post-initialisation classes for this OA only
         */
        postInit = new PostInitLoader(PostInitLoader.generateOAPropertyName(
                ORB.ORB_INITIALISER_NS, _associatedORB.getName(), oaName), this);
        postInit = null;
    }

    public synchronized void initPOA (String[] args) throws InvalidName,
            SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::initPOA (String[])");
        }

        if (!_oa.initialised())
        {
            if (_associatedORB._orb.initialised())
            {
                performPreInit(_oaName);

                parseProperties(args, true);

                try
                {
                    _oa.init(); // create the root poa
                }
                catch (final org.omg.CORBA.INITIALIZE ex)
                {
                    opLogger.i18NLogger.warn_OA_initialize();
                    
                    throw ex;
                }
                catch (final Exception e)
                {
                    opLogger.i18NLogger.warn_OA_caughtexception("OA.initPOA", e);

                   throw new FatalError("OA.initPOA: "+e.toString());
                }

                parseProperties(args, false);

                performPostInit(_oaName);
            }
            else
            {
                throw new FatalError( opLogger.i18NLogger.get_OA_uninitialsedorb() );
            }
        }
    }

    public synchronized ChildOA createPOA (String adapterName, Policy[] policies)
            throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::createPOA ("
                    + adapterName + " )");
        }

        if (!_oa.initialised())
        {
            opLogger.i18NLogger.warn_OA_oanotinitialised();

            throw new AdapterInactive();
        }

        if (_defaultAdapterName == null)
            _defaultAdapterName = adapterName;

        /**
         * Perform OA pre-initialisation routines
         */
        performPreInit(adapterName);

        /**
         * Create a child POA of this POA passing the policies passed in
         */
        _oa.createPOA(adapterName, policies);
        ChildOA newChildOA = new ChildOA(_associatedORB, adapterName, _oa
                .poa(adapterName));

        /**
         * Perform OA post-initialisation routines
         */
        performPostInit(adapterName);

        return (newChildOA);
    }

    public void initOA () throws InvalidName, SystemException
    {
        initOA(null);
    }

    public void initOA (String[] args) throws InvalidName, SystemException
    {
        initPOA(args);
    }

    public synchronized boolean addAttribute (Attribute p)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::addAttribute (" + p
                    + ")");
        }

        if ((_oa.initialised()) || (p == null)) // oa already set up!
            return false;

        if (p.postOAInit())
            _postOAInitProperty.put(p, p);
        else
            _preOAInitProperty.put(p, p);

        return true;
    }

    public abstract void destroy () throws SystemException;

    public synchronized POA rootPoa ()
    {
        return _oa.rootPoa();
    }

    public synchronized POA poa (String adapterName)
    {
        return _oa.poa(adapterName);
    }

    public synchronized boolean setPoa (String adapterName, POA thePOA)
    {
        if (adapterName != null)
        {
            _oa.poa(adapterName, thePOA);

            return true;
        }
        else
            return false;
    }

    public synchronized void addPreShutdown (PreShutdown c)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::addPreShutdown ("
                    + c + ")");
        }

        _preOAShutdown.put(c, c);
    }

    public synchronized void addPostShutdown (PostShutdown c)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::addPostShutdown ("
                    + c + ")");
        }

        _postOAShutdown.put(c, c);
    }

    /**
     * @return a CORBA object reference for the Servant/Implementation.
     */

    public abstract org.omg.CORBA.Object corbaReference (Servant obj);

    public org.omg.CORBA.Object corbaReference (Servant obj, POA poa)
    {
        try
        {
            return poa.servant_to_reference(obj);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Register the object with the ORB.
     */
    public abstract boolean objectIsReady (Servant obj, byte[] id)
            throws SystemException;

    public abstract boolean objectIsReady (Servant obj) throws SystemException;

    /**
     * Dispose of the object, i.e., unregister it from the ORB.
     */
    public abstract boolean shutdownObject (org.omg.CORBA.Object obj);

    public abstract boolean shutdownObject (Servant obj);

    /**
     * Place the server into a state where it can begin to accept requests for
     * objects from clients.
     */

    public void run (String name) throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::run (" + name + ")");
        }

        _oa.run(name);
    }

    public void run () throws SystemException
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::run ()");
        }

        _oa.run();
    }

    private final void parseProperties (String[] params, boolean preInit)
    {
        if (opLogger.logger.isTraceEnabled()) {
            opLogger.logger.trace("OA::parseProperties (String[], " + preInit + ")");
        }

        Hashtable work = ((preInit) ? _preOAInitProperty : _postOAInitProperty);

        if (!work.isEmpty())
        {
            Enumeration elements = work.elements();

            while (elements.hasMoreElements())
            {
                Attribute p = (Attribute) elements.nextElement();

                if (p != null)
                {
                    if (opLogger.logger.isTraceEnabled()) {
                        opLogger.logger.trace("Attribute "
                                + p + " initialising.");
                    }

                    p.initialise(params);
                    p = null;
                }
            }

            // work.clear();
        }
    }

    OA(com.arjuna.orbportability.ORB orb, String oaName)
    {
        _associatedORB = orb;
        _oaName = oaName;
        _oa = new com.arjuna.orbportability.oa.core.OA(_associatedORB._orb);
    }

    /**
     * OA constructor - creates an OA which represents a given POA on a given
     * ORB
     * 
     * @param orb
     *            The ORB this OA exists in
     * @param oa
     *            The OA this class will wrap around
     */
    OA(com.arjuna.orbportability.ORB orb, String oaName, POA oa)
    {
        _associatedORB = orb;
        _oaName = oaName;

        /**
         * Create OA class and associate it with the given ORB
         */
        _oa = new com.arjuna.orbportability.oa.core.OA(_associatedORB._orb);

        /**
         * Set the Root OA for this OA class to the given OA
         */
        _oa.rootPoa(oa);
    }

    /**
     * Retrieve an OA instance given a unique name, if an OA instance with this
     * name doesn't exist then create it.
     * 
     * @param associatedORB
     *            The ORB this OA is being created for.
     * @return The OA instance referred to by the name given.
     */
    public synchronized static RootOA getRootOA (ORB associatedORB)
    {

        /**
         * Get the OA for this ORB instance
         */
        RootOA oa = (RootOA) _orbToOAMap.get(associatedORB);

        if (oa == null)
        {
            oa = new com.arjuna.orbportability.RootOA(associatedORB);

            _orbToOAMap.put(associatedORB, oa);
        }

        return (oa);
    }

    com.arjuna.orbportability.oa.core.OA _oa = null;

    com.arjuna.orbportability.ORB _associatedORB = null;

    protected String _defaultAdapterName = null;

    protected String _oaName = null;

    protected Hashtable _preOAShutdown = new Hashtable();

    protected Hashtable _postOAShutdown = new Hashtable();

    protected Hashtable _preOAInitProperty = new Hashtable();

    protected Hashtable _postOAInitProperty = new Hashtable();

    private static HashMap _orbToOAMap = new HashMap();
}
