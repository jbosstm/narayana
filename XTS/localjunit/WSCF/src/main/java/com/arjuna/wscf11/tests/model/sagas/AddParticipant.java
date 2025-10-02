package com.arjuna.wscf11.tests.model.sagas;

import javax.inject.Named;

import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.wscf11.tests.SagasParticipant;
import com.arjuna.wscf11.tests.WSCF11TestUtils;

/**
 */

@Named
public class AddParticipant
{
    public void testAddParticipant()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

	try
	{
	    cm.begin("Sagas11HLS");

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