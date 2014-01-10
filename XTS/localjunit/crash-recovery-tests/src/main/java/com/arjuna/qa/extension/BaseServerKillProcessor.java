package com.arjuna.qa.extension;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ServerKillProcessor;

/**
 * JBoss Server kill processor base class.
 * @author <a href="mailto:hhovsepy@redhat.com">Hayk Hovsepyan</a>
 */
public abstract class BaseServerKillProcessor implements ServerKillProcessor {

    protected final int checkPeriodMillis = 10 * 1000;
    protected final int numChecks = 60;
    
    protected static int processLogId = 0;


    protected abstract String runShellCommand(String cmd) throws Exception;
    
    protected abstract Logger getLogger();
    
    protected abstract String getJBossAliveCmd();
    
    protected abstract String getDefunctJavaCmd();
    
    protected abstract String getShutdownJBossCmd();
    
    protected abstract String getProcessesCmd();
    
    @Override
    public void kill(Container container) throws Exception {
    	getLogger().info("waiting for byteman to kill the server");

        for (int i = 0; i < numChecks; i++) {

            if (jbossIsAlive()) {
                Thread.sleep(checkPeriodMillis);
                getLogger().info("jboss-as is still alive, sleeping for a further " + checkPeriodMillis + "ms");
            } else if (isDefunctJavaProcess()) {
            	getLogger().info("Found a defunct java process, sleeping for a further " + checkPeriodMillis + "ms");
                dumpProcesses(container);
            } else {
            	getLogger().info("jboss-as killed by byteman scirpt");
                dumpProcesses(container);
                return;
            }
        }

        //We've waited long enough for Byteman to kill the server and it has not yet done it.
        // Kill the server manually and fail the test
        shutdownJBoss();
        throw new RuntimeException("jboss-as was not killed by Byteman, this indicates a test failure");
    }

    protected boolean jbossIsAlive() throws Exception {
        //Command will 'res != null' if jboss is not running and 'res == null' if it is
        return !isEmpty(runShellCommand(getJBossAliveCmd()));
    }
    
    protected boolean isDefunctJavaProcess() throws Exception {
        //Command will 'res != null' if a defunct java process is not running and 'res == null' if there is
    	return !isEmpty(runShellCommand(getDefunctJavaCmd()));
    }

    protected void shutdownJBoss() throws Exception {
    	String res = runShellCommand(getJBossAliveCmd());
    	if (res != null && !res.isEmpty()) {
    		String[] splitLine = res.split("\\s+");
    		if (splitLine.length != 1) {
                String pid = splitLine[(splitLine.length) - 1];
                runShellCommand(String.format(getShutdownJBossCmd(), pid));	
            }
    	}

        // wait 5 * 60 second for jboss-as shutdown complete
        for (int i = 0; i < numChecks; i++) {

            if (jbossIsAlive()) {
                Thread.sleep(5000);
            } else {
            	getLogger().info("jboss-as shutdown after sending shutdown command");
                return;
            }
        }
    }

    protected String dumpStream(String msg, InputStream is) {
        try {
            BufferedReader ein = new BufferedReader(new InputStreamReader(is));
            List<String> lines = new LinkedList<String>();
            String line;
            while ( (line = ein.readLine()) != null) {
            	lines.add(line);
            }

            is.close();
            if (!lines.isEmpty()) {
            	String res = lines.get(0);
                System.out.printf("%s %s\n", msg, res);
                getLogger().info("Execution result: '" + res + "'");
                return res;
            }
        } catch (IOException e) {
        	getLogger().info("Exception dumping stream: " + e.getMessage());
        }
        return null;
    }
    
    protected void dumpProcesses(Container container) throws Exception {
        Map<String, String> config = container.getContainerConfiguration().getContainerProperties();
        String testClass = config.get("testClass");
        String scriptName = config.get("scriptName");

        String dir = "target" + File.separator + "surefire-reports" + File.separator + "processes";
        runShellCommand("mkdir " + dir);

        String logFile = dir + File.separator + scriptName + ":" + testClass + "_" + processLogId++ + ".txt";
        runShellCommand(getProcessesCmd() + " > " + logFile);
        getLogger().info("Logged current running processes to: " + logFile);
    }
    
    public boolean isEmpty(String res) {
    	return res == null || res.isEmpty();
    }
}
