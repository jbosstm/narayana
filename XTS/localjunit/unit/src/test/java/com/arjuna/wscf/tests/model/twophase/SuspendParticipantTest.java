/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wscf.tests.model.twophase;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wsas.activity.ActivityHierarchy;
import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.mw.wscf11.model.twophase.CoordinatorManagerFactory;
import com.arjuna.wscf.tests.TwoPhaseParticipant;
import com.arjuna.wscf.tests.TwoPhaseSynchronization;
import com.arjuna.wscf.tests.WSCF11TestUtils;
import com.arjuna.wscf.tests.WarDeployment;

@RunWith(Arquillian.class)
public class SuspendParticipantTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testSuspendParticipant()
            throws Exception
            {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

        try
        {
            cm.begin("TwoPhase11HLS");

            cm.enlistParticipant(new TwoPhaseParticipant("p1"));
            cm.enlistParticipant(new TwoPhaseParticipant("p2"));
            cm.enlistSynchronization(new TwoPhaseSynchronization());

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

            cm.confirm();
        }
        catch (Exception ex)
        {
            WSCF11TestUtils.cleanup(cm);

            throw ex;
        }
            }
}
