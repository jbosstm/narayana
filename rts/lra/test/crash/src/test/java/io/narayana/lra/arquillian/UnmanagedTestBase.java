/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package io.narayana.lra.arquillian;

import io.narayana.lra.logging.LRALogger;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.runner.RunWith;

/**
 * This class is the test base to manually manage Arquillian containers.
 * All test classes in this modules should extend this class.
 */
@RunWith(Arquillian.class)
public abstract class UnmanagedTestBase {

    @ArquillianResource
    private ContainerController containerController;

    @ArquillianResource
    private Deployer deployer;

    final void startContainer(String containerQualifier, String deploymentQualifier) {

        if (!containerController.isStarted(containerQualifier)) {
            LRALogger.logger.debugf("Starting container %s", containerQualifier);
            containerController.start(containerQualifier);
        }

        deployer.deploy(deploymentQualifier);
    }

    final void restartContainer(String containerQualifier) {
        try {
            if (containerController.isStarted(containerQualifier)) {
                // ensure that the controller is not running
                containerController.stop(containerQualifier);
                LRALogger.logger.debugf("Container %s was killed successfully", containerController);
            }
        } catch (Exception e) {
            LRALogger.logger.errorf("There was an error killing the container %s: %s", containerQualifier, e.getMessage());
        }

        containerController.start(containerQualifier);
    }

    final void stopContainer(String containerQualifier, String deploymentQualifier) {
        if (containerController.isStarted(containerQualifier)) {
            LRALogger.logger.debugf("Stopping container %s", containerQualifier);

            deployer.undeploy(deploymentQualifier);

            containerController.stop(containerQualifier);
        }
    }
}
