package org.jboss.narayana.rest.integration.test.integration;

import java.io.File;

import org.junit.Assert;

import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.jbossts.star.util.TxSupport;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 *
 */
public abstract class AbstractIntegrationTestCase {

    private static final String CONTAINER_NAME = "jboss";

    protected static final String DEPLOYMENT_NAME = "test";

    protected static final String BASE_URL = getBaseUrl();

    protected static final String DEPLOYMENT_URL = BASE_URL + "/" + DEPLOYMENT_NAME;

    protected static final String TRANSACTION_MANAGER_URL = BASE_URL + "/rest-at-coordinator/tx/transaction-manager";

    @ArquillianResource
    private ContainerController containerController;

    @ArquillianResource
    private Deployer deployer;

    protected TxSupport txSupport;

    @Before
    public void before() {
        txSupport = new TxSupport(TRANSACTION_MANAGER_URL);
    }

    @After
    public void after() {
        try {
            txSupport.rollbackTx();
        } catch (Throwable t){
        }
    }

    protected void startContainer() {
        startContainer(null);
    }

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

            deployer.deploy(DEPLOYMENT_NAME);
        }
    }

    protected void restartContainer(final String vmArguments) {
        if (vmArguments == null) {
            containerController.start(CONTAINER_NAME);
        } else {
            final Config config = new Config();
            config.add("javaVmArguments", vmArguments);
            containerController.start(CONTAINER_NAME, config.map());
        }
    }

    protected void stopContainer() {
        deployer.undeploy(DEPLOYMENT_NAME);
        containerController.stop(CONTAINER_NAME);
        containerController.kill(CONTAINER_NAME);
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

    private static String getBaseUrl() {
        String baseAddress = System.getProperty("jboss.bind.address");
        String basePort = System.getProperty("jboss.bind.port");

        if (baseAddress == null) {
            baseAddress = "http://localhost";
        } else if (!baseAddress.toLowerCase().startsWith("http://") && !baseAddress.toLowerCase().startsWith("https://")) {
            baseAddress = "http://" + baseAddress;
        }

        if (basePort == null) {
            basePort = "8080";
        }

        return baseAddress + ":" + basePort;
    }

}
