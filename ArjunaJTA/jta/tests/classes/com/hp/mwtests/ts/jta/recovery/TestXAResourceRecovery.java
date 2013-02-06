/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.recovery;

import java.sql.SQLException;
import java.util.Stack;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

public class TestXAResourceRecovery implements XAResourceRecovery {

	private static Stack<XAResource> resources = new Stack<XAResource>();

	public XAResource getXAResource() throws SQLException {
		return resources.pop();
	}

	public boolean initialise(String p) throws SQLException {
		return true;
	}

	public boolean hasMoreResources() {
		return resources.size() > 0;
	}

	public static void setResources(XAResource resource, XAResource resource2) {
		resources.push(resource);
		resources.push(resource2);
	}
}
