package com.jboss.jbosstm.xts.demo.services.recovery;

import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryModule;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;

import java.io.ObjectInputStream;

/**
 * Application-specific WS-AT participant recovery manager for demo application, This class
 * is responsible for recreating application-specific durable participants from records
 * logged at prepare time.
 */
public class DemoBARecoveryModule implements XTSBARecoveryModule
{
    /**
     * the singleton recovery module
     */
    private static DemoBARecoveryModule theRecoveryModule = null;

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
            theRecoveryModule = new DemoBARecoveryModule();
        }
        if (serviceCount == 0) {
            XTSBARecoveryManager.getRecoveryManager().registerRecoveryModule(theRecoveryModule);
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
                XTSBARecoveryManager.getRecoveryManager().unregisterRecoveryModule(theRecoveryModule);
            }
        }
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and recreate the participant by deserializing
     * it from the supplied object input stream. n.b. this is only appropriate in case the
     * original was a ParticipantCompletion participant saved using serialization.
     *
     * @param id     the id used when the participant was created
     * @param stream a stream from which the application should deserialise the participant
     *               if it recognises that the id belongs to the module's application
     * @return the deserialized ParticipantCompletion participant
     * @throws Exception if an error occurs deserializing the ParticipantCompletion participant
     */
    public BusinessAgreementWithParticipantCompletionParticipant deserializeParticipantCompletionParticipant(String id, ObjectInputStream stream) throws Exception
    {
        if (id.startsWith("org.jboss.jbossts.xts-demo:restaurantBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:theatreBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:taxiBA")) {
            System.out.println("xts-demo : attempting to deserialize WS-BA participant " + id);
            BusinessAgreementWithParticipantCompletionParticipant participant = (BusinessAgreementWithParticipantCompletionParticipant)stream.readObject();
            System.out.println("xts-demo : deserialized WS-BA participant " + id);
            return participant;
        }
        return null;
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and use the saved recovery state to recreate the
     * participant. n.b. this is only appropriate in case the original was a ParticipantCompletion
     * participant saved after being converted to a byte array using the PersistibleBAParticipant
     * interface.
     *
     * @param id            the id used when the participant was created
     * @param recoveryState a byte array returned form the original participant via a call to
     *                      method getRecoveryState of interface PersistableBAParticipant
     * @return the recreated ParticipantCompletion participant
     * @throws Exception if an error occurs converting the recoveryState back to a
     *                   ParticipantCompletion participant
     */
    public BusinessAgreementWithParticipantCompletionParticipant recreateParticipantCompletionParticipant(String id, byte[] recoveryState) throws Exception
        {
        if (id.startsWith("org.jboss.jbossts.xts-demo:restauarantBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:theatreBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:taxiBA")) {
            // this should not get called -- xts-demo WS-BA participants are saved and restored
            // using serialization
            throw new Exception("xts-demo : invalid request to recreate() WS-BA participant " + id);
        }
        return null;
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and recreate the participant by deserializing
     * it from the supplied object input stream. n.b. this is only appropriate in case the
     * original was a CoordinatorCompletion participant saved using serialization.
     *
     * @param id     the id used when the participant was created
     * @param stream a stream from which the application should deserialise the participant
     *               if it recognises that the id belongs to the module's application
     * @return the deserialized ParticipantCompletion participant
     * @throws Exception if an error occurs deserializing the CoordinatorCompletion participant
     */
    public BusinessAgreementWithCoordinatorCompletionParticipant deserializeCoordinatorCompletionParticipant(String id, ObjectInputStream stream) throws Exception {
        if (id.startsWith("org.jboss.jbossts.xts-demo:restauarantBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:theatreBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:taxiBA")) {
            // this should not get called -- xts-demo WS-BA participants employ the ParticipantCompletion protocol
            throw new Exception("xts-demo : invalid request to deserialize WS-BA CoordinatorCompletion participant " + id);
        }
        return null;
    }

    /**
     * called during recovery processing to allow an application to identify a participant id
     * belonging to one of its participants and use the saved recovery state to recreate the
     * participant. n.b. this is only appropriate in case the original was a CoordinatorCompletion
     * participant saved after being converted to a byte array using the PersistibleBAParticipant
     * interface.
     *
     * @param id            the id used when the participant was created
     * @param recoveryState a byte array returned form the original participant via a call to
     *                      method getRecoveryState of interface PersistableBAParticipant
     * @return the recreated ParticipantCompletion participant
     * @throws Exception if an error occurs converting the recoveryState back to a
     *                   CoordinatorCompletion participant
     */
    public BusinessAgreementWithCoordinatorCompletionParticipant recreateCoordinatorCompletionParticipant(String id, byte[] recoveryState) throws Exception {
        if (id.startsWith("org.jboss.jbossts.xts-demo:restauarantBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:theatreBA") ||
                id.startsWith("org.jboss.jbossts.xts-demo:taxiBA")) {
            // this should not get called -- xts-demo WS-BA participants are saved and restored
            // using serialization
            throw new Exception("xts-demo : invalid request to recreate() WS-BA participant " + id);
        }
        return null;
    }
}