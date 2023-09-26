/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed.server;

public class LookupProvider {
	private static LookupProvider instance;

	private RemoteServer[] remoteServers = new RemoteServer[3];

	public static LookupProvider getInstance() {
		if (instance == null) {
			instance = new LookupProvider();
		}
		return instance;
	}

	protected LookupProvider() {
	}

	public RemoteServer lookup(String jndiName) {
		int index = (Integer.valueOf(jndiName) / 1000) - 1;
		return remoteServers[index];
	}

	public void clear() {
		for (int i = 0; i < remoteServers.length; i++) {
			// Disconnect
			remoteServers[i] = null;
		}
	}

	public void bind(int index, RemoteServer connectTo) {
		remoteServers[index] = connectTo;
	}
}