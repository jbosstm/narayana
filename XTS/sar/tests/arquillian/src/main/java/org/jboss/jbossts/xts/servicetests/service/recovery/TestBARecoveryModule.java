package org.jboss.jbossts.xts.servicetests.service.recovery;

import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.ba.XTSBARecoveryManager;
import org.jboss.jbossts.xts.servicetests.service.Constants;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;

import java.io.ObjectInputStream;

/**
 * Application-specific WS-AT participant recovery manager for service test application, This class
 * is responsible for recreating application-specific durable participants from records
 * logged at prepare time.
 */
public class TestBARecoveryModule implements XTSBARecoveryModule
{
    /**
     * the singleton recovery module
     */
    private static TestBARecoveryModule theRecoveryModule = null;

    /**
     * a count of how many xts demo services are currently installed
     */
    private static int serviceCount = 0;

    /**
     * called during deployment of a test service to ensure the recovery module for the
     * test is installed whenever any of the services is active
     */
    public static void register()
    {
        if (theRecoveryModule == null) {
            theRecoveryModule = new TestBARecoveryModule();
        }
        if (serviceCount == 0) {
            XTSBARecoveryManager.getRecoveryManager().registerRecoveryModule(theRecoveryModule);
        }
        serviceCount++;
    }

    /**
     * called during undeployment of a test service to ensure the recovery module for
     * the test is deinstalled once none of the services is active
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

    public BusinessAgreementWithParticipantCompletionParticipant deserializeParticipantCompletionParticipant(String id, ObjectInputStream stream) throws Exception {
        if (id.startsWith(Constants.PARTICIPANT_ID_PREFIX + "ParticipantCompletionParticipant")) {
            System.out.println("xts service test : attempting to deserialize WS-BA participant completion participant " + id);
            BusinessAgreementWithParticipantCompletionParticipant participant
                    = (BusinessAgreementWithParticipantCompletionParticipant)stream.readObject();
            System.out.println("xts service test : deserialized WS-BA participant completion participant " + id);
            return participant;
        }

        return null;
    }

    public BusinessAgreementWithParticipantCompletionParticipant recreateParticipantCompletionParticipant(String id, byte[] recoveryState) throws Exception {
        if (id.startsWith(Constants.PARTICIPANT_ID_PREFIX + "ParticipantCompletionParticipant")) {
            // this should not get called -- test WS-BA participants are saved and restored
            // using serialization
            throw new Exception("xts service test : invalid request to recreate() WS-BA participant completion participant " + id);
        }

        return null;
    }

    public BusinessAgreementWithCoordinatorCompletionParticipant deserializeCoordinatorCompletionParticipant(String id, ObjectInputStream stream) throws Exception {
        if (id.startsWith(Constants.PARTICIPANT_ID_PREFIX + "CoordinatorCompletionParticipant")) {
            System.out.println("xts service test : attempting to deserialize WS-BA coordinator completion participant " + id);
            BusinessAgreementWithCoordinatorCompletionParticipant participant
                    = (BusinessAgreementWithCoordinatorCompletionParticipant)stream.readObject();
            System.out.println("xts service test : deserialized WS-BA coordinator completion participant " + id);
            return participant;
        }

        return null;
    }

    public BusinessAgreementWithCoordinatorCompletionParticipant recreateCoordinatorCompletionParticipant(String id, byte[] recoveryState) throws Exception {
        if (id.startsWith(Constants.PARTICIPANT_ID_PREFIX + "CoordinatorCompletionParticipant")) {
            // this should not get called -- test WS-BA participants are saved and restored
            // using serialization
            throw new Exception("xts service test : invalid request to recreate() WS-BA coordinator completion participant " + id);
        }
        
        return null;
    }

    public void endScan()
    {
    }
}