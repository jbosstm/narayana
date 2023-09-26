/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wscf.tests.model.sagas;

import static org.junit.Assert.fail;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wscf.model.sagas.api.CoordinatorManager;
import com.arjuna.mw.wscf.model.sagas.exceptions.CoordinatorCancelledException;
import com.arjuna.mw.wscf11.model.sagas.CoordinatorManagerFactory;
import com.arjuna.wscf.tests.SagasParticipant;
import com.arjuna.wscf.tests.WSCF11TestUtils;
import com.arjuna.wscf.tests.WarDeployment;

@RunWith(Arquillian.class)
public class ParticipantFaultedCloseTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testParticipantFaultedClose()
            throws Exception
            {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

        try
        {
            String id = "1236";

            cm.begin("Sagas11HLS");

            SagasParticipant participant = new SagasParticipant(id);

            cm.enlistParticipant(participant);

            cm.participantFaulted(id);

            System.out.println("Started: "+cm.identifier()+"\n");

            cm.close();

            fail("Close succeeded after participantFaulted");
        }
        catch (CoordinatorCancelledException ex)
        {
            WSCF11TestUtils.cleanup(cm);
        }
        catch (Exception ex)
        {
            WSCF11TestUtils.cleanup(cm);
            throw ex;
        }
            }
}
