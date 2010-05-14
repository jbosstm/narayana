package com.arjuna.wscf11.tests.model.sagas;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.wscf11.tests.SagasParticipant;
import com.arjuna.wscf11.tests.WSCF11TestUtils;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 */

public class SuspendResumeMultiParticipant
{
    @Test
    public void testSuspendResumeMultiParticipant()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

	try
	{
	    cm.begin();

	    cm.enlistParticipant(new SagasParticipant("p1"));
	    cm.enlistParticipant(new SagasParticipant("p2"));

	    System.out.println("Started: "+cm.identifier()+"\n");

	    ActivityHierarchy hier = cm.suspend();

	    System.out.println("Suspended: "+hier+"\n");

	    if (cm.currentActivity() != null)
	    {
            WSCF11TestUtils.cleanup(cm);

            fail("Hierarchy still active.");
	    }
		cm.resume(hier);

        System.out.println("Resumed: "+hier+"\n");

		cm.close();
	}
	catch (Exception ex)
	{
	    WSCF11TestUtils.cleanup(cm);

        throw ex;
    }
    }
}