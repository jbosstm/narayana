/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.wstx.tests.arq.ba;

import com.arjuna.mw.wst11.BusinessActivityManager;
import com.arjuna.mw.wst11.UserBusinessActivity;
import com.arjuna.wst.TransactionRolledBackException;
import com.arjuna.wstx.tests.arq.WarDeployment;
import com.arjuna.wstx.tests.common.DemoBusinessParticipant;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.BMScript;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorCloseBeforeCompletedRules;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class CloseBeforeCompletedTest {

    @Deployment
    public static WebArchive createDeployment() {

        return WarDeployment.getDeployment(
                DemoBusinessParticipant.class,
                ParticipantCompletionCoordinatorCloseBeforeCompletedRules.class);
    }

    @BeforeClass()
    public static void submitBytemanScript() throws Exception {

        BMScript.submit(ParticipantCompletionCoordinatorCloseBeforeCompletedRules.RESOURCE_PATH);
    }

    @AfterClass()
    public static void removeBytemanScript() {

        BMScript.remove(ParticipantCompletionCoordinatorCloseBeforeCompletedRules.RESOURCE_PATH);
    }

    @Test
    public void test() throws Exception {

        ParticipantCompletionCoordinatorCloseBeforeCompletedRules.setParticipantCount(1);

        UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
        com.arjuna.wst11.BAParticipantManager bpm = null;
        DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.CLOSE, "1235");
        try {
            uba.begin();

            bpm = bam.enlistForBusinessAgreementWithParticipantCompletion(p, p.identifier());
            bpm.completed();
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch (Exception einner) {
                einner.printStackTrace();
            }
            throw eouter;
        }

        boolean caughtTransactionRolledBackException = false;
        try {
            uba.close();
        } catch (TransactionRolledBackException e) {
            caughtTransactionRolledBackException = true;
        }
        assertTrue(caughtTransactionRolledBackException);
    }
}
