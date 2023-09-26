/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.hp.mwtests.ts.jta.recovery;

import java.sql.SQLException;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

public class XARXARRMSyncerXARecovery implements XAResourceRecovery {

	public XAResource getXAResource() throws SQLException {
		count++;

		return new XARXARMSyncer();
	}

	public boolean initialise(String p) throws SQLException {
		return true;
	}

	public boolean hasMoreResources() {
		if (count < 1)
			return true;
		else
			return false;
	}

	private int count = 0;

}
