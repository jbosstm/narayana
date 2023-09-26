/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators.GenericRecoveryCoordinator;
import com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators.RecoveryCoordinatorId;
import com.arjuna.ats.jts.logging.jtsLogger;
import java.nio.charset.StandardCharsets;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.Resource;
import org.omg.CosTransactions.Status;


public class JavaIdlRCDefaultServant extends GenericRecoveryCoordinator
{
    private ORB _ourOrb;

    static byte[] RCObjectId = null;

    /**
     * constructor supplies orb - used only within package
     * @param orb which orb to use
     */
    JavaIdlRCDefaultServant(ORB orb)
    {
        super();    // ensure id is null
        _ourOrb = orb;

        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("JavaIdlRCDefaultServant(orb)");
        }

    }

    public Status replay_completion ( Resource res ) throws SystemException, NotPrepared
    {
        if (jtsLogger.logger.isDebugEnabled()) {
            jtsLogger.logger.debug("JavaIdlRCDefaultServant::replay_completion)");
        }

        try
        {
            //Begin New
            org.omg.CORBA.Object obj = _ourOrb.resolve_initial_references("POACurrent");
            org.omg.PortableServer.Current poa_current = org.omg.PortableServer.CurrentHelper.narrow(obj);
            byte[] objectId = poa_current.get_object_id();
            //End New

            String objectIdString = new String(objectId, StandardCharsets.UTF_8);
            String poaName = poa_current.get_POA().the_name();

            if (objectIdString.startsWith(poaName)) {
                // strip off the POA name prefix from the object name - the remainder encodes our Uids
                int index = poaName.length();

                if (objectIdString.length() > index)
                    index += 1;

                objectIdString = objectIdString.substring(index);
            }

            // convert that to the structured id
            RecoveryCoordinatorId  recovCoId = RecoveryCoordinatorId.reconstruct(objectIdString);

            if (jtsLogger.logger.isDebugEnabled()) {
                jtsLogger.logger.debug("JavaIdlDefaultServant replay_completion for Id "+recovCoId);
            }

            // and do the real replay
            return GenericRecoveryCoordinator.replay_completion(recovCoId, res);
        }
        /***/
        catch (NotPrepared exp)
        {
            throw exp;
        }
        /**/
        catch (Exception ex) {
            jtsLogger.i18NLogger.warn_orbspecific_recoverycoordinators_RCDefaultServant_3(this.getClass().getName(), ex);

            return Status.StatusUnknown;
        }
    }
}