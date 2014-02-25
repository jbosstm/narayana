/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2013
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapper;
import com.arjuna.ats.arjuna.tools.osb.mbean.ParticipantStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord;

/**
 * MBean implementation of a transaction participant corresponding to a JTA
 * XAResource
 */
public class CommitMarkableResourceRecordBean extends LogRecordWrapper
		implements CommitMarkableResourceRecordBeanMBean {
	String className = "unavailable";
	String eisProductName = "unavailable";
	String eisProductVersion = "unavailable";
	String jndiName = "unavailable";

	public CommitMarkableResourceRecordBean(UidWrapper w) {
		super(w.getUid());
	}

	public CommitMarkableResourceRecordBean(ActionBean parent,
			AbstractRecord rec, ParticipantStatus listType) {
		super(parent, rec, listType);
		// xares = new JTAXAResourceRecordWrapper(rec.order());
	}

	public boolean activate() {
		boolean ok = super.activate();
		XAResource xares = (XAResource) rec.value();

		className = rec.getClass().getName();

		if (rec instanceof CommitMarkableResourceRecord) {
			CommitMarkableResourceRecord xarec = (CommitMarkableResourceRecord) rec;

			eisProductName = xarec.getProductName();
			eisProductVersion = xarec.getProductVersion();
			jndiName = xarec.getJndiName();
		}

		return ok;
	}

	public String getClassName() {
		return className;
	}

	public String getEisProductName() {
		return eisProductName;
	}

	public String getEisProductVersion() {
		return eisProductVersion;
	}

	public String getJndiName() {
		return jndiName;
	}
}
