package com.arjuna.wscf.tests.model.sagas;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.wscf.tests.SagasParticipant;
import com.arjuna.wscf.tests.WSCF11TestUtils;
import com.arjuna.wscf.tests.WarDeployment;

@RunWith(Arquillian.class)
public class ParticipantExitedCloseTest {
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment();
	}

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
