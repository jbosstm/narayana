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

package com.arjuna.ats.internal.jts.orbspecific.ibmorb.recoverycoordinators;

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
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Policy;
import org.omg.CosTransactions.RecoveryCoordinatorHelper;
import org.omg.PortableServer.*;

import java.util.Properties;

/**
 * Initialises ibmorb ORB RecoveryCoordinator creation subsystem
 * and provides the ibmorb specific implementations of stuff
 *
 * All orbs are likely to be the same, constructing a GenericRecoveryCreator,
 * but with an orb-specific manager
 *
 */

public class JavaIdlRCServiceInit implements RecoveryServiceInit
{

    public JavaIdlRCServiceInit()
    {
    }

    /**
     * Provide the POA for the RecoveryCoordinator.
     * Construct with the policies appropriate for its use in the RecoveryManager
     */
    private static POA getRCPOA ()
    {
        String rcServiceName = GenericRecoveryCreator.getRecCoordServiceName();

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("JavaIdlRCServiceInit.getRCPOA " + rcServiceName);
        }

        if (_poa == null)
        {
            String domainName = "recovery_coordinator";
            String poaName = POA_NAME_PREFIX + rcServiceName+domainName;

            try
            {
                POA rootPOA = _oa.rootPoa();

                if (rootPOA == null) {
                    jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_8();

                    return null;
                }

                // create direct persistent POA
                // make the policy lists, with standard policies
                Policy[] policies;

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
            p.setProperty("com.sun.CORBA.POA.ORBPersistentServerPort", recoveryManagerPort);
            p.setProperty("com.sun.CORBA.POA.ORBServerId", recoveryManagerPort);

            if (recoveryManagerAddr != null && recoveryManagerAddr.length() > 0)
            {
                p.setProperty("OAIAddr", recoveryManagerAddr);
                // TODO what is the JAVA Idl equivalent
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
                JavaIdlRCDefaultServant theButler = new JavaIdlRCDefaultServant(_orb.orb());

                // register it on the POA
                ourPOA.set_servant(theButler);

//                ourPOA.the_POAManager().activate( );

                org.omg.CORBA.Object obj = ourPOA.create_reference_with_id(RC_ID.getBytes(),
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
                catch ( SecurityException sex )
                {
                    jtsLogger.i18NLogger.fatal_orbspecific_jacorb_recoverycoordinators_JacOrbRCServiceInit_5();
                }

                if (jtsLogger.logger.isDebugEnabled()) {
                    jtsLogger.logger.debug("JavaIdlRCServiceInit - set default servant and activated");
                }

                // activate the poa

                _oa.rootPoa().the_POAManager().activate();

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

    private static String initRCKey() {
        String domainName = "recovery_coordinator";
        String poaName = POA_NAME_PREFIX + GenericRecoveryCreator.getRecCoordServiceName() + domainName;
        return OBJ_KEY_PREFIX + poaName + '/' + RC_ID;
    }

    private static final String POA_NAME_PREFIX = "RcvCo-";
    private static String OBJ_KEY_PREFIX = ""; // is there an equivalent of JacORBs jacorb.implname
    protected static POA                _poa = null;
    static String RC_ID = "RecoveryManager";

    static String RC_KEY = initRCKey();


    protected static com.arjuna.orbportability.ORB _orb = null;
    protected static RootOA _oa = null;

    static protected String uid4Recovery = "0:ffff52e38d0c:c91:4140398c:0";

}
