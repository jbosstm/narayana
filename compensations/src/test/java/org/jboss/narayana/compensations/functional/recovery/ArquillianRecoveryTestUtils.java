/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.compensations.functional.recovery;

import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static java.io.File.separator;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public final class ArquillianRecoveryTestUtils {

    private static final int RECOVERY_PERIOD = 10;

    private static final int TRANSPORT_TIMEOUT = 2000;

    public static final String BASE_VM_ARGUMENTS = System.getProperty("server.jvm.args").trim()
            + " -Dcom.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod=" + RECOVERY_PERIOD
            + " -Dorg.jboss.jbossts.xts.transport.transportTimeout=" + TRANSPORT_TIMEOUT;

    public static final String BYTEMAN_VM_ARGUMENTS = BASE_VM_ARGUMENTS.replaceAll("=listen",
            "=script:" + System.getProperty("project.build.directory") + "/test-classes/scripts/crash-recovery-rules.btm,boot:"
                    + System.getProperty("project.build.directory") + "/lib/byteman.jar,listen");

    private final ContainerController containerController;

    private final Deployer deployer;

    private final String containerName;

    private final String deploymentName;

    private final String jbossHome;

    public ArquillianRecoveryTestUtils(ContainerController containerController, Deployer deployer, String containerName,
            String deploymentName, String jbossHome) {
        this.containerController = containerController;
        this.deployer = deployer;
        this.containerName = containerName;
        this.deploymentName = deploymentName;
        this.jbossHome = jbossHome;
    }

    public void sleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

    public void startContainer(List<String> bytemanFiles) {
        if (!containerController.isStarted(containerName)) {
            clearObjectStore();
            restartContainer(bytemanFiles);
            deployer.deploy(deploymentName);
        }
    }

    @SuppressWarnings("all")
    public void restartContainer(List<String> bytemanFiles) {
        Config config = new Config().add("javaVmArguments", getVmArguments(bytemanFiles));
        containerController.start(containerName, config.map());
    }

    public void stopContainer() {
        deployer.undeploy(deploymentName);
        containerController.stop(containerName);
        containerController.kill(containerName);
    }

    private void clearObjectStore() {
        File objectStore = new File(jbossHome + separator + "standalone" + separator + "data" + separator + "tx-object-store");

        if (objectStore.exists()) {
            if (!deleteDirectory(objectStore)) {
                fail("Failed to clear object store: " + objectStore.getPath());
            }
        }
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            for (File file : path.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }

        return path.delete();
    }

    private String getVmArguments(List<String> bytemanFiles) {
        if (bytemanFiles.isEmpty()) {
            return BASE_VM_ARGUMENTS;
        }

        String projectBuildDirectory = System.getProperty("project.build.directory");
        String bytemanScripts = bytemanFiles.stream()
                .map(file -> "script:" + projectBuildDirectory + "/test-classes/scripts/" + file)
                .collect(Collectors.joining(","));

        return BASE_VM_ARGUMENTS.replaceAll("=listen",
                "=" + bytemanScripts + ",boot:" + projectBuildDirectory + "/lib/byteman.jar,listen");
    }

}
