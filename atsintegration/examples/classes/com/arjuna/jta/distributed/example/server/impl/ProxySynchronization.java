/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.jta.distributed.example.server.impl;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.Xid;

import com.arjuna.jta.distributed.example.server.LookupProvider;

/**
 * An example to show how the transport can register a proxy synchronization.
 * 
 * <p>
 * Note that we do not proxy the afterCompletion, this is left to run locally
 * per subordinate.
 */
public class ProxySynchronization implements Synchronization {

	private String localServerName;
	private String remoteServerName;
	private Xid toRegisterAgainst;

	public ProxySynchronization(String localServerName, String remoteServerName, Xid toRegisterAgainst) {
		this.localServerName = localServerName;
		this.remoteServerName = remoteServerName;
		this.toRegisterAgainst = toRegisterAgainst;
	}

	/**
	 * Propagate the before completion in a transport specific manner.
	 */
	@Override
	public void beforeCompletion() {
		System.out.println("ProxySynchronization (" + localServerName + ":" + remoteServerName + ") beforeCompletion");
		try {
			LookupProvider.getInstance().lookup(remoteServerName).beforeCompletion(toRegisterAgainst);
		} catch (SystemException e) {
			// Unfortunately we cannot do much else here
			e.printStackTrace();
		}
	}

	@Override
	public void afterCompletion(int status) {
		// These are not proxied but are handled during local commits
	}
}
