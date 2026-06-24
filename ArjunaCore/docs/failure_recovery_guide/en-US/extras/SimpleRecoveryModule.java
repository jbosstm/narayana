package com.arjuna.demo.recoverymodule;

import com.arjuna.ats.arjuna.recovery.RecoveryModule;

public class SimpleRecoveryModule implements RecoveryModule {
	public String filename = "c:/tmp/RecordState";

	public SimpleRecoveryModule() {
		System.out
				.println("The SimpleRecoveryModule is loaded");
	}

	public void periodicWorkFirstPass() {
		try {
			java.io.FileInputStream file = new java.io.FileInputStream(
					filename);
			java.io.InputStreamReader input = new java.io.InputStreamReader(
					file);
			java.io.BufferedReader reader = new java.io.BufferedReader(
					input);
			String stringState = reader.readLine();
			if (stringState.compareTo("I'm prepared") == 0)
				System.out
						.println("The transaction is in the prepared state");
			file.close();
		} catch (java.io.IOException ex) {
			System.out.println("Nothing found on the Disk");
		}
	}

	public void periodicWorkSecondPass() {
		try {
			java.io.FileInputStream file = new java.io.FileInputStream(
					filename);
			java.io.InputStreamReader input = new java.io.InputStreamReader(
					file);
			java.io.BufferedReader reader = new java.io.BufferedReader(
					input);
			String stringState = reader.readLine();
			if (stringState.compareTo("I'm prepared") == 0) {
				System.out
						.println("The record is still in the prepared state");
				System.out.println("– Recovery is needed");
			} else if (stringState
					.compareTo("I'm Committed") == 0) {
				System.out
						.println("The transaction has completed and committed");
			}
			file.close();
		} catch (java.io.IOException ex) {
			System.out.println("Nothing found on the Disk");
			System.out
					.println("Either there was no transaction");
			System.out.println("or it as been rolled back");
		}
	}
}