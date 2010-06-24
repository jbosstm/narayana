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
 * Copyright (C) 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JacOrbRCServiceInit.java 2342 2006-03-30 13:06:17Z  $
 *
 */

package com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.*;

import com.arjuna.ats.jts.logging.*;
import com.arjuna.ats.internal.jts.Implementations;
import com.arjuna.ats.internal.jts.ORBManager;


import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.internal.jts.recovery.RecoveryORBManager;

import com.arjuna.orbportability.*;

import java.io.InputStream;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosTransactions.*;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.state.*;

import java.io.IOException;
import java.util.Properties;

/**
 * Initialises JacORB RecoveryCoordinator creation subsystem
 * and provides the JacORB-specific implementations of stuff
 *
 * All orbs are likely to be the same, constructing a GenericRecoveryCreator,
 * but with an orb-specific manager
 *
 */

public class JacOrbRCServiceInit implements RecoveryServiceInit
{

    public JacOrbRCServiceInit()
    {
    }

    /**
     * Provide the POA for the recoverycoordinator.
     * Construct with the policies appropriate for its use in the RecoveryManager,
     * but the policies are usable by the JacOrbRCManager to create the IOR's in
     * TS-using processes.
     */

    static POA getRCPOA (String domainName)
    {
        String rcServiceName = GenericRecoveryCreator.getRecCoordServiceName();
        
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("JacOrbRCServiceInit.getRCPOA " + rcServiceName);
        }

        if (_poa == null)
        {
            String poaName = POA_NAME_PREFIX + rcServiceName+domainName;
            boolean oaInit = true;
            String oaPort = "OAPort";
        String oaAddr = "OAIAddr";
        String oldPort = System.getProperty(oaPort, "");
        String oldAddr = System.getProperty(oaAddr, "");

        /** If the ORB Manager hasn't been initialised then create our own ORB **/

            if ( !RecoveryORBManager.isInitialised() )
            {
                _orb = com.arjuna.orbportability.internal.InternalORB.getInstance("RecoveryServer");
                String[] params = null;
                String recoveryManagerPort = ""+jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort();
                String recoveryManagerAddr = jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerAddress();

                if (recoveryManagerAddr == null)
                    recoveryManagerAddr = "";

                jtsLogger.i18NLogger.info_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_6(recoveryManagerPort, recoveryManagerAddr);

                final Properties p = new Properties();
                // Try to preload jacorb.properties
                final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader() ;
                if (contextClassLoader != null)
                {
                    final InputStream is = contextClassLoader.getResourceAsStream("jacorb.properties") ;
                    if (is != null)
                    {
                        try
                        {
                            p.load(is) ;
                        }
                        catch (final IOException ioe)
                        {
                            p.clear() ;
                        }
                    }
                }
                p.setProperty(oaPort, recoveryManagerPort);

                if (recoveryManagerAddr.length() != 0)
                {
                    p.setProperty(oaAddr, recoveryManagerAddr);
                    System.setProperty(oaAddr, oldAddr);
                }

                _orb.initORB(params, p);
                _oa = OA.getRootOA(_orb);

                if (oldPort == null)
                    oldPort = "";

                System.setProperty(oaPort, oldPort);    // Remove property that JacORB added so future ORB's work.

                RecoveryORBManager.setORB(_orb);
                RecoveryORBManager.setPOA(_oa);
            }
            else
            {
                /** Otherwise use the ORB already registered with the ORB Manager **/
                _orb = RecoveryORBManager.getORB();
                _oa = (RootOA) RecoveryORBManager.getPOA();
                
                oaInit = false;

                jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_6a(oldPort);
            }

            try
            {
                /*
                 * Only initialise the object adapter if we created it.
                 * Otherwise we assume the ORB and the POA have been
                 * initialised already.
                 */

                if (oaInit)
                    _oa.initOA();

                if (domainName.equals("recovery_coordinator") && !ORBManager.isInitialised())
                {
                    try
                    {
                        ORBManager.setORB(_orb);
                        ORBManager.setPOA(_oa);
                    }
                    catch (Exception ex) {
                        jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_7(ex);
                    }
                }

                org.omg.CORBA.ORB theORB = _orb.orb();
                org.omg.PortableServer.POA rootPOA = _oa.rootPoa();

                if (rootPOA == null) {
                    jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_8();

                    return null;
                }
                    
                // create direct persistent POA
                // make the policy lists, with standard policies
                org.omg.CORBA.Policy[] policies = null;

                policies = new Policy []
                {
                    rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT),
                    rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
                    rootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                    rootPOA.create_id_uniqueness_policy(IdUniquenessPolicyValue.MULTIPLE_ID),
                    rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_DEFAULT_SERVANT)
                };

                _poa = rootPOA.create_POA(poaName, rootPOA.the_POAManager(), policies);
            }
            catch (Exception ex) {
                jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_1(ex);
            }
        }

        return _poa;
    }

    /**
     * This starts the service in the RecoveryManager.
     */

    public  boolean startRCservice ()
    {
        POA ourPOA = getRCPOA("recovery_coordinator");

        if (ourPOA == null)  // shortcut
            return false;
        
        Implementations.initialise();
        
        try
            {
                // get the orb, so we can pass it to the default servant

                // make the default servant
                JacOrbRCDefaultServant theButler = new JacOrbRCDefaultServant(_orb.orb());

                // register it on the POA
                ourPOA.set_servant(theButler);

                org.omg.CORBA.Object obj = ourPOA.create_reference_with_id("RecoveryManager".getBytes(),
                                                                           RecoveryCoordinatorHelper.id());

                // Write the object reference in the file

                String reference = _orb.orb().object_to_string(obj);

                try
                    {
                        if (currentStore == null)
                        {
                            currentStore = TxControl.getStore();
                        }

                        OutputObjectState oState = new OutputObjectState();
                        oState.packString(reference);
                        currentStore.write_committed( new Uid(uid4Recovery), type(), oState);
                    }
                catch ( java.lang.SecurityException sex )
                {
                    jtsLogger.i18NLogger.fatal_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_5();
                }

                if (jtsLogger.logger.isDebugEnabled()) {
                    jtsLogger.logger.debug("JacOrbRCServiceInit - set default servant and activated");
                }

                // activate the poa

                _oa.rootPoa().the_POAManager().activate();

                //_oa.run();
                ORBRunner _runOA = new ORBRunner();

                return true;
            } catch (Exception ex) {
            jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_3(ex);
            return false;
        }

    }

    public static void shutdownRCService ()
    {
        _poa = null;
    }

    public static String type ()
    {
        return "/RecoveryCoordinator";
    }


    private static final String POA_NAME_PREFIX = "RcvCo-";

    protected static POA                _poa = null;

    protected static com.arjuna.orbportability.ORB _orb = null;
    protected static com.arjuna.orbportability.RootOA _oa = null;

    protected static String RecoveryIdStore = "RecoveryCoordinatorIdStore";
    protected static String RecoveryCoordStore = "RecoveryCoordinator";

    private static final String orbNamePrefix = "ots_";
    private static final String orbName = "arjuna.portable_interceptor.";

    private ObjectStore     currentStore;

    static protected String uid4Recovery = "0:ffff52e38d0c:c91:4140398c:0";

};