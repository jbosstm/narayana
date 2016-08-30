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
