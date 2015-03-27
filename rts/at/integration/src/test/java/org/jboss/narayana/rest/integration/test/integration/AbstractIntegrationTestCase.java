package org.jboss.narayana.rest.integration.test.integration;

import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Assert;

import java.io.File;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractIntegrationTestCase {

    @ArquillianResource
    private ContainerController containerController;

    @ArquillianResource
    private Deployer deployer;

    protected void startContainer(final String containerName, final String deploymentName) {
        startContainer(containerName, deploymentName, null);
    }

    protected void startContainer(final String containerName, final String deploymentName, final String vmArguments) {
        if (!containerController.isStarted(containerName)) {
            clearObjectStore();
            if (vmArguments == null) {
                containerController.start(containerName);
            } else {
                final Config config = new Config();
                config.add("javaVmArguments", vmArguments);
                containerController.start(containerName, config.map());
            }

            deployer.deploy(deploymentName);
        }
    }

    protected void restartContainer(final String containerName, final String vmArguments) {
        if (vmArguments == null) {
            containerController.start(containerName);
        } else {
            final Config config = new Config();
            config.add("javaVmArguments", vmArguments);
            containerController.start(containerName, config.map());
        }
    }

    protected void stopContainer(final String containerName, final String deplymentName) {
        deployer.undeploy(deplymentName);
        containerController.stop(containerName);
        containerController.kill(containerName);
    }

    protected void clearObjectStore() {
        final String jbossHome = System.getenv("JBOSS_HOME");
        if (jbossHome == null) {
            Assert.fail("$JBOSS_HOME not set");
        } else {
            final File objectStore = new File(jbossHome + File.separator + "standalone" + File.separator + "data" + File.separator
                    + "tx-object-store");

            if (objectStore.exists()) {
                if (!deleteDirectory(objectStore)) {
                    Assert.fail("Failed to remove tx-object-store: " + objectStore.getPath());
                }
            }
        }
    }

    protected static boolean deleteDirectory(final File path) {
        if (path.exists()) {
            final File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }

        return (path.delete());
    }

}
