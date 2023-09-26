/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.wscf.tests.model.twophase;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.mw.wscf.model.twophase.api.CoordinatorManager;
import com.arjuna.mw.wscf11.model.twophase.CoordinatorManagerFactory;
import com.arjuna.wscf.tests.TwoPhaseParticipant;
import com.arjuna.wscf.tests.WSCF11TestUtils;
import com.arjuna.wscf.tests.WarDeployment;


@RunWith(Arquillian.class)
public class AddParticipantTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    @Test
    public void testAddParticipant()
            throws Exception
            {
        System.out.println("Running test : " + this.getClass().getName());

        CoordinatorManager cm = CoordinatorManagerFactory.coordinatorManager();

        try
        {
            cm.begin("TwoPhase11HLS");

            cm.enlistParticipant(new TwoPhaseParticipant(null));

            System.out.println("Started: "+cm.identifier()+"\n");

            cm.confirm();
        }
        catch (Exception ex)
        {
            WSCF11TestUtils.cleanup(cm);
            throw ex;
        }
            }
}
