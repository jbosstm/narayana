package com.arjuna.qa.extension;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ServerKillProcessor;

public class JBossAS7ServerKillProcessor implements ServerKillProcessor {
	private final Logger log = Logger.getLogger(
			JBossAS7ServerKillProcessor.class.getName());
	private static String killSequence = "[jbossHome]/bin/jboss-cli.[suffix] --commands=connect,quit";
	private static String shutdownSequence = "[jbossHome]/bin/jboss-cli.[suffix] --connect command=:shutdown";
	private int checkDurableTime = 10;
	private int numofCheck = 60;

	@Override
	public void kill(Container container) throws Exception {
		log.info("waiting for byteman to kill server");
		String jbossHome = System.getenv().get("JBOSS_HOME");
		if(jbossHome == null) {
			jbossHome = container.getContainerConfiguration().getContainerProperties().get("jbossHome");
		}
		killSequence = killSequence.replace("[jbossHome]", jbossHome);
		shutdownSequence = shutdownSequence.replace("[jbossHome]", jbossHome);

		String suffix;
		String os = System.getProperty("os.name").toLowerCase();
		if(os.indexOf("windows") > -1) {
			suffix = "bat";
		} else {
			suffix = "sh";
		}
		killSequence = killSequence.replace("[suffix]", suffix);
		shutdownSequence = shutdownSequence.replace("[suffix]", suffix);
		
		int checkn = 0;
		boolean killed = false;
		do {
			if(checkJBossAlive()) {
				Thread.sleep(checkDurableTime * 1000);
				log.info("jboss-as is alive");
			} else {
				killed = true;
				break;
			}
			checkn ++;
		} while(checkn < numofCheck);
		
		if(killed) {
			log.info("jboss-as killed by byteman scirpt");
		} else {
			String env = System.getenv().get("CLI_IPV6_OPTS");
			log.info("jboss-as not killed and shutdown");
			Process p = Runtime.getRuntime().exec(shutdownSequence, new String[] {"JAVA_OPTS="+env});
			p.waitFor();
			p.destroy();
			// wait 5 * 60 second for jboss-as shutdown complete
			int checkn_s = 0;
			do {
				if(checkJBossAlive()) {
					Thread.sleep(5000);
				} else {
					log.info("jboss-as shutdown");
					break;
				}
				checkn_s ++;
			} while (checkn_s < 60);
			throw new RuntimeException("jboss-as not killed and shutdown");
		}
	}
	
	private boolean checkJBossAlive() throws Exception {
		String env = System.getenv().get("CLI_IPV6_OPTS");
		Process p = Runtime.getRuntime().exec(killSequence, new String[] {"JAVA_OPTS="+env});
		p.waitFor();
		int rc = p.exitValue();

		if (rc != 0 && rc != 1) {
			p.destroy();
			throw new RuntimeException("Kill Sequence failed");
		}
		
		InputStream out = p.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(out));
		String result= in.readLine();
		out.close();
		p.destroy();
		
		return !(result != null && result.contains("The controller is not available"));
	}
}
