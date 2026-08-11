package com.arjuna.wscf11.tests.model.sagas;

import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.wscf11.tests.SagasParticipant;
import com.arjuna.wscf11.tests.WSCF11TestUtils;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 */

public class ParticipantExitedClose
{
    @Test
    public void testParticipantExitedClose()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

	try
	{
        String id = "1235";

	    cm.begin("Sagas11HLS");

        SagasParticipant participant = new SagasParticipant(id);

	    cm.enlistParticipant(participant);

	    cm.delistParticipant(id);

	    System.out.println("Started: "+cm.identifier()+"\n");

	    cm.close();
	}
    catch (Exception ex)
    {
        WSCF11TestUtils.cleanup(cm);
        throw ex;
    }
    }
}