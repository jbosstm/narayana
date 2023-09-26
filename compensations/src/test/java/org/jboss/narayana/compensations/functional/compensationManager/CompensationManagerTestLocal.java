/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.functional.compensationManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;


/**
 * @author paul.robinson@redhat.com 22/03/2013
 */
@RunWith(Arquillian.class)
public class CompensationManagerTestLocal extends CompensationManagerTest {


    @Deployment
    public static WebArchive createTestArchive() {

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage("org.jboss.narayana.compensations.functional.common")
                .addPackage("org.jboss.narayana.compensations.functional.compensationManager")
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

        return archive;
    }

}