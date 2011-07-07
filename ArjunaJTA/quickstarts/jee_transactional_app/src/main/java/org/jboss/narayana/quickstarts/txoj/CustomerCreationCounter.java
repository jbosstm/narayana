/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2011,
 * @author JBoss, by Red Hat.
 */
package org.jboss.narayana.quickstarts.txoj;

import java.io.IOException;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

public class CustomerCreationCounter extends LockManager {

	private int state;

	public CustomerCreationCounter() {
		super(ObjectType.RECOVERABLE);

		state = 0;

		// if (!(setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED)) {
		// throw new Exception("setlock error.");
		// }
	}

	public void incr(int value) throws Exception {
		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			state += value;
		} else {
			throw new Exception("Error - could not set write lock.");
		}
	}

	public int get() throws Exception {
		if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED) {
			return state;
		} else {
			throw new Exception("Error - could not set read lock.");
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
		return "/StateManager/LockManager/" + this.getClass().getName();
	}
}
