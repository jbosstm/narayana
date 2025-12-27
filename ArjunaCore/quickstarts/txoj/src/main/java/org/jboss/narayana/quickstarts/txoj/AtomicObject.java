package org.jboss.narayana.quickstarts.txoj;

import java.io.IOException;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

public class AtomicObject extends LockManager {

	private int state;

	public AtomicObject() throws Exception {
		super();

		state = 0;

		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			System.out.println("Created persistent object " + get_uid());
		} else {
			throw new Exception("setlock error.");
		}
	}

	public void incr(int value) throws Exception {

		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			state += value;

			return;
		} else {
			throw new Exception("Write lock error.");
		}
	}

	public void set(int value) throws Exception {
		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			state = value;
			return;
		} else {
			throw new Exception("Write lock error.");
		}
	}

	public int get() throws Exception {
		if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED) {
			return state;
		} else {
			throw new Exception("Read lock error.");
		}
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
		AtomicAction a = new AtomicAction();
		a.begin();

		AtomicObject obj = new AtomicObject();
		obj.set(1234);
		a.commit();

		a = new AtomicAction();
		a.begin();
		try {
			if (obj.get() != 1234) {
				throw new RuntimeException("The object was not set to 1234");
			}
		} finally {
			a.commit();
		}

		a = new AtomicAction();
		a.begin();
		obj.incr(1);
		a.abort();

		a = new AtomicAction();
		a.begin();
		try {
			if (obj.get() != 1234) {
				throw new RuntimeException(
						"The object was not set to 1234 after abort");
			}
		} finally {
			a.commit();
		}

		a = new AtomicAction();
		a.begin();
		obj.incr(11111);
		a.commit();

		a = new AtomicAction();
		a.begin();
		try {
			if (obj.get() != 12345) {
				throw new RuntimeException(
						"The object was not set to 12345 after commit");
			}
		} finally {
			a.commit();
		}

		System.out.println("Atomic object operated as expected");
	}
}
