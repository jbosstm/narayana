/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.txbridge.tests.common;

import org.jboss.jbossts.txbridge.utils.HttpUtils;
import org.apache.commons.httpclient.HttpMethodBase;
import org.jboss.byteman.agent.submit.Submit;
import org.jboss.byteman.contrib.dtest.BytemanTestHelper;
import org.jboss.byteman.contrib.dtest.Instrumentor;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;
import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.net.ConnectException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * Common methods for tx bridge test cases.
 * TODO: make server name ('default', ...) configurable
 */
public abstract class AbstractBasicTests {

    protected static Instrumentor instrumentor;

    protected static final ServerManager manager = new ServerManager(); // ASTestConfig.java/ServerTask.java
    protected static final Argument bytemanArgument = new Argument();    

    protected static String jboss_home;
    protected static String java_home;
    protected static String byteman_home;
    
    protected static String serverName = "default";

    /*
-Xdebug
-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006
-Xmx1024m
-javaagent:/home/jhalli/IdeaProjects/jboss/byteman_trunk/install/lib/byteman.jar=port:9091,listener:true
-Dorg.jboss.byteman.debug=true
-Dorg.jboss.byteman.verbose=true
-Dorg.jboss.byteman.dump.generated.classes=true
-Dorg.jboss.byteman.dump.generated.classes.dir=dump
-Demma.exit.delay=10
     */
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        
        instrumentor = new Instrumentor(new Submit(), 1199);
        
        jboss_home = System.getProperty("JBOSS_HOME");
        if(jboss_home == null) {
            throw new IllegalStateException("no JBOSS_HOME defined");
        }
        File jbossHomeDirectory = new File(jboss_home);
        if(!jbossHomeDirectory.exists() || !jbossHomeDirectory.isDirectory()) {
            throw new IllegalStateException("invalid JBOSS_HOME");
        }
        manager.setJbossHome(jboss_home);

        java_home = System.getProperty("JAVA_HOME");
        if(java_home == null) {
            throw new IllegalStateException("no JAVA_HOME defined");
        }
        File javaHomeDir = new File(java_home);
        if(!javaHomeDir.exists() || !javaHomeDir.isDirectory()) {
            throw new IllegalStateException("invalid JAVA_HOME");
        }
        manager.setJavaHome(java_home);

        byteman_home = System.getProperty("BYTEMAN_HOME");
        if(byteman_home == null) {
            throw new IllegalStateException("no BYTEMAN_HOME defined");
        }
        File bytemanHomeDir = new File(byteman_home);
        if(!bytemanHomeDir.exists() || !bytemanHomeDir.isDirectory()) {
            throw new IllegalStateException("invalid BYTEMAN_HOME");
        }

        String _serverName = System.getProperty("SERVER_NAME");
        if (_serverName != null && _serverName.length() > 0) {
            serverName = _serverName;
        }

        Server server = new Server();
        server.setName(serverName);

        server.addJvmArg(bytemanArgument);
        Argument arg2 = new Argument();
        arg2.setValue("-Xmx1024m");
        server.addJvmArg(arg2);

        Argument arg3 = new Argument();
        arg3.setValue("-Xdebug");
        server.addJvmArg(arg3);
        Argument arg4 = new Argument();
        arg4.setValue("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006");
        server.addJvmArg(arg4);

        Argument arg5 = new Argument();
        arg5.setValue("-XX:MaxPermSize=256m"); // caution: JVM specific
        server.addJvmArg(arg5);

        Property property1 = new Property();
        property1.setKey("org.jboss.byteman.debug");
        property1.setValue("true");
        server.addSysProperty(property1);
        Property property2 = new Property();
        property2.setKey(BytemanTestHelper.RMIREGISTRY_PORT_PROPERTY_NAME);
        property2.setValue("1199");
        server.addSysProperty(property2);

        Property property3 = new Property();
        property3.setKey("emma.exit.delay");
        property3.setValue("10");
        server.addSysProperty(property3);

        manager.addServer(server);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        Server server = manager.getServer(serverName);
        if (server.isRunning()) {
            manager.stopServer(serverName);
        }
    }

    @Before
    public void setUp() throws Exception {

        bytemanArgument.setValue("-javaagent:"+byteman_home+"/byteman.jar=port:9091,listener:true,sys:"+byteman_home+"/byteman-dtest.jar");

        Server server = manager.getServer(serverName);
        // TODO: fix JMXAdapter leak.
        server.setServerConnection(null);
        if (!server.isRunning()) {
            manager.startServer(serverName);
        }

        //instrumentor.installHelperJar("/home/jhalli/IdeaProjects/jboss/byteman_trunk/contrib/dtest/build/lib/byteman-dtest.jar");
        instrumentor.setRedirectedSubmissionsFile(null);
        
    }

    @After
    public void tearDown() throws Exception {
        instrumentor.removeAllInstrumentation();

        if (restartServerForEachTest()) {
            manager.stopServer(serverName);
        }
    }

    protected boolean restartServerForEachTest() {
        return false;
    }

    protected void execute(String baseURL) throws Exception {
        execute(baseURL, true);
    }
    
    protected void execute(String baseURL, boolean expectResponse) throws Exception {

        HttpMethodBase request = null;

        try {
            request = HttpUtils.accessURL(new URL(baseURL));
        } catch(ConnectException e) {
            if(expectResponse) {
                throw e;
            }
        }

        if(expectResponse) {
            String response = request.getResponseBodyAsString().trim();
            assertEquals("finished", response);
        }
    }

}
