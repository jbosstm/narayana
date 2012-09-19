package com.arjuna.qa.extension;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ServerKillProcessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

public class JBossAS7ServerKillProcessor implements ServerKillProcessor {

    private static final Logger logger = Logger.getLogger(JBossAS7ServerKillProcessor.class.getName());
    private static final String CHECK_JBOSS_ALIVE_CMD = "if [ \"$(jps | grep jboss-modules.jar)\" == \"\" ]; then exit 1; fi";
    private static final String SHUTDOWN_JBOSS_CMD = "jps | grep jboss-modules.jar | awk '{ print $1 }' | xargs kill";

    private int checkPeriodMillis = 10 * 1000;
    private int numChecks = 60;

    private static int processLogId = 0;

    @Override
    public void kill(Container container) throws Exception {
        logger.info("waiting for byteman to kill the server");

        for (int i = 0; i < numChecks; i++) {

            if (jbossIsAlive()) {
                Thread.sleep(checkPeriodMillis);
                logger.info("jboss-as is still alive, sleeping for a further " + checkPeriodMillis + "ms");
            } else {
                logger.info("jboss-as killed by byteman scirpt");
                dumpProcesses(container);
                return;
            }
        }

        //We've waited long enough for Byteman to kil the server and it has not yet done it.
        // Kill the server manually and fail the test
        shutdownJBoss();
        throw new RuntimeException("jboss-as was not killed by Byteman, this indicates a test failure");
    }

    private boolean jbossIsAlive() throws Exception {
        int exitCode = runShellCommand(CHECK_JBOSS_ALIVE_CMD);

        //Command will 'exit 1' if jboss is not running adn 'exit 0' if it is
        return exitCode == 0;
    }

    private void shutdownJBoss() throws Exception {
        runShellCommand(SHUTDOWN_JBOSS_CMD);

        // wait 5 * 60 second for jboss-as shutdown complete
        for (int i = 0; i < 60; i++) {

            if (jbossIsAlive()) {
                Thread.sleep(5000);
            } else {
                logger.info("jboss-as shutdown after sending shutdown command");
                return;
            }
        }
    }

    private int runShellCommand(String cmd) throws Exception {
        logger.info("Executing shell command: '" + cmd + "'");
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        Process p = pb.start();
        p.waitFor();

        dumpStream("std out", p.getInputStream());
        dumpStream("std error", p.getErrorStream());

        p.destroy();

        return p.exitValue();
    }

    private void dumpStream(String msg, InputStream is) {
        try {
            BufferedReader ein = new BufferedReader(new InputStreamReader(is));
            String res = ein.readLine();
            is.close();
            if (res != null)
            {
                System.out.printf("%s %s\n", msg, res);
            }
        } catch (IOException e) {
            logger.info("Exception dumping stream: " + e.getMessage());
        }
    }

    public void dumpProcesses(Container container) throws Exception
    {
        Map<String, String> config = container.getContainerConfiguration().getContainerProperties();
        String testClass = config.get("testClass");
        String scriptName = config.get("scriptName");

        String dir = "./target/surefire-reports/processes";
        runShellCommand("mkdir -p " + dir);

        String logFile = dir + "/" + scriptName + ":" + testClass + "_" + processLogId++ + ".txt";
        runShellCommand("ps aux > " + logFile);
        logger.info("Logged current running processes to: " + logFile);
    }
}
