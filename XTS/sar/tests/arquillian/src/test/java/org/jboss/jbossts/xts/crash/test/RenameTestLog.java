package org.jboss.jbossts.xts.crash.test;

import java.io.File;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class RenameTestLog {
	public static String testName = null;
	public static String scriptName = null;

	@Test
	public void rename() {
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
