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

package org.jboss.narayana.compensations.integration.archive;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.xts.bytemanSupport.participantCompletion.ParticipantCompletionCoordinatorRules;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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

    private TestServiceClient client = new TestServiceClient("compensations-ejb");

    @Deployment(name = "client")
    public static WebArchive getClientDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "client.war")
                .addClasses(EarArchiveTestLocal.class, TestServiceClient.class, TestService.class)
                .addClass(ParticipantCompletionCoordinatorRules.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        System.out.println(archive.toString(true));

        return archive;
    }

    @Deployment(name = "service")
    public static EnterpriseArchive getServiceDeployment() {
        JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class, "compensations-ejb.jar")
                .addClasses(TestConfirmationHandler.class, TestService.class, TestServiceImpl.class, TestServiceService.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

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
        int invocationsCounterBefore = client.getConfirmationHandlerInvocationsCounter();
        client.compensatableMethod();
        Assert.assertEquals(invocationsCounterBefore + 1, client.getConfirmationHandlerInvocationsCounter());
    }

}
