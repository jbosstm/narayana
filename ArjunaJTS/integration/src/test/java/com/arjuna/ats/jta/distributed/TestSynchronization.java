/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed;

import jakarta.transaction.Synchronization;

public class TestSynchronization implements Synchronization {
	private String serverId;

	public TestSynchronization(String serverId) {
		this.serverId = serverId;
	}

	@Override
	public void beforeCompletion() {
		System.out.println(" TestSynchronization (" + serverId + ")      beforeCompletion");
	}

	@Override
	public void afterCompletion(int status) {
		System.out.println(" TestSynchronization (" + serverId + ")      afterCompletion");
	}
}