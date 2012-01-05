package com.arjuna.demo.recoverymodule;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.*;

public class TestRecoveryModule {
	public static void main(String args[]) {
		try {
			AtomicAction tx = new AtomicAction();
			tx.begin(); // Top level begin

			// enlist the participant
			tx.add(SimpleRecord.create());

			System.out.println("About to complete the transaction ");
			for (int i = 0; i < args.length; i++) {
				if ((args[i].compareTo("-commit") == 0))
					_commit = true;
				if ((args[i].compareTo("-rollback") == 0))
					_commit = false;
				if ((args[i].compareTo("-crash") == 0))
					_crash = true;
			}
			if (_commit)
				tx.commit(); // Top level commit
			else
				tx.abort(); // Top level rollback
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static boolean _commit = true;
	protected static boolean _crash = false;
}