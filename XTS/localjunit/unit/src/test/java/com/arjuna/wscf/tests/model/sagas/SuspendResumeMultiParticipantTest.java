package com.arjuna.wscf.tests.model.sagas;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.wscf.tests.SagasParticipant;
import com.arjuna.wscf.tests.WSCF11TestUtils;
import com.arjuna.wscf.tests.WarDeployment;

@RunWith(Arquillian.class)
public class SuspendResumeMultiParticipantTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testSuspendResumeMultiParticipant()
            throws Exception
            {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

        try
        {
            cm.begin("Sagas11HLS");

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
