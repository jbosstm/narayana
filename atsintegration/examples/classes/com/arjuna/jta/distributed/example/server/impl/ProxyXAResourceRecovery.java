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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.jboss.tm.XAResourceRecovery;

/**
 * This class is required by the transaction manager so that it can query the
 * list of running subordinate transactions which have it as a parent. Typically
 * we will know about these via the <code>Serializable</code>
 * <code>ProxyXAResource</code> class. However scenarios exist whereby the
 * subordinate has a prepared transaction and failed to return to the parent or
 * the parent failed itself. In this case we need to query these XIDs from the
 * remote server to detect these orphans.
 */
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
