package com.arjuna.wscf.tests.model.twophase;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wscf.model.twophase.api.UserCoordinator;
import com.arjuna.mw.wscf11.model.twophase.UserCoordinatorFactory;
import com.arjuna.wscf.tests.WarDeployment;

@RunWith(Arquillian.class)
public class BeginCancelTest {
	
	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment();
	}
	
	@Test
    public void testBeginCancel()
            throws Exception
    {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

        ua.begin("TwoPhase11HLS");

        System.out.println("Started: "+ua.identifier()+"\n");

        ua.cancel();

        System.out.println("Cancelled");
    }
}
