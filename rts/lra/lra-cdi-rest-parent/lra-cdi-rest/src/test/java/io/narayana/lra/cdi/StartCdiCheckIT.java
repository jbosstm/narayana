/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
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

package io.narayana.lra.cdi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.process.JavaProcess;
import org.zeroturnaround.process.Processes;

import io.narayana.lra.annotation.Forget;
import io.narayana.lra.cdi.bean.AllAnnotationsNoPathBean;
import io.narayana.lra.cdi.bean.AsyncSuspendWithoutForgetBean;
import io.narayana.lra.cdi.bean.CorrectBean;
import io.narayana.lra.cdi.bean.CorrectMethodLRABean;
import io.narayana.lra.cdi.bean.ForgetWithoutDeleteBean;
import io.narayana.lra.cdi.bean.LRANoContextBean;
import io.narayana.lra.cdi.bean.LeaveWithoutPutBean;
import io.narayana.lra.cdi.bean.LraJoinFalseBean;
import io.narayana.lra.cdi.bean.LraJoinFalseMethodLRABean;
import io.narayana.lra.cdi.bean.MultiForgetBean;
import io.narayana.lra.cdi.bean.NoPostOrGetBean;

/**
 * Test case which checks functionality of CDI extension by deploying wrongly
 * composed LRA components and expect an deployment exception to be thrown.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class StartCdiCheckIT {
    private static final Logger log = Logger.getLogger(StartCdiCheckIT.class.getName());

    private static final int SWARM_START_TIMEOUT_S = Integer.getInteger("swarm.test.start.timeout", 30);
    private static final String SWARM_LOG_LEVEL = System.getProperty("swarm.test.logging.level", "INFO");
    private static final Boolean SWARM_CONSOLE_PRINT = Boolean.getBoolean("swarm.test.full.console.print");

    private static final File SWARM_ARCHIVE = new File("../lra-cdi-rest-test-template/target/lra-cdi-rest-test-template-swarm.jar");
    private static File[] lraAnnotationsLibs; // lra-annotations artifact resolved by ShrinkWrap from pom.xml

    @Rule
    public TestName testName = new TestName();

    @BeforeClass
    public static void classSetUp() {
        lraAnnotationsLibs = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .resolve("org.jboss.narayana.rts:lra-annotations")
            .withTransitivity()
            .as(File.class);
    }

    @Test
    public void complementaryPathAnnotation() throws Exception {
        checkSwarmWithDeploymentException("should use complementary annotation.*Path",
            AllAnnotationsNoPathBean.class);
    }

    @Test
    public void methodTypeAnnotationMissing() throws Exception {
        checkSwarmWithDeploymentException("should use complementary annotation.*(PUT|GET)",
            NoPostOrGetBean.class);
    }

    @Test
    public void asyncInvocationWithoutForgetDefined() throws Exception {
        checkSwarmWithDeploymentException("The LRA class has to contain @Status and @Forget annotations",
                AsyncSuspendWithoutForgetBean.class);
    }

    @Test
    public void forgetMissingDelete() throws Exception {
        checkSwarmWithDeploymentException("should use complementary annotation.*(DELETE)",
            ForgetWithoutDeleteBean.class);
    }

    @Test
    public void leaveMissingPut() throws Exception {
        checkSwarmWithDeploymentException("should use complementary annotation.*(PUT)",
            LeaveWithoutPutBean.class);
    }

    @Test
    public void multiForgetAnnotations() throws Exception {
        checkSwarmWithDeploymentException("multiple annotations.*" + Forget.class.getName(),
            MultiForgetBean.class);
    }

    @Test
    public void lraJoinFalseCorrect() throws Exception {
        checkSwarmNoException(LraJoinFalseBean.class);
    }

    @Test
    public void lraJoinFalseCorrectLRAOnMethod() throws Exception {
        checkSwarmNoException(LraJoinFalseMethodLRABean.class);
    }

    @Test
    public void allCorrect() throws Exception {
        checkSwarmNoException(CorrectBean.class);
    }

    @Test
    public void allCorrectLRAOnMethod() throws Exception {
        checkSwarmNoException(CorrectMethodLRABean.class);
    }

    @Test
    public void noLraContext() throws Exception {
        checkSwarmNoException(LRANoContextBean.class);
    }

    private WebArchive getBaseDeployment() {
        WebArchive deployment = ShrinkWrap.create(WebArchive.class)
            .addClass(LraAnnotationProcessingExtension.class)
            .addAsManifestResource(new StringAsset(LraAnnotationProcessingExtension.class.getName()),
                "services/javax.enterprise.inject.spi.Extension")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        deployment.addAsLibraries(lraAnnotationsLibs);

        return deployment;
    }

    private StartedProcess startSwarm(WebArchive deployment, OutputStream outputStream, OutputStream errorStream) throws Exception{
        final String testMethodName = this.testName.getMethodName();
        log.infof("Starting swarm for test '%s'", testMethodName);

        File testDeployment = new File("target", "test.war");
        deployment.as(ZipExporter.class).exportTo(testDeployment, true);

        ProcessExecutor executor = new ProcessExecutor().command(
            "java", "-Dfile=" + testDeployment.getAbsolutePath(), "-Dswarm.logging=" + SWARM_LOG_LEVEL, "-jar", SWARM_ARCHIVE.getAbsolutePath());

        if(outputStream != null) executor.redirectOutputAlsoTo(outputStream);
        if(errorStream != null)  executor.redirectErrorAlsoTo(errorStream);

        log.infof("Running swarm with command: '%s'", executor.getCommand().stream().collect(Collectors.joining(" ")));
        return executor.start();
    }

    private void stopSwarm(StartedProcess startedProcess) throws InterruptedException, IOException {
        JavaProcess javaProcess = Processes.newJavaProcess(startedProcess.getProcess());

        boolean finished = javaProcess.destroyGracefully().waitFor(SWARM_START_TIMEOUT_S, TimeUnit.SECONDS);

        if(!finished) {
            javaProcess.destroyForcefully();
        }
    }

    private void checkSwarmWithDeploymentException(String stringToMatch, Class<?>... classesToAdd) throws Exception {
        WebArchive deployment = getBaseDeployment().addClasses(classesToAdd);
        OutputStream output = new ByteArrayOutputStream();
        StartedProcess swarmProcess = startSwarm(deployment, output, output);

        try {
            boolean isContain = false;
            long timeoutMs = TimeUnit.SECONDS.toMillis(SWARM_START_TIMEOUT_S); // 10s wait for swarm to start
            long startMs = System.currentTimeMillis();
            while(startMs + timeoutMs > System.currentTimeMillis() && !isContain) {
                String[] lines = output.toString().split(System.getProperty("line.separator"));
                isContain = Arrays.asList(lines).stream()
                    .anyMatch(line -> line.matches(".*" + stringToMatch + ".*"));
                Thread.sleep(1000);
            }

            if(!isContain) {
                log.errorf("=====================================================%n" +
                           "Swarm checking string %s deployed with classes %s fails. Swarm run output: %s%n" +
                           "=====================================================%n",
                           stringToMatch, Arrays.asList(classesToAdd), output.toString());
                Assert.fail("The swarm output does not contain '" + stringToMatch + "'");
            }
        } finally {
            stopSwarm(swarmProcess);
            if(SWARM_CONSOLE_PRINT) log.infof(output.toString());
        }
    }

    private void checkSwarmNoException(Class<?>... classesToAdd) throws Exception {
        WebArchive deployment = getBaseDeployment().addClasses(classesToAdd);
        OutputStream outputStd = new ByteArrayOutputStream();
        OutputStream outputErr = new ByteArrayOutputStream();
        StartedProcess swarmProcess = startSwarm(deployment, outputStd, outputErr);

        try {
            boolean isStarted = false;
            boolean isContainErr = false;
            long timeoutMs = TimeUnit.SECONDS.toMillis(SWARM_START_TIMEOUT_S); // 10s wait for swarm to start
            long startMs = System.currentTimeMillis();
            while(startMs + timeoutMs > System.currentTimeMillis() && !isStarted && !isContainErr) {
                isStarted = outputStd.toString().contains("WFSWARM99999");
                isContainErr = !outputErr.toString().isEmpty() || outputStd.toString().contains("ERROR");
                Thread.sleep(1000);
            }
            if(!isStarted || isContainErr) {
                log.errorf("=====================================================%n" +
                        "Errors on timeout (%s s) on Swarm startup happens for deployed clases: %s. Outputs: %s%n%s%n" +
                        "=====================================================%n",
                        SWARM_START_TIMEOUT_S, Arrays.asList(classesToAdd), outputStd.toString(), outputErr.toString());
                Assert.fail("Errors on Swarm startup happens. Check test log for details.");
            }
        } finally {
            stopSwarm(swarmProcess);
            if(SWARM_CONSOLE_PRINT) log.infof(outputStd.toString(), outputErr.toString());
        }
    }
}
