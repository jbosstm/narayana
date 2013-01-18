package com.arjuna.wscf.tests.model.twophase;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.exceptions.NoCoordinatorException;
import com.arjuna.mw.wscf.model.twophase.api.UserCoordinator;
import com.arjuna.mw.wscf11.model.twophase.UserCoordinatorFactory;
import com.arjuna.wscf.tests.WSCF11TestUtils;
import com.arjuna.wscf.tests.WarDeployment;

@RunWith(Arquillian.class)
public class SuspendConfirmTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testSuspendConfirm()
            throws Exception
            {
        System.out.println("Running test : " + this.getClass().getName());

        UserCoordinator ua = UserCoordinatorFactory.userCoordinator();

        try
        {
            ua.begin("TwoPhase11HLS");

            System.out.println("Started: "+ua.identifier()+"\n");

            ActivityHierarchy hier = ua.suspend();

            System.out.println("Suspended: "+hier+"\n");

            if (ua.currentActivity() != null) {
                WSCF11TestUtils.cleanup(ua);
                fail("Hierarchy still active.");
            }

            ua.confirm();
        }
        catch (NoCoordinatorException ex)
        {
            // why is it ok to get here?
            WSCF11TestUtils.cleanup(ua);
        }
        catch (Exception ex)
        {
            WSCF11TestUtils.cleanup(ua);
            throw ex;
        }
            }
}
