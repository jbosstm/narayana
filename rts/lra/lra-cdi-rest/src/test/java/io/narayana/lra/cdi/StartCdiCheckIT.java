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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import io.narayana.lra.cdi.bean.AllAnnotationsNoPathBean;
import io.narayana.lra.cdi.bean.ForgetWithoutDeleteBean;
import io.narayana.lra.annotation.Forget;
import io.narayana.lra.cdi.LraAnnotationProcessingExtension;
import io.narayana.lra.cdi.bean.OnlyOneLraAnnotationBean;
import io.narayana.lra.cdi.bean.OnlyTwoLraAnnotationsBean;
import io.narayana.lra.cdi.bean.CorrectBean;
import io.narayana.lra.cdi.bean.LeaveWithoutPutBean;
import io.narayana.lra.cdi.bean.MultiForgetBean;
import io.narayana.lra.cdi.bean.NoPostOrGetBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.jaxrs.JAXRSFraction;

import com.google.common.collect.Lists;

/**
 * Test case which checks functionality of CDI extension by deploying wrongly
 * composed LRA components and expect an deployment exception to be thrown.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class StartCdiCheckIT {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

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
        checkSwarmWithDeploymentException("should use complementary annotation.*(POST|GET)",
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
    public void allCorrect() throws Exception {
        File logFile = tmpFolder.newFile();
        Swarm swarm = new Swarm(getLoggingArgs(logFile))
            .fraction(new JAXRSFraction())
            .fraction(new CDIFraction())
            .start();

        try {
            swarm.deploy(getBaseDeployment().addClasses(CorrectBean.class));
        } finally {
            swarm.stop();
        }
    }

    private static List<String> loggingArgs = Arrays.asList(new String[] {
            "-Dswarm.logging.periodic-rotating-file-handlers=FILE",
            "-Dswarm.logging.periodic-rotating-file-handlers.FILE.file.path=%s",
            "-Dswarm.logging.root-logger.handlers=[CONSOLE,FILE]"
        });

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

    private String[] getLoggingArgs(final File logFile) {
        return Lists.transform(loggingArgs, inputString -> {
           String outString = inputString;
           if(inputString.contains("path")) outString = String.format(inputString, logFile.getAbsolutePath());
           return outString;
        }).toArray(new String[]{});
    }

    private void checkSwarmWithDeploymentException(String stringToMatch, Class<?>... classesToAdd) throws Exception {
        File logFile = tmpFolder.newFile();

        Swarm swarm = new Swarm(getLoggingArgs(logFile))
            .fraction(new JAXRSFraction())
            .fraction(new CDIFraction())
            .start();

        try {
            swarm
                .deploy(getBaseDeployment().addClasses(classesToAdd));
            Assert.fail("Expected deployment exception to be thrown");
        } catch (org.wildfly.swarm.container.DeploymentException de) {
            // expected
        } finally {
            swarm.stop();
        }

        assertLogLine(logFile, stringToMatch);
    }

    private void assertLogLine(File file, String expectedString) throws IOException {
        Assert.assertNotNull("file", file);
        boolean isContain = Files.readAllLines(file.toPath()).stream()
            .anyMatch(line -> line.matches(".*" + expectedString + ".*"));
        Assert.assertTrue("Log file does not contain '" + expectedString + "'", isContain);
    }
}
