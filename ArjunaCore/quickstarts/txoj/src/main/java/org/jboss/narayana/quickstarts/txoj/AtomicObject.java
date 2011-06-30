package org.jboss.narayana.quickstarts.txoj;

import java.io.IOException;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

public class AtomicObject extends LockManager {

	private int state;

	public AtomicObject() {
		super();

		state = 0;

		AtomicAction A = new AtomicAction();

		A.begin();

		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			if (A.commit() == ActionStatus.COMMITTED)
				System.out.println("Created persistent object " + get_uid());
			else
				System.out.println("Action.commit error.");
		} else {
			A.abort();

			System.out.println("setlock error.");
		}
	}

	public void incr(int value) throws Exception {
		AtomicAction A = new AtomicAction();

		A.begin();

		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			state += value;

			if (A.commit() != ActionStatus.COMMITTED)
				throw new Exception("Action commit error.");
			else
				return;
		}

		A.abort();

		throw new Exception("Write lock error.");
	}

	public void set(int value) throws Exception {
		AtomicAction A = new AtomicAction();

		A.begin();

		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			state = value;

			if (A.commit() != ActionStatus.COMMITTED)
				throw new Exception("Action commit error.");
			else
				return;
		}

		A.abort();

		throw new Exception("Write lock error.");
	}

	public int get() throws Exception {
		AtomicAction A = new AtomicAction();
		int value = -1;

		A.begin();

		if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED) {
			value = state;

			if (A.commit() == ActionStatus.COMMITTED)
				return value;
			else
				throw new Exception("Action commit error.");
		}

		A.abort();

		throw new Exception("Read lock error.");
	}

	public boolean save_state(OutputObjectState os, int ot) {
		boolean result = super.save_state(os, ot);

		if (!result)
			return false;

		try {
			os.packInt(state);
		} catch (IOException e) {
			result = false;
		}

		return result;
	}

	public boolean restore_state(InputObjectState os, int ot) {
		boolean result = super.restore_state(os, ot);

		if (!result)
			return false;

		try {
			state = os.unpackInt();
		} catch (IOException e) {
			result = false;
		}

		return result;
	}

	public String type() {
		return "/StateManager/LockManager/AtomicObject";
	}

	public static void main(String[] args) throws Exception {
		AtomicObject obj = new AtomicObject();
		AtomicAction a = new AtomicAction();

		a.begin();

		obj.set(1234);

		a.commit();

		if (obj.get() != 1234) {
			throw new RuntimeException("The object was not set to 1234");
		}

		a = new AtomicAction();

		a.begin();

		obj.incr(1);

		a.abort();

		if (obj.get() != 1234) {
			throw new RuntimeException(
					"The object was not set to 1234 after abort");
		}

		a = new AtomicAction();

		a.begin();

		obj.incr(11111);

		a.commit();

		if (obj.get() != 12345) {
			throw new RuntimeException(
					"The object was not set to 12345 after commit");
		}

		System.out.println("Atomic object operated as expected");
	}
}
