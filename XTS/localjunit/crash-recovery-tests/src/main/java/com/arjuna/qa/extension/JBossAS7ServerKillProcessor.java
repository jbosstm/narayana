/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.extension;

import java.util.logging.Logger;

/**
 * JBoss Server kill processor for Linux/Unix machines.
 * @author <a href="mailto:hhovsepy@redhat.com">Hayk Hovsepyan</a>
 */
public class JBossAS7ServerKillProcessor extends BaseServerKillProcessor {

    private static final Logger logger = Logger.getLogger(JBossAS7ServerKillProcessor.class.getName());
    private static final String PROCESSES_CMD = ServerExtension.OSType.getOSType().getPSCommand();
    private static final String PS_AUX_CMD = ServerExtension.isSolaris() ? "/usr/ucb/ps aux" : "ps aux";
    private static final String CHECK_JBOSS_ALIVE_CMD = "if [ \"x`" + PROCESSES_CMD + " | grep 'jboss-module[s]'`\" = \"x\" ]; then exit 1; fi";
    private static final String SHUTDOWN_JBOSS_CMD = PROCESSES_CMD + " | grep jboss-module[s] | awk '" + ServerExtension.OSType.getOSType().getPSIDIndex() + "' | xargs kill";
    private static final String CHECK_FOR_DEFUNCT_JAVA_CMD = "if [ \"x`" + PS_AUX_CMD + " | grep '\\[java\\] <defunct>'`\" = \"x\" ]; then exit 1; fi";  
    
    @Override
    protected boolean jbossIsAlive() throws Exception {
        //Command will 'exit 1' if jboss is not running and 'exit 0' if it is.
    	return runShellCommandExitCode(getJBossAliveCmd()) == 0;
    }
    
    @Override
    protected boolean isDefunctJavaProcess() throws Exception {
        //Command will 'exit 1' if a defunct java process is not running and 'exit 0' if there is.
        return runShellCommandExitCode(getDefunctJavaCmd()) == 0;
    }

    @Override
    protected void shutdownJBoss() throws Exception {
    	runShellCommand(getShutdownJBossCmd());

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
    
    private int runShellCommandExitCode(String cmd) throws Exception {
        getLogger().info("Executing shell command: '" + cmd + "'");
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        Process p = pb.start();
        
        dumpStream("std out", p.getInputStream());
        dumpStream("std error", p.getErrorStream());
    
        p.waitFor();
	        
        p.destroy();

        return p.exitValue();
    }

	@Override
	protected String runShellCommand(String cmd) throws Exception {
		getLogger().info("Executing shell command: '" + cmd + "'");
        ProcessBuilder pb = new ProcessBuilder("/bin/sh", "-c", cmd);
        Process p = pb.start();
        String res = dumpStream("std out", p.getInputStream());
        dumpStream("std error", p.getErrorStream());
    
        p.waitFor();
	        
        p.destroy();

        return res;
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected String getJBossAliveCmd() {
		return CHECK_JBOSS_ALIVE_CMD;
	}

	@Override
	protected String getDefunctJavaCmd() {
		return CHECK_FOR_DEFUNCT_JAVA_CMD;
	}

	@Override
	protected String getShutdownJBossCmd() {
		return SHUTDOWN_JBOSS_CMD;
	}

	@Override
	protected String getProcessesCmd() {
		return PS_AUX_CMD;
	}
}
