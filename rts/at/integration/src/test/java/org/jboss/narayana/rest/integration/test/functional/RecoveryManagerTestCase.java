package org.jboss.narayana.rest.integration.test.functional;

import com.arjuna.ats.arjuna.common.Uid;
import junit.framework.Assert;
import org.jboss.jbossts.star.util.TxStatus;
import org.jboss.narayana.rest.integration.ParticipantInformation;
import org.jboss.narayana.rest.integration.ParticipantsContainer;
import org.jboss.narayana.rest.integration.RecoveryManager;
import org.jboss.narayana.rest.integration.api.ParticipantsManagerFactory;
import org.jboss.narayana.rest.integration.api.Prepared;
import org.jboss.narayana.rest.integration.test.common.LoggingParticipant;
import org.jboss.narayana.rest.integration.test.common.TestParticipantDeserializer;
import org.junit.Test;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class RecoveryManagerTestCase {

    private static final String APPLICATION_ID = "org.jboss.narayana.rest.integration.test.functional.RecoveryManagerTestCase";

    @Test
    public void testRecoveryWithoutDeserializer() {
        final String participantId = new Uid().toString();
        final LoggingParticipant loggingParticipantBefore = new LoggingParticipant(new Prepared());
        loggingParticipantBefore.commit();

        final ParticipantInformation participantInformationBefore = new ParticipantInformation(participantId, APPLICATION_ID + "1",
                "", loggingParticipantBefore, TxStatus.TransactionCommitted.name());

        ParticipantsContainer.getInstance().clear();
        ParticipantsManagerFactory.getInstance().setBaseUrl("");
        RecoveryManager.getInstance().persistParticipantInformation(participantInformationBefore);
        RecoveryManager.getInstance().registerDeserializer(APPLICATION_ID + "2", new TestParticipantDeserializer());

        final ParticipantInformation participantInformationAfter = ParticipantsContainer.getInstance()
                .getParticipantInformation(participantId);
        Assert.assertNull(participantInformationAfter);
    }

}
