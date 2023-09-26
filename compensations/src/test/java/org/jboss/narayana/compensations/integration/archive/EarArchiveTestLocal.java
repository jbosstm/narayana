/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package org.jboss.narayana.compensations.integration.archive;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class EarArchiveTestLocal {

    @Deployment(name = "client")
    public static WebArchive getClientDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "client.war")
                .addClasses(EarArchiveTestLocal.class, TestServiceClient.class, TestService.class)
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addAsWebInfResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

        System.out.println(archive.toString(true));

        return archive;
    }

    @Deployment(name = "service")
    public static EnterpriseArchive getServiceDeployment() {
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, "compensations-ejb.jar")
                .addClasses(TestConfirmationHandler.class, TestService.class, TestServiceImpl.class, TestServiceService.class)
                .addAsManifestResource(new StringAsset("<beans bean-discovery-mode=\"all\"></beans>"), "beans.xml");

        EnterpriseArchive enterpriseArchive = ShrinkWrap.create(EnterpriseArchive.class, "service.ear")
                .addAsModule(javaArchive)
                .setApplicationXML("test-application.xml");

        System.out.println(enterpriseArchive.toString(true));

        return enterpriseArchive;
    }

    @Test
    @OperateOnDeployment("client")
    public void test() {
        ParticipantCompletionCoordinatorRules.setParticipantCount(1);
        TestServiceClient client = new TestServiceClient("compensations-ejb");
        int invocationsCounterBefore = client.getConfirmationHandlerInvocationsCounter();
        client.compensatableMethod();
        Assert.assertEquals(invocationsCounterBefore + 1, client.getConfirmationHandlerInvocationsCounter());
    }

}