package com.arjuna.xts.nightout.services.recovery;

import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.at.XTSATRecoveryManager;
import com.arjuna.wst.Durable2PCParticipant;

import java.io.ObjectInputStream;

/**
 * Application-specific WS-AT participant recovery manager for demo application, This class
 * is responsible for recreating application-specific durable participants from records
 * logged at prepare time.
 */
public class DemoRPCATRecoveryModule implements XTSATRecoveryModule
{
    /**
     * the singleton recovery module
     */
    private static DemoRPCATRecoveryModule theRecoveryModule = null;

    /**
     * a count of how many xts demo services are currently installed
     */
    private static int serviceCount = 0;

    /**
     * called during deployment of an xts-demo web service to ensure the recovery module for the
     * demo is installed whenever any of the services is active
     */
    public static void register()
    {
        if (theRecoveryModule == null) {
            theRecoveryModule = new DemoRPCATRecoveryModule();
        }
        if (serviceCount == 0) {
            XTSATRecoveryManager.getRecoveryManager().registerRecoveryModule(theRecoveryModule);
        }
        serviceCount++;
    }

    /**
     * called during undeployment of an xts-demo web service to ensure the recovery module for
     * the demo is deinstalled once none of the services is active
     */
    public static void unregister()
    {
        if (serviceCount > 0) {
            serviceCount--;
            if (serviceCount == 0) {
                XTSATRecoveryManager.getRecoveryManager().unregisterRecoveryModule(theRecoveryModule);
            }
        }
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and recreate the participant by deserializing
     * it from the supplied object input stream. n.b. this is only appropriate in case the
     * participant was originally saved using serialization.
     *
     * @param id     the id used when the participant was created
     * @param stream a stream from which the application should deserialise the participant
     *               if it recognises that the id belongs to the module's application
     * @return
     * @throws Exception if an error occurs deserializing the durable participant
     */
    public Durable2PCParticipant deserialize(String id, ObjectInputStream stream) throws Exception {
        if (id.startsWith("org.jboss.jbossts.xts-demorpc:restaurantAT") ||
                id.startsWith("org.jboss.jbossts.xts-demorpc:theatreAT") ||
                id.startsWith("org.jboss.jbossts.xts-demorpc:taxiAT")) {
            System.out.println("xts-demorpc : attempting to deserialize WS-AT participant " + id);
            Durable2PCParticipant participant = (Durable2PCParticipant)stream.readObject();
            System.out.println("xts-demorpc : deserialized WS-AT participant " + id);
            return participant;
        }

        return null;
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and use the saved recovery state to recreate the
     * participant. n.b. this is only appropriate in case the participant was originally saved
     * after being converted to a byte array using the PersistibleATParticipant interface.
     *
     * @param id            the id used when the participant was created
     * @param recoveryState a byte array returned form the original participant via a call to
     *                      method getRecoveryState of interface PersistableATParticipant
     * @return
     * @throws Exception if an error occurs converting the recoveryState back to a
     *                   durable participant
     */
    public Durable2PCParticipant recreate(String id, byte[] recoveryState) throws Exception {
        if (id.startsWith("org.jboss.jbossts.xts-demorpc:restauarantAT") ||
                id.startsWith("org.jboss.jbossts.xts-demorpc:theatreAT") ||
                id.startsWith("org.jboss.jbossts.xts-demorpc:taxiAT")) {
            // this should not get called -- xts-demo WS-AT participants are saved and restored
            // using serialization
            throw new Exception("xts-demorpc : invalid request to recreate() WS-AT participant " + id);
        }
        return null;
    }

    public void endScan()
    {
    }
}