/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2011, Red Hat, Inc., and individual contributors
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
package org.jboss.narayana.osgi.jta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.File;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;

import org.apache.karaf.itests.KarafTestSupport;
import org.jboss.narayana.osgi.jta.internal.OsgiTransactionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.remote.RBCRemoteTargetOptions;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.container.internal.JavaVersionUtil;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import aQute.bnd.osgi.Constants;
import jakarta.transaction.TransactionManager;

/**
 * @author <a href="mailto:zfeng@redhat.com">Amos Feng</a>
 */

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiJTATest extends KarafTestSupport{

    
    @Configuration
    public Option[] config() {
        String httpPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_HTTP_PORT), Integer.parseInt(MAX_HTTP_PORT)));
        String rmiRegistryPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_RMI_REG_PORT), Integer.parseInt(MAX_RMI_REG_PORT)));
        String rmiServerPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_RMI_SERVER_PORT), Integer.parseInt(MAX_RMI_SERVER_PORT)));
        String sshPort = Integer.toString(getAvailablePort(Integer.parseInt(MIN_SSH_PORT), Integer.parseInt(MAX_SSH_PORT)));
        String localRepository = System.getProperty("org.ops4j.pax.url.mvn.localRepository");
        if (localRepository == null) {
            localRepository = "";
        }

        // org.ops4j.pax.exam.spi.PaxExamRuntime.defaultTestSystemOptions() does it implicitly when pax.exam.system = test
        ConfigurationManager cm = new ConfigurationManager();
        String logging = cm.getProperty(org.ops4j.pax.exam.Constants.EXAM_LOGGING_KEY, org.ops4j.pax.exam.Constants.EXAM_LOGGING_PAX_LOGGING);
        Option[] examOptions = new Option[] {
                bootDelegationPackage("sun.*"),
                frameworkStartLevel(org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE),
                url("link:classpath:META-INF/links/org.ops4j.pax.exam.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.extender.service.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),

                when(logging.equals(org.ops4j.pax.exam.Constants.EXAM_LOGGING_PAX_LOGGING)).useOptions(
                        url("link:classpath:META-INF/links/org.ops4j.pax.logging.api.link").startLevel(START_LEVEL_SYSTEM_BUNDLES)),

                url("link:classpath:META-INF/links/org.ops4j.base.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.core.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.extender.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.lifecycle.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.tracker.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
                // but we don't want geronimo atinject...
//                url("link:classpath:META-INF/links/org.apache.geronimo.specs.atinject.link").startLevel(START_LEVEL_SYSTEM_BUNDLES)
                // we want SMX inject 1_3
                url("link:classpath:org.apache.servicemix.bundles.javax-inject.link").startLevel(START_LEVEL_SYSTEM_BUNDLES)
        };

        Option[] testOptions = null;
        if (JavaVersionUtil.getMajorVersion() >= 9) {
            testOptions = new Option[] {
                // debugConfiguration("8889", true),
                KarafDistributionOption.karafDistributionConfiguration().frameworkUrl(getKarafDistribution()).name("Apache Karaf").unpackDirectory(new File("target/exam")),
                // enable JMX RBAC security, thanks to the KarafMBeanServerBuilder
                KarafDistributionOption.configureSecurity().disableKarafMBeanServerBuilder(),
                KarafDistributionOption.configureConsole().ignoreLocalConsole(),
                KarafDistributionOption.keepRuntimeFolder(),
                KarafDistributionOption.logLevel(LogLevel.INFO),
                CoreOptions.systemTimeout(3600000),
                RBCRemoteTargetOptions.waitForRBCFor(3600000),
                CoreOptions.mavenBundle().groupId("org.awaitility").artifactId("awaitility").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.hamcrest").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.karaf.itests").artifactId("common").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("javax.annotation").artifactId("javax.annotation-api").versionAsInProject(),
              CoreOptions.mavenBundle().groupId("jakarta.el").artifactId("jakarta.el-api").versionAsInProject(),
              CoreOptions.mavenBundle().groupId("jakarta.inject").artifactId("jakarta.inject-api").versionAsInProject(),
              CoreOptions.mavenBundle().groupId("jakarta.interceptor").artifactId("jakarta.interceptor-api").versionAsInProject(),
              CoreOptions.mavenBundle().groupId("jakarta.enterprise").artifactId("jakarta.enterprise.cdi-api").versionAsInProject(),
              CoreOptions.mavenBundle().groupId("jakarta.enterprise").artifactId("jakarta.enterprise.lang-model").versionAsInProject(),

              CoreOptions.mavenBundle().groupId("jakarta.transaction").artifactId("jakarta.transaction-api").versionAsInProject(),
                KarafDistributionOption.features("src/main/resources/features.xml", "narayana-osgi-jta"),
                
                KarafDistributionOption.features(CoreOptions.mavenBundle().groupId("org.apache.karaf.features")
                                .artifactId("standard").classifier("features")
                                .version("3.0.1").type("xml"), "scr"),

                //replaceConfigurationFile("etc/host.key", getConfigFile("/etc/host.key")),
                KarafDistributionOption.replaceConfigurationFile("etc/users.properties", getConfigFile("/etc/users.properties")),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.features.cfg", "updateSnapshots", "none"),
                KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", httpPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.localRepository", localRepository),
                KarafDistributionOption.editConfigurationFilePut("etc/branding.properties", "welcome", ""), // No welcome banner
                KarafDistributionOption.editConfigurationFilePut("etc/branding-ssh.properties", "welcome", ""),
                new VMOption("--add-reads=java.xml=java.logging"),
                new VMOption("--add-exports=java.base/org.apache.karaf.specs.locator=java.xml,ALL-UNNAMED"),
                new VMOption("--patch-module"),
                new VMOption("java.base=lib/endorsed/org.apache.karaf.specs.locator-"
                    + System.getProperty("karaf.version") + ".jar"),
                new VMOption("--patch-module"), new VMOption("java.xml=lib/endorsed/org.apache.karaf.specs.java.xml-"
                    + System.getProperty("karaf.version") + ".jar"),
                new VMOption("--add-opens"),
                new VMOption("java.base/java.security=ALL-UNNAMED"),
                new VMOption("--add-opens"),
                new VMOption("java.base/java.net=ALL-UNNAMED"),
                new VMOption("--add-opens"),
                new VMOption("java.base/java.lang=ALL-UNNAMED"),
                new VMOption("--add-opens"),
                new VMOption("java.base/java.util=ALL-UNNAMED"),
                new VMOption("--add-opens"),
                new VMOption("java.naming/javax.naming.spi=ALL-UNNAMED"),
                new VMOption("--add-opens"),
                new VMOption("java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED"),
                new VMOption("--add-exports=java.base/sun.net.www.protocol.file=ALL-UNNAMED"),
                new VMOption("--add-exports=java.base/sun.net.www.protocol.ftp=ALL-UNNAMED"),
                new VMOption("--add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED"),
                new VMOption("--add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED"),
                new VMOption("--add-exports=java.base/sun.net.www.protocol.jar=ALL-UNNAMED"),
                new VMOption("--add-exports=java.base/sun.net.www.content.text=ALL-UNNAMED"),
                new VMOption("--add-exports=jdk.naming.rmi/com.sun.jndi.url.rmi=ALL-UNNAMED"),
                new VMOption("--add-exports=java.rmi/sun.rmi.registry=ALL-UNNAMED"),
                new VMOption("-classpath"),
                new VMOption("lib/jdk9plus/*" + File.pathSeparator + "lib/boot/*"
                    + File.pathSeparator + "lib/endorsed/*")
                
            };
        } else {
            testOptions = new Option[] {
                //debugConfiguration("8889", true),
                KarafDistributionOption.karafDistributionConfiguration().frameworkUrl(getKarafDistribution()).name("Apache Karaf").unpackDirectory(new File("target/exam")),
                // enable JMX RBAC security, thanks to the KarafMBeanServerBuilder
                KarafDistributionOption.configureSecurity().disableKarafMBeanServerBuilder(),
                KarafDistributionOption.configureConsole().ignoreLocalConsole(),
                KarafDistributionOption.keepRuntimeFolder(),
                KarafDistributionOption.logLevel(LogLevel.INFO),
                CoreOptions.systemTimeout(3600000),
                RBCRemoteTargetOptions.waitForRBCFor(3600000),
                CoreOptions.mavenBundle().groupId("org.awaitility").artifactId("awaitility").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.hamcrest").versionAsInProject(),
                CoreOptions.mavenBundle().groupId("org.apache.karaf.itests").artifactId("common").versionAsInProject(),
                //replaceConfigurationFile("etc/host.key", getConfigFile("/etc/host.key")),
                KarafDistributionOption.replaceConfigurationFile("etc/users.properties", getConfigFile("/etc/users.properties")),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.features.cfg", "updateSnapshots", "none"),
                KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port", httpPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiRegistryPort", rmiRegistryPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.management.cfg", "rmiServerPort", rmiServerPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.apache.karaf.shell.cfg", "sshPort", sshPort),
                KarafDistributionOption.editConfigurationFilePut("etc/org.ops4j.pax.url.mvn.cfg", "org.ops4j.pax.url.mvn.localRepository", localRepository),
                KarafDistributionOption.editConfigurationFilePut("etc/branding.properties", "welcome", ""), // No welcome banner
                KarafDistributionOption.editConfigurationFilePut("etc/branding-ssh.properties", "welcome", ""),
            };
        }

        return combine(examOptions, testOptions);
    }
    
    @Test
    public  void testTransactionManager() throws Exception {
//        assertEquals("System Bundle ID", 0, bundleContext.getBundle().getBundleId());
        

        Bundle bundle = bundleContext.getBundle();
        bundle.start();
        assertEquals("Bundle ACTIVE", Bundle.ACTIVE, bundle.getState());

        BundleContext context = bundle.getBundleContext();
        assertNotNull("BundleContext available", context);

        ServiceTracker<TransactionManager, TransactionManager> tracker = new ServiceTracker<>(context, TransactionManager.class, null);
        tracker.open();
        TransactionManager tm = tracker.waitForService(10000);

        if (tm == null) {
            for (Bundle b : context.getBundles()) {
                System.out.println(b.getSymbolicName() + "/" + b.getVersion() + " = " + b.getState());
                Dictionary<String, String> headers = b.getHeaders();
                System.out.println("\tHeaders");
                for (Enumeration<String> e = headers.keys(); e.hasMoreElements(); ) {
                    String key = e.nextElement();
                    String val = headers.get(key);
                    System.out.println("\t\t" + key + " = " + val);
                }
                System.out.println("\tServices");
                ServiceReference[] refs = b.getRegisteredServices();
                if (refs != null) {
                    for (ServiceReference svc : refs) {
                        for (String key : svc.getPropertyKeys()) {
                            Object val = svc.getProperty(key);
                            if (val.getClass().isArray()) {
                                val = Arrays.toString((Object[]) val);
                            }
                            System.out.println("\t\t" + key + " = " + val);
                        }
                    }
                }
            }

            fail("TransactionManager service not available");
        }

        tm.begin();
        tm.commit();

        bundle.stop();
        assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundle.getState());
    }

}
