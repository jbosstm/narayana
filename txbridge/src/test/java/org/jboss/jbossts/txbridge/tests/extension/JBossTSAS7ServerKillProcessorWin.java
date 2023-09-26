/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.jbossts.txbridge.tests.extension;

import java.util.logging.Logger;

/**
 * JBoss Server kill processor for Windows machines.
 * @author <a href="mailto:hhovsepy@redhat.com">Hayk Hovsepyan</a>
 */
public class JBossTSAS7ServerKillProcessorWin extends JBossTSBaseServerKillProcessor {

	private static final Logger logger = Logger.getLogger(JBossTSAS7ServerKillProcessorWin.class.getName());
	private static final String PS_AUX_CMD = JBossTSServerExtension.OSType.WINDOWS.getPSCommand();
	private static final String CHECK_JBOSS_ALIVE_CMD = PS_AUX_CMD + " | findstr jboss-module | findstr /v findstr"; //skip "findstr" from output, windows workaround
	private static final String CHECK_FOR_DEFUNCT_JAVA_CMD = PS_AUX_CMD + " | findstr defunct | findstr /v findstr"; //skip "findstr" from output, windows workaround
	private static final String SHUTDOWN_JBOSS_CMD = "taskkill /F /T /PID %s";
    
    @Override
    protected String runShellCommand(String cmd) throws Exception {
        getLogger().info("Executing shell command: '" + cmd + "'");
        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", cmd);
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
