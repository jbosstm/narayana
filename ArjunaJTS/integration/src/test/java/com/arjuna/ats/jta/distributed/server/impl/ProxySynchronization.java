/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed.server.impl;

import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import javax.transaction.xa.Xid;

import com.arjuna.ats.jta.distributed.server.LookupProvider;

public class ProxySynchronization implements Synchronization {

	private String localServerName;
	private String remoteServerName;
	private Xid toRegisterAgainst;

	public ProxySynchronization(String localServerName, String remoteServerName, Xid toRegisterAgainst) {
		this.localServerName = localServerName;
		this.remoteServerName = remoteServerName;
		this.toRegisterAgainst = toRegisterAgainst;
	}

	@Override
	public void beforeCompletion() {
		System.out.println("ProxySynchronization (" + localServerName + ":" + remoteServerName + ") beforeCompletion");
		try {
			LookupProvider.getInstance().lookup(remoteServerName).beforeCompletion(toRegisterAgainst);
		} catch (SystemException e) {
			// Nothing we can really do here
			e.printStackTrace();
		}
	}

	@Override
	public void afterCompletion(int status) {
		// These are not proxied but are handled during local commits
	}
}