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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.jaxrs.JAXRSFraction;
import org.wildfly.swarm.logging.LoggingFraction;

import io.narayana.lra.annotation.Forget;
import io.narayana.lra.cdi.bean.AllAnnotationsNoPathBean;
import io.narayana.lra.cdi.bean.CompleteOptionalBean;
import io.narayana.lra.cdi.bean.CorrectBean;
import io.narayana.lra.cdi.bean.CorrectMethodLRABean;
import io.narayana.lra.cdi.bean.ForgetWithoutDeleteBean;
import io.narayana.lra.cdi.bean.LeaveWithoutPutBean;
import io.narayana.lra.cdi.bean.LraJoinFalseBean;
import io.narayana.lra.cdi.bean.LraJoinFalseMethodLRABean;
import io.narayana.lra.cdi.bean.MultiForgetBean;
import io.narayana.lra.cdi.bean.NoPostOrGetBean;
import io.narayana.lra.cdi.bean.OnlyOneLraAnnotationBean;
import io.narayana.lra.cdi.bean.OnlyTwoLraAnnotationsBean;

/**
 * Test case which checks functionality of CDI extension by deploying wrongly
 * composed LRA components and expect an deployment exception to be thrown.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class StartCdiCheckIT {
    private static final Logger log = Logger.getLogger(StartCdiCheckIT.class.getName());

    private static final int SWARM_START_TIMEOUT = Integer.getInteger("swarm.test.start.timeout", 2);
    private static final String LOG_FILE_NAME = "target/cdi-test-swarm.log"; // see logging.properties

    @Rule
    public TestName testName = new TestName();

    @Before
    public void cleanUp() throws Exception {
        File log = new File(LOG_FILE_NAME);
        if(log.exists()) {
            // need to clean file for the next test can check existence of log strings
            PrintWriter writer = new PrintWriter(log);
            writer.print("");
            writer.close();
        }
    }

    @Test
    public void onlyCompensateAnnotationPresent() throws Exception {
        checkSwarmWithDeploymentException("LRA which requires methods handling LRA events. Missing annotations",
            OnlyOneLraAnnotationBean.class);
    }

    @Test
    public void onlyCompleteAndStatusAnnotationsPresent() throws Exception {
        checkSwarmWithDeploymentException("LRA which requires methods handling LRA events. Missing annotations",
            OnlyTwoLraAnnotationsBean.class);
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
        Swarm swarm = startSwarm();
        try {
            swarm.deploy(getBaseDeployment().addClasses(LraJoinFalseBean.class));
        } finally {
            swarm.stop();
        }
    }
    
    @Test
    public void lraJoinFalseCorrectLRAOnMethod() throws Exception {
        Swarm swarm = startSwarm();
        try {
            swarm.deploy(getBaseDeployment().addClasses(LraJoinFalseMethodLRABean.class));
        } finally {
            swarm.stop();
        }
    }

    @Test
    public void allCorrect() throws Exception {
        Swarm swarm = startSwarm();
        try {
            swarm.deploy(getBaseDeployment().addClasses(CorrectBean.class));
        } finally {
            swarm.stop();
        }
    }
    
    @Test
    public void allCorrectLRAOnMethod() throws Exception {
        Swarm swarm = startSwarm();
        try {
            swarm.deploy(getBaseDeployment().addClasses(CorrectMethodLRABean.class));
        } finally {
            swarm.stop();
        }
    }
    
    @Test
    public void completeAnnotationIsOptional() throws Exception {
        Swarm swarm = startSwarm();
        try {
            swarm.deploy(getBaseDeployment().addClasses(CompleteOptionalBean.class));
        } finally {
            swarm.stop();
        }
    }

    private WebArchive getBaseDeployment() {
        WebArchive deployment = ShrinkWrap.create(WebArchive.class, "lra-cdi-check.war")
            .addClass(LraAnnotationProcessingExtension.class)
            .addAsManifestResource(new StringAsset(LraAnnotationProcessingExtension.class.getName()),
                "services/javax.enterprise.inject.spi.Extension")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        
        File[] libs = Maven.resolver()
            .loadPomFromFile("pom.xml")
            .resolve("org.jboss.narayana.rts:lra-annotations")
            .withTransitivity().as(File.class); 
        deployment.addAsLibraries(libs);
        
        return deployment;
    }

    private Swarm startSwarm() throws Exception{
        final Swarm swarm = new Swarm()
            .fraction(new JAXRSFraction())
            .fraction(new CDIFraction())
            .fraction(new LoggingFraction());

        final String testMethodName = this.testName.getMethodName();
        log.infof("Starting swarm '%s' for test '%s'", swarm, testMethodName);

        runWithTimeout(() -> {
            try {
                swarm.start();
            } catch (Exception startE) {
                log.errorf(startE, "Error starting swarm '%s' for test '%s'", swarm, testMethodName);
                runWithTimeout(() -> {
                    try {
                        swarm.stop();
                    } catch (Exception stopE) {
                        log.debugf(stopE, "Error stopping swarm '%s' for test '%s'", swarm, testMethodName);
                    }
                }, 1, TimeUnit.MINUTES);
            }
        }, SWARM_START_TIMEOUT, TimeUnit.MINUTES);

        return swarm;
    }

    private static void runWithTimeout(Runnable r, int timeout, TimeUnit timeUnit) {
        log.tracef("Running runnable '%s' with timeout '%s' s", r, timeUnit.toSeconds(timeout));
        ExecutorService e = Executors.newSingleThreadExecutor();
        e.submit(r);

        try {
            e.shutdown();
            e.awaitTermination(timeout, timeUnit);
        }
        catch (InterruptedException ie) {
            log.debugf(ie, "Shutdowning executor '%s' task of test execution interrupted", r);
        }
        finally {
            if (!e.isTerminated()) {
                log.debugf("Executor '%s' was not finished we are going to forcibly end it", r);
            }
            e.shutdownNow();
        }
    }

    private void checkSwarmWithDeploymentException(String stringToMatch, Class<?>... classesToAdd) throws Exception {
        Swarm swarm = startSwarm();
        try {
            log.infof("Test '%s' of swarm '%s' deploying '%s'", testName.getMethodName(), swarm, classesToAdd);
            swarm
                .deploy(getBaseDeployment().addClasses(classesToAdd));
            Assert.fail("Expected deployment exception to be thrown");
        } catch (org.wildfly.swarm.container.DeploymentException de) {
            // expected
        } finally {
            swarm.stop();
        }

        assertLogLine(new File(LOG_FILE_NAME), stringToMatch);
    }

    private void assertLogLine(File file, String expectedString) throws IOException {
        Assert.assertNotNull("file", file);
        boolean isContain = Files.readAllLines(file.toPath()).stream()
            .anyMatch(line -> line.matches(".*" + expectedString + ".*"));
        Assert.assertTrue("Log file does not contain '" + expectedString + "'", isContain);
    }
}
