package com.arjuna.qa.extension;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ServerKillProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

public class JBossAS7ServerKillProcessorWin implements ServerKillProcessor {

    private static final Logger logger = Logger.getLogger(JBossAS7ServerKillProcessorWin.class.getName());
    private static final String CHECK_JBOSS_ALIVE_CMD = "wmic PROCESS GET Name,ProcessId | findstr jboss-module";
    private static final String CHECK_FOR_DEFUNCT_JAVA_CMD = "wmic PROCESS GET Name,ProcessId | findstr defunct";
    private static final String SHUTDOWN_JBOSS_CMD = "taskkill /F /T /PID %s";

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
            } else if (isDefunctJavaProcess()) {
                logger.info("Found a defunct java process, sleeping for a further " + checkPeriodMillis + "ms");
                dumpProcesses(container);
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
        String res = runShellCommand(CHECK_JBOSS_ALIVE_CMD);

        //Command will 'res != null' if jboss is not running and 'res == null' if it is
        return res != null && !res.isEmpty();
    }
    
    private boolean isDefunctJavaProcess() throws Exception {
    	String res = runShellCommand(CHECK_FOR_DEFUNCT_JAVA_CMD);

        //Command will 'res != null' if a defunct java process is not running and 'res == null' if there is
    	return res != null && !res.isEmpty();
    }

    private void shutdownJBoss() throws Exception {
    	String res = runShellCommand(CHECK_JBOSS_ALIVE_CMD);
    	if (res != null && !res.isEmpty()) {
    		String[] splitLine = res.split("\\s+");
    		if (splitLine.length != 1) {
                String pid = splitLine[(splitLine.length) - 1];
                runShellCommand(String.format(SHUTDOWN_JBOSS_CMD, pid));	
            }
    	}

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

    private String runShellCommand(String cmd) throws Exception {
        logger.info("Executing shell command: '" + cmd + "'");
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", cmd);
        Process p = pb.start();
        p.waitFor();

        String res = dumpStream("std out", p.getInputStream());
        dumpStream("std error", p.getErrorStream());

        p.destroy();

        return res;
    }

    private String dumpStream(String msg, InputStream is) {
        try {
            BufferedReader ein = new BufferedReader(new InputStreamReader(is));
            String res = ein.readLine();
            is.close();
            if (res != null)
            {
                System.out.printf("%s %s\n", msg, res);
                return res;
            }
        } catch (IOException e) {
            logger.info("Exception dumping stream: " + e.getMessage());
        }
        return null;
    }
    
    public void dumpProcesses(Container container) throws Exception
    {
        Map<String, String> config = container.getContainerConfiguration().getContainerProperties();
        String testClass = config.get("testClass");
        String scriptName = config.get("scriptName");

        String dir = "target" + File.separator + "surefire-reports" + File.separator + "processes";
        runShellCommand("mkdir " + dir);

        String logFile = dir + File.separator + scriptName + ":" + testClass + "_" + processLogId++ + ".txt";
        runShellCommand("wmic PROCESS GET ProcessId,CommandLine,Name > " + logFile);
        logger.info("Logged current running processes to: " + logFile);
    }
}
