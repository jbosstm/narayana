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

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Common methods for crash recovery test cases.
 *
 * @author Ivo Studensky (istudens@redhat.com)
 */
public abstract class AbstractCrashRecoveryTests extends AbstractBasicTests {
    private static Logger log = Logger.getLogger(AbstractCrashRecoveryTests.class);


    /**
     * Instrumentation to be done on server reboot.
     * For details see @link #rebootServer.
     * Note: Before each server restart it is necessary to store the instrumentation into a file and then start up the server
     * with that file as a parameter for byteman to ensure the appropriate classes are instrumented once the server is up.
     *
     * @throws Exception
     */
    protected abstract void instrumentationOnServerReboot() throws Exception;


    protected void cleanTxStore() throws Exception {
        String jbossHome = System.getenv("JBOSS_HOME");
        removeContents(new File(jbossHome, "standalone/data/tx-object-store/"));
    }


    protected void rebootServer(ContainerController controller) throws Exception {

        instrumentor.removeLocalState();
        File rulesFile = File.createTempFile("jbosstxbridgetests", ".btm");
        rulesFile.deleteOnExit();
        instrumentor.setRedirectedSubmissionsFile(rulesFile);

        instrumentationOnServerReboot();

        // just let Arquillian know that server has been killed
        // note: in fact the server has been killed by byteman before
        controller.kill(CONTAINER);

        // start up the server
        String javaVmArguments = System.getProperty("server.jvm.args").trim();
        javaVmArguments = javaVmArguments.replaceFirst("byteman-dtest.jar", "byteman-dtest.jar,script:" + rulesFile.getCanonicalPath());
        log.trace("javaVmArguments = " + javaVmArguments);
        controller.start(CONTAINER, new Config().add("javaVmArguments", javaVmArguments).map());
    }


    /////////////////

    // stolen from CrashRecoveryDelays - should probably just add that to the classpath?
    // prod the recovery manager via its socket. This avoid any sleep delay.
    protected static void doRecovery() throws InterruptedException {
        int port = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryPort();
        String host = recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryAddress();

        log.info("doRecovery#host = " + host);
        log.info("doRecovery#port = " + port);

        BufferedReader in = null;
        PrintStream out = null;
        Socket sckt = null;

        try {
            sckt = new Socket(host, port);

            in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
            out = new PrintStream(sckt.getOutputStream());

            // Output ping message
            out.println("SCAN");
            out.flush();

            // Receive pong message
            String inMessage = in.readLine();

            log.trace("inMessage = " + inMessage);
            if (!inMessage.equals("DONE")) {
                log.error("Recovery failed with message: " + inMessage);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }

                sckt.close();
            } catch (Exception e) {
            }
        }
    }

    // stolen from EmptyObjectStore.java
    protected static void removeContents(File directory) {
        if ((directory != null) &&
                directory.isDirectory() &&
                (!directory.getName().equals("")) &&
                (!directory.getName().equals("/")) &&
                (!directory.getName().equals("\\")) &&
                (!directory.getName().equals(".")) &&
                (!directory.getName().equals(".."))) {
            File[] contents = directory.listFiles();

            for (int index = 0; index < contents.length; index++) {
                if (contents[index].isDirectory()) {
                    removeContents(contents[index]);

                    //System.err.println("Deleted: " + contents[index]);
                    contents[index].delete();
                } else {
                    log.info("Deleted: " + contents[index]);
                    contents[index].delete();
                }
            }
        }
    }

}
