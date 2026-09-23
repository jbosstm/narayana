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

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.LockManager;
import com.arjuna.ats.txoj.LockMode;
import com.arjuna.ats.txoj.LockResult;

/**
 * This is an Transactional Object for Java. It behaves like a transaction aware
 * resource. This particular variation of a TXOJ is not persistent so we do not
 * need to store the UID.
 */
public class CustomerCreationCounter extends LockManager {

	/**
	 * This is the transactional state of the object.
	 */
	private int state = 0;

	/**
	 * Increment the counter.
	 * 
	 * @param value
	 * @throws Exception
	 */
	public void incr(int value) throws Exception {
		if (setlock(new Lock(LockMode.WRITE), 0) == LockResult.GRANTED) {
			state += value;
		} else {
			throw new Exception("Error - could not set write lock.");
		}
	}

	/**
	 * Get the current count of customers
	 * 
	 * @return
	 * @throws Exception
	 */
	public int get() throws Exception {
		if (setlock(new Lock(LockMode.READ), 0) == LockResult.GRANTED) {
			return state;
		} else {
			throw new Exception("Error - could not set read lock.");
		}
	}

	/**
	 * TXOJ classes must override save_state in order to save their state.
	 */
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

	/**
	 * TXOJ classes must override restore_state in order to have their state
	 * recovered by transactions.
	 */
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

	/**
	 * TXOJ classes must override the type method to partition them from other
	 * classes of <code>LockManager</code>.
	 */
	public String type() {
		return super.type() + this.getClass().getName();
	}
}
