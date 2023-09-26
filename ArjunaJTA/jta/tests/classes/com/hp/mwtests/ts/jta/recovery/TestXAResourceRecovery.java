/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.recovery;

import java.sql.SQLException;
import java.util.Stack;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

public class TestXAResourceRecovery implements XAResourceRecovery {

	private static Stack<XAResource> resources = new Stack<XAResource>();
	private static int count = 0;

	public XAResource getXAResource() throws SQLException {
		// This method was changed because now periodicWorkFirstPass can be called to retry loading Xids from XAR
		// During getNewXAResource. If we don't return an XAR then getContactedJndiNames (JBTM-860) fails and the TX
		// cannot be cleaned up
		count--;
		XAResource toReturn = resources.remove(0);
		resources.push(toReturn);
		return toReturn;
	}

	public boolean initialise(String p) throws SQLException {
		return true;
	}

	public boolean hasMoreResources() {
		if (count == 0) {
			count = 2;
			return false;
		} else {
			return true;
		}
	}

	public static void setResources(XAResource resource, XAResource resource2) {
		resources.clear();
		resources.push(resource);
		resources.push(resource2);
		count = 2;
	}
}