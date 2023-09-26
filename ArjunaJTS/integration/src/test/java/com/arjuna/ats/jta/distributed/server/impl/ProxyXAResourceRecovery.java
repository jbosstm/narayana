/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.distributed.server.impl;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.jboss.tm.XAResourceRecovery;

public class ProxyXAResourceRecovery implements XAResourceRecovery {

	private List<ProxyXAResource> resources = new ArrayList<ProxyXAResource>();

	public ProxyXAResourceRecovery(String nodeName, String[] toRecoverFor) {
		for (int i = 0; i < toRecoverFor.length; i++) {
			resources.add(new ProxyXAResource(nodeName, toRecoverFor[i]));
		}
	}

	@Override
	public XAResource[] getXAResources() {
		return resources.toArray(new XAResource[] {});
	}

}