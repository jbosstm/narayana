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

import java.util.Properties;

import org.omg.CORBA.Policy;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosTransactions.RecoveryCoordinatorHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.objectstore.TxLog;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jts.Implementations;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCreator;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.RecoveryServiceInit;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.RootOA;

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
    private static POA getRCPOA ()
    {
        String rcServiceName = GenericRecoveryCreator.getRecCoordServiceName();

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("JacOrbRCServiceInit.getRCPOA " + rcServiceName);
        }

        if (_poa == null)
        {
            String domainName = "recovery_coordinator";
            String poaName = POA_NAME_PREFIX + rcServiceName+domainName;

            try
            {
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

    private static void initORBandOA() throws InvalidName
    {
        if ( !ORBManager.isInitialised() )
        {
            // If the ORB Manager hasn't been initialised then create our own ORB

            _orb = com.arjuna.orbportability.ORB.getInstance("RecoveryServer");

            String recoveryManagerPort = ""+ jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerPort();
            String recoveryManagerAddr = jtsPropertyManager.getJTSEnvironmentBean().getRecoveryManagerAddress();

            jtsLogger.i18NLogger.info_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_6(recoveryManagerPort, recoveryManagerAddr);

            final Properties p = new Properties();
            p.setProperty("OAPort", recoveryManagerPort);

            if (recoveryManagerAddr != null && recoveryManagerAddr.length() > 0)
            {
                p.setProperty("OAIAddr", recoveryManagerAddr);
            }

            try {
                _orb.initORB((String[])null, p);
                _oa = OA.getRootOA(_orb);
                _oa.initOA();
            } catch(RuntimeException e) {
                ORBManager.reset();
                _orb.shutdown();
                throw e;
            }

            ORBManager.setORB(_orb);
            ORBManager.setPOA(_oa);
        }
        else
        {
            // Otherwise use the ORB already registered with the ORB Manager

            _orb = ORBManager.getORB();
            _oa = (RootOA) ORBManager.getPOA();

            jtsLogger.i18NLogger.info_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_6a();
        }
        ORBManager.getPOA().addPreShutdown(new JacOrbRCShutdown());
    }

    /**
     * This starts the service in the RecoveryManager.
     */

    public  boolean startRCservice ()
    {
        try
            {
                initORBandOA();

                POA ourPOA = getRCPOA();

                if (ourPOA == null)  // shortcut
                    return false;

                Implementations.initialise();


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
                        OutputObjectState oState = new OutputObjectState();
                        oState.packString(reference);

                        TxLog txLog = StoreManager.getCommunicationStore();
                        txLog.write_committed( new Uid(uid4Recovery), type(), oState);
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
                _runOA = new ORBRunner();

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

    static protected String uid4Recovery = "0:ffff52e38d0c:c91:4140398c:0";

    private static Object orbRunnerLock = new Object();
    private static ORBRunner _runOA;

    public static void waitForRunningORBRunner() {
        synchronized (orbRunnerLock) {
            if (_runOA != null) {
                try {
                    orbRunnerLock.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static void orbRunnerCompleted() {
        synchronized (orbRunnerLock) {
            _runOA = null;
            orbRunnerLock.notify();
        }
    }

}