package org.jboss.jbossts.xts.crash.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class BaseCrashTest {
	public static String scriptDir = "target/test-classes/scripts/";

	public static void deleteTestLog() {
		// delete byteman testlog
		File file = new File("testlog");
		if(file.isFile() && file.exists()){
			file.delete();
		}
	}

	public static void copyBytemanScript(String fileName) 
	throws Exception {
		File dest = new File(scriptDir + "Running.txt");
		File source = new File(scriptDir + fileName);

		FileInputStream input = new FileInputStream(source);
		try {		
			FileOutputStream output = new FileOutputStream(dest);

			try {
				byte[] buffer = new byte[4096];
				int n = 0;
				while (-1 != (n = input.read(buffer))) {
					output.write(buffer, 0, n);
				}
			} finally {
				try {
					if (output != null) {
						output.close();
					}
				} catch (IOException ioe) {
					// ignore
				}
			}
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ioe) {
				// ignore
			}
		}
	}
}
