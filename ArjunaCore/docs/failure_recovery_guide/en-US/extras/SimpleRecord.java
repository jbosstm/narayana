package com.arjuna.demo.recoverymodule;

import com.arjuna.ats.arjuna.coordinator.*;
import java.io.File;

public class SimpleRecord extends AbstractRecord {
	public String filename = "c:/tmp/RecordState";

	public SimpleRecord() {
		System.out.println("Creating new resource");
	}

	public static AbstractRecord create() {
		return new SimpleRecord();
	}

	public int topLevelAbort() {
		try {
			File fd = new File(filename);
			if (fd.exists()) {
				if (fd.delete())
					System.out.println("File Deleted");
			}
		} catch (Exception ex) {
			// …
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelCommit() {
		if (TestRecoveryModule._crash)
			System.exit(0);
		try {
			java.io.FileOutputStream file = new java.io.FileOutputStream(
					filename);
			java.io.PrintStream pfile = new java.io.PrintStream(
					file);
			pfile.println("I'm Committed");
			file.close();
		} catch (java.io.IOException ex) {
			// ...
		}
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare() {
		try {
			java.io.FileOutputStream file = new java.io.FileOutputStream(
					filename);
			java.io.PrintStream pfile = new java.io.PrintStream(
					file);
			pfile.println("I'm prepared");
			file.close();
		} catch (java.io.IOException ex) {
			// ...
		}
		return TwoPhaseOutcome.PREPARE_OK;
	}
	// …
}