/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */




package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators.GenericRecoveryCoordinator;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.RcvCoManager;
import com.arjuna.ats.jts.logging.jtsLogger;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.RecoveryCoordinatorHelper;

/**
 * Implementation of RecoveryCreator for JavaIdl orb
 * Handles the creation of RecoveryCoordinator objects for
 * JavaIdl orb.  The RCs are created locally but also will be
 * recreated in the RecoveryManager if necessary following a crash
 * of this process.
 */

public class JavaIdlRCManager implements RcvCoManager
{

    /**
     * The repository id for RecoveryCoordinator
     */
    private static final String rcvcoRepositoryId = RecoveryCoordinatorHelper.id();

    public JavaIdlRCManager()
    {
    }

    /**
     * We create a RecoveryCoordinator reference, but without (we think)
     * actually making the implementation object available to the orb.
     * The data needed to construct the RecoveryCoordinator is put in
     * the ObjectId. If a replay_completion is received, it will be sent,
     * via the locationd daemon, to the RecoveryManager.
     */

    public RecoveryCoordinator makeRC( Uid RCUid, Uid tranUid,
                                       Uid processUid,
                                       boolean isServerTransaction )
    {
        initialise();

        RecoveryCoordinator rc = null;

        // mangle those parameters to the string key (object id sort of thing)

        try
        {
            String rcObjectId = GenericRecoveryCoordinator.makeId(RCUid, tranUid, processUid, isServerTransaction);

            if (ref_ReCoo != null)
            {
                // New for IOR template
                String new_ior = RecoverIOR.getIORFromString(ORBManager.getORB().orb(), ref_ReCoo, rcObjectId);
                org.omg.CORBA.Object rcAsObject = ORBManager.getORB().orb().string_to_object(new_ior);
                //End for IOR Template

                rc = RecoveryCoordinatorHelper.narrow(rcAsObject);

                if (jtsLogger.logger.isDebugEnabled()) {
                    jtsLogger.logger.debug("JavaIdlRCManager: Created reference for tran "+tranUid+" = "+rc);
                }
            }
            else
            {
                if (JavaIdlRCManager._runWithoutDaemon)
                    throw new NO_IMPLEMENT();
                else {
                    jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_RCManager_3();

                    rc = null;
                }
            }
        }

        catch (Exception ex) {
            jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_RCManager_2(ex);
        }

        return rc;
    }

    public void destroy (RecoveryCoordinator rc) throws SystemException
    {
    }

    public void destroyAll (Object[] params) throws SystemException
    {
    }

    private synchronized void initialise ()
    {
        if (!_initialised)
        {
            _initialised = true;

            if (!_runWithoutDaemon)
            {
                try
                {
                    ParticipantStore participantStore = StoreManager.getCommunicationStore();
                    InputObjectState iState = participantStore.read_committed(new Uid( JavaIdlRCServiceInit.uid4Recovery), JavaIdlRCServiceInit.type());

                    if (iState != null)
                        ref_ReCoo = iState.unpackString();
                    else
                        jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_RCManager_4();
                }
                catch (java.io.FileNotFoundException ex) {
                    jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_RCManager_4();
                }
                catch (Exception ex) {
                    jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_RCManager_5(ex);
                }
            }
        }
    }

    static protected String ref_ReCoo = null;

    private static boolean _runWithoutDaemon = false;
    private static boolean _initialised = false;



    static
    {
        /*
       * Undocumented "feature" that lets us run tests without having
       * to start the recovery daemon. In general we don't want people
       * doing that kind of thing, but it makes development testing a
       * lot easier.
       *
       * Note: this relies directly on system property lookup, since we don't
       * want to expose the setting via the public EnvironmentBean config.
       */
        String idl_noDaemon = System.getProperty("com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators.noDaemon");

        _runWithoutDaemon = (idl_noDaemon != null && idl_noDaemon.equals("YES"));
    }

}