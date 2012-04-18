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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JacOrbRCManager.java 2342 2006-03-30 13:06:17Z  $
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
import org.omg.PortableServer.POA;

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
                    jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_3();

                    rc = null;
                }
            }
        }

        catch (Exception ex) {
            jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_2(ex);
        }

        return rc;
    }

    public void destroy (RecoveryCoordinator rc) throws SystemException
    {
        // does nothing for JacORB
    }

    public void destroyAll (Object[] params) throws SystemException
    {
        // does nothing for JacORB
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
                        jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_4();
                }
                catch (java.io.FileNotFoundException ex) {
                    jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_4();
                }
                catch (Exception ex) {
                    jtsLogger.i18NLogger.warn_orbspecific_jacorb_recoverycoordinators_JacOrbRCManager_5(ex);
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
        String env = System.getProperty("com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators.noDaemon");
        String env2 = System.getProperty("com.arjuna.ats.internal.jts.orbspecific.jacorb.recoverycoordinators.noDaemon");

        _runWithoutDaemon = (env != null && env.equals("YES")) || (env2 != null && env2.equals("YES"));
    }

}
