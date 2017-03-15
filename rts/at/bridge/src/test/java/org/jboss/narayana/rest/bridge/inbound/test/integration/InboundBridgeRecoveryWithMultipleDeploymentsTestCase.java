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
package org.jboss.narayana.rest.bridge.inbound.test.integration;

import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.provider.HttpResponseException;
import org.jboss.jbossts.star.util.media.txstatusext.TransactionStatusElement;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingRestATResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.LoggingXAResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.RestATManagementResource;
import org.jboss.narayana.rest.bridge.inbound.test.common.SimpleInboundBridgeResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(Arquillian.class)
public class InboundBridgeRecoveryWithMultipleDeploymentsTestCase extends AbstractTestCase {

    private static final String FIRST_DEPLOYMENT_NAME = "first-rest-tx-bridge-test";

    private static final String SECOND_DEPLOYMENT_NAME = "second-rest-tx-bridge-test";

    private static final String FIRST_DEPLOYMENT_URL = BASE_URL + "/" + FIRST_DEPLOYMENT_NAME;

    private static final String SECOND_DEPLOYMENT_URL = BASE_URL + "/" + SECOND_DEPLOYMENT_NAME;

    private static final String LOGGING_REST_AT_RESOURCE_URL = FIRST_DEPLOYMENT_URL + "/"
            + LoggingRestATResource.BASE_URL_SEGMENT;

    private static final int RECOVERY_PERIOD = 2;

    private static final int RECOVERY_WAIT_CYCLES = 5;

    private static final String VM_ARGUMENTS = System.getProperty("server.jvm.args")
            + " -Dcom.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod=" + RECOVERY_PERIOD;

    private static final String BYTEMAN_ARGUMENTS = "-Dorg.jboss.byteman.verbose"
            + " -Djboss.modules.system.pkgs=org.jboss.byteman"
            + " -Dorg.jboss.byteman.transform.all"
            + " -javaagent:../lib/byteman.jar=script:scripts/@BMScript@.btm,boot:../lib/byteman.jar,listener:true";

    @Deployment(name = FIRST_DEPLOYMENT_NAME, testable = false, managed = false)
    public static WebArchive createFirstTestArchive() {
        return ShrinkWrap.create(WebArchive.class, FIRST_DEPLOYMENT_NAME + ".war")
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF")
                .addClasses(RestATManagementResource.class, LoggingXAResource.class, SimpleInboundBridgeResource.class,
                        LoggingRestATResource.class).addAsWebInfResource("web.xml", "web.xml");
    }

    @Deployment(name = SECOND_DEPLOYMENT_NAME, testable = false, managed = false)
    public static WebArchive createSecondTestArchive() {
        return ShrinkWrap.create(WebArchive.class, SECOND_DEPLOYMENT_NAME + ".war")
                .addAsManifestResource(new StringAsset(ManifestMF), "MANIFEST.MF")
                .addClasses(RestATManagementResource.class, LoggingXAResource.class, SimpleInboundBridgeResource.class,
                        LoggingRestATResource.class).addAsWebInfResource("web.xml", "web.xml");
    }

    @After
    public void after() {
        super.after();
        stopContainer();
    }

    @Test
    public void testCrashBeforeCommit() throws Exception {
        startContainer(VM_ARGUMENTS + " " + BYTEMAN_ARGUMENTS.replace("@BMScript@", "CrashBeforeCommit"));
        txSupport.startTx();

        enlistRestATParticipant(LOGGING_REST_AT_RESOURCE_URL);
        postRestATResource(FIRST_DEPLOYMENT_URL);
        postRestATResource(SECOND_DEPLOYMENT_URL);

        try {
            txSupport.commitTx();
            Assert.fail("Container was not killed.");
        } catch (HttpResponseException e) {
            // After crash participant won't return any response and exception will be thrown.
        }

        restartContainer(VM_ARGUMENTS);

        Object status;
        int cycles = 0;

        do {
            Thread.sleep(RECOVERY_PERIOD * 3000);
            status = null;
            try {
                // Updates coordinator's active transactions list
                txSupport.getTransactions();
                // After successful recovery transaction is removed and 404 is returned.
                status = txSupport.txStatus(); //getTransactionInfo().getStatus();
            } catch (HttpResponseException e) {
            }
        } while (status != null && cycles++ < RECOVERY_WAIT_CYCLES);

        if (status != null) {
            Assert.fail("Recovery failed");
        }
    }

    @Override
    protected void startContainer(final String vmArguments) {
        if (!containerController.isStarted(CONTAINER_NAME)) {
            clearObjectStore();
            if (vmArguments == null) {
                containerController.start(CONTAINER_NAME);
            } else {
                final Config config = new Config();
                config.add("javaVmArguments", vmArguments);
                containerController.start(CONTAINER_NAME, config.map());
            }

            deployer.deploy(FIRST_DEPLOYMENT_NAME);
            deployer.deploy(SECOND_DEPLOYMENT_NAME);
        }
    }

    @Override
    protected void stopContainer() {
        deployer.undeploy(FIRST_DEPLOYMENT_NAME);
        deployer.undeploy(SECOND_DEPLOYMENT_NAME);
        containerController.stop(CONTAINER_NAME);
        containerController.kill(CONTAINER_NAME);
        clearObjectStore();
    }

}
