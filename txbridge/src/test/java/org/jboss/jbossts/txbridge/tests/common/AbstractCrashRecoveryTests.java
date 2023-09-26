/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.txbridge.tests.common;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import org.jboss.arquillian.container.test.api.Config;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.byteman.contrib.dtest.Instrumentor;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common methods for crash recovery test cases.
 *
 * @author Ivo Studensky (istudens@redhat.com)
 */
public abstract class AbstractCrashRecoveryTests extends AbstractBasicTests implements OnServerRebootInstrumentator {
    private static Logger log = Logger.getLogger(AbstractCrashRecoveryTests.class);

    @Override
    public abstract void instrument(Instrumentor instrumentor) throws Exception;

    protected void cleanTxStore() throws Exception {
        String jbossHome = System.getenv("JBOSS_HOME");
        removeContents(new File(jbossHome, "standalone/data/tx-object-store/"));
    }

    protected void rebootServer(ContainerController controller, OnServerRebootInstrumentator onServerRebootInstrumentator) throws Exception {

        instrumentor.removeLocalState();
        File rulesFile = File.createTempFile("jbosstxbridgetests", "");
        rulesFile.deleteOnExit();
        instrumentor.setRedirectedSubmissionsFile(rulesFile);

        onServerRebootInstrumentator.instrument(instrumentor);

        // just let Arquillian know that server has been killed
        // note: in fact the server has been killed by byteman before
        controller.kill(CONTAINER);

        // start up the server
        String javaVmArguments = System.getProperty("server.jvm.args").trim();
        javaVmArguments = javaVmArguments.replaceFirst("byteman-dtest.jar", Matcher.quoteReplacement("byteman-dtest.jar,script:" + rulesFile.getAbsolutePath()));
        log.trace("javaVmArguments = " + javaVmArguments);
        controller.start(CONTAINER, new Config().add("javaVmArguments", javaVmArguments).map());

        // all following instrumentation will be submitted directly to  server
        instrumentor.setRedirectedSubmissionsFile(null);
    }

    protected void rebootServer(ContainerController controller) throws Exception {
        rebootServer(controller, this);
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
            throw new IllegalStateException("Cannot run recovery scan at " + host + ":" + port +
                    ". Please verify if 'recovery-listener' is enabled in the configuration.", ex);
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