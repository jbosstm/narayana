package com.arjuna.wscf11.tests.model.sagas;

import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.wscf11.tests.SagasParticipant;
import com.arjuna.wscf11.tests.WSCF11TestUtils;
import org.junit.Test;

/**
 */

public class AddParticipant
{
    @Test
    public void testAddParticipant()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

	try
	{
	    cm.begin();

	    cm.enlistParticipant(new SagasParticipant(null));

	    System.out.println("Started: "+cm.identifier()+"\n");

        cm.complete();

        cm.close();
	}
	catch (Exception ex)
	{
	    WSCF11TestUtils.cleanup(cm);
        throw ex;
    }
    }
}