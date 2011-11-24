package com.arjuna.qa.junit;

import java.io.File;

import org.junit.After;
import org.junit.Before;

public class BaseCrashTest {
	protected String XTSServiceTest = " -Dorg.jboss.jbossts.xts.servicetests.XTSServiceTestName=@TestName@";
	protected String BytemanArgs = "-Djboss.modules.system.pkgs=org.jboss.byteman -Dorg.jboss.byteman.transform.all -javaagent:target/test-classes/lib/byteman.jar=script:target/test-classes/scripts/@BMScript@.txt,boot:target/test-classes/lib/byteman.jar,listener:true";
	protected String javaVmArguments;
	protected String testName;
	protected String scriptName;
	
	@Before
	public void setUp() {
		javaVmArguments = BytemanArgs.replace("@BMScript@", scriptName);
		
		File file = new File("testlog");
		if(file.isFile() && file.exists()){
			file.delete();
		}
	}
	
	@After
	public void tearDown() {
		String log = "target/log";

		if(testName != null && scriptName != null) {
			String logFileName = scriptName + "." + testName;
			File file = new File("testlog");
			File logDir = new File(log);

			if(!logDir.exists()) {
				logDir.mkdirs();
			}

			if(file.isFile() && file.exists()){
				file.renameTo(new File(log+"/"+logFileName));
			}
		}
	}	
}
