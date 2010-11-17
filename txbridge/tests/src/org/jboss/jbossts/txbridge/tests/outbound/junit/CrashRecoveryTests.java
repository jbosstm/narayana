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
package org.jboss.jbossts.txbridge.tests.outbound.junit;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import org.apache.commons.httpclient.HttpMethodBase;
import org.jboss.jbossas.servermanager.Argument;
import org.jboss.jbossas.servermanager.Property;
import org.jboss.jbossas.servermanager.Server;
import org.jboss.jbossas.servermanager.ServerManager;
import org.jboss.jbossts.txbridge.outbound.BridgeXAResource;
import org.jboss.jbossts.txbridge.tests.outbound.client.TestClient;
import org.jboss.jbossts.txbridge.tests.outbound.service.TestServiceImpl;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestDurableParticipant;
import org.jboss.jbossts.txbridge.tests.outbound.utility.TestVolatileParticipant;
import org.junit.*;
import static org.junit.Assert.*;

import org.jboss.byteman.agent.submit.Submit;
import org.jboss.byteman.contrib.dtest.*;

import com.arjuna.qa.junit.HttpUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.ConnectException;

/**
 * Crash Recovery test cases for the outbound side of the transaction bridge.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-05
 */

public class CrashRecoveryTests
{
    private static final String baseURL = "http://localhost:8080/txbridge-outbound-tests-client/testclient";

    private static Instrumentor instrumentor;
    private InstrumentedClass instrumentedTestVolatileParticipant;
    private InstrumentedClass instrumentedTestDurableParticipant;

    private static final ServerManager manager = new ServerManager(); // ASTestConfig.java/ServerTask.java
    private static final Argument bytemanArgument = new Argument();

    private static String jboss_home;
    private static String java_home;
    private static String byteman_home;

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
        

        Server server = new Server();
        server.setName("default");

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

    @Before
    public void setUp() throws Exception {

        bytemanArgument.setValue("-javaagent:"+byteman_home+"/byteman.jar=port:9091,listener:true,sys:"+byteman_home+"/byteman-dtest.jar");
        removeContents(new File(jboss_home, "server/default/data/tx-object-store/"));


        // TODO: fix JMXAdapter leak.
        manager.getServer("default").setServerConnection(null);
        manager.startServer("default");

        //instrumentor.installHelperJar("/home/jhalli/IdeaProjects/jboss/byteman_trunk/contrib/dtest/build/lib/byteman-dtest.jar");
        instrumentor.setRedirectedSubmissionsFile(null);

        instrumentedTestVolatileParticipant = instrumentor.instrumentClass(TestVolatileParticipant.class);
        instrumentedTestDurableParticipant = instrumentor.instrumentClass(TestDurableParticipant.class);

        instrumentor.injectOnCall(TestServiceImpl.class, "doNothing", "$0.enlistVolatileParticipant(1), $0.enlistDurableParticipant(1)");
    }

    @After
    public void tearDown() throws Exception {
        instrumentor.removeAllInstrumentation();

        manager.stopServer("default");
    }

    private void execute(boolean expectResponse) throws Exception {

        HttpMethodBase request = null;

        try {
            request = HttpUtils.accessURL(new URL(baseURL));
        } catch(ConnectException e) {
            if(expectResponse) {
                throw e;
            }
        } catch(SocketException e) {
            if(expectResponse) {
                throw e;
            }
        }

        if(expectResponse) {
            String response = request.getResponseBodyAsString().trim();
            assertEquals("finished", response);
        }
    }

    private void rebootServer() throws Exception {

        instrumentor.removeLocalState();
        File rulesFile = new File("/tmp/bar3");
        rulesFile.delete();
        instrumentor.setRedirectedSubmissionsFile(rulesFile);
        bytemanArgument.setValue(bytemanArgument.getValue()+",script:"+rulesFile.getCanonicalPath());

        instrumentedTestVolatileParticipant = instrumentor.instrumentClass(TestVolatileParticipant.class);
        instrumentedTestDurableParticipant = instrumentor.instrumentClass(TestDurableParticipant.class);

        manager.getServer("default").setServerConnection(null);
        Thread.sleep(2000);
        manager.startServer("default");
    }

    @Test
    public void testCrashOneLog() throws Exception {

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$1 = true"); // shouldCommit=true

        instrumentor.crashAtMethodExit("^XTSATRecoveryManager", "writeParticipantRecoveryRecord");

        execute(false);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");

        rebootServer();

        doRecovery();
        doRecovery();

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    @Test
    public void testCrashTwoLogs() throws Exception {

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$1 = true"); // shouldCommit=true

        instrumentor.crashAtMethodExit(BridgeXAResource.class, "prepare");

        execute(false);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");

        rebootServer();

        doRecovery();
        doRecovery();

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
    }

    // this one requires <property name="commitOnePhase">false</property> on CoordinatorEnvironmentBean
    //@Test
    public void testCrashThreeLogs() throws Exception {

        instrumentor.injectOnCall(TestClient.class,  "terminateTransaction", "$1 = true"); // shouldCommit=true

        instrumentor.crashAtMethodEntry(BridgeXAResource.class, "commit");

        execute(false);

        instrumentedTestVolatileParticipant.assertKnownInstances(1);
        instrumentedTestVolatileParticipant.assertMethodCalled("prepare");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("commit");
        instrumentedTestVolatileParticipant.assertMethodNotCalled("rollback");

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("commit");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");

        rebootServer();

        doRecovery();
        doRecovery();

        instrumentedTestDurableParticipant.assertKnownInstances(1);
        instrumentedTestDurableParticipant.assertMethodNotCalled("prepare");
        instrumentedTestDurableParticipant.assertMethodNotCalled("rollback");
        instrumentedTestDurableParticipant.assertMethodCalled("commit");
    }

    /////////////////

    // stolen from CrashRecoveryDelays - should probably just add that to the classpath?
    // prod the recovery manager via its socket. This avoid any sleep delay.
    private static void doRecovery() throws InterruptedException
    {
        int port = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryPort();
        String host = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryAddress();

        BufferedReader in = null;
        PrintStream out = null;
        Socket sckt = null;

        try
        {
            sckt = new Socket(host,port);

            in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
            out = new PrintStream(sckt.getOutputStream());

            // Output ping message
            out.println("SCAN");
            out.flush();

            // Receive pong message
            String inMessage = in.readLine();

            if(!inMessage.equals("DONE")) {
                System.err.println("Recovery failed with message: "+inMessage);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            try {
                if ( in != null )
                {
                    in.close();
                }

                if ( out != null )
                {
                    out.close();
                }

                sckt.close();
            } catch(Exception e) {}
        }
   }

    // stolen from EmptyObjectStore.java
    public static void removeContents(File directory)
    {
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals("..")))
        {
            File[] contents = directory.listFiles();

            for (int index = 0; index < contents.length; index++)
            {
                if (contents[index].isDirectory())
                {
                    removeContents(contents[index]);

                    //System.err.println("Deleted: " + contents[index]);
                    contents[index].delete();
                }
                else
                {
                    System.err.println("Deleted: " + contents[index]);
                    contents[index].delete();
                }
            }
        }
    }
}
