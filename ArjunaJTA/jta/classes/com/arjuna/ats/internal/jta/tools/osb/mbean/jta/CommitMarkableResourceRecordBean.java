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
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

/**
 * MBean implementation of a transaction participant corresponding to a JTA
 * XAResource
 */
/**
 * @Deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class CommitMarkableResourceRecordBean extends LogRecordWrapper
		implements CommitMarkableResourceRecordBeanMBean {
	String className;
	String eisProductName;
	String eisProductVersion;
	String jndiName;
    int timeout;
	XidImple xidImple;

	int heuristic;

	public CommitMarkableResourceRecordBean(UidWrapper w) {
		super(w.getUid());
		init();
	}

	public CommitMarkableResourceRecordBean(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
		super(parent, rec, listType);
		init();
		// xares = new JTAXAResourceRecordWrapper(rec.order());
	}

	private void init() {
		jndiName = getUid().stringForm();
		className = "unavailable";
		eisProductName = "unavailable";
		eisProductVersion = "unavailable";
		timeout = 0;
		heuristic = -1;
		xidImple = new XidImple(new XID());
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
			heuristic = xarec.getHeuristic();
		}

        if (xares != null) {
            className = xares.getClass().getName();

            try {
                timeout = xares.getTransactionTimeout();
            } catch (Exception e) {
            }
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

    @Override
    public int getTimeout() {
        return timeout;
    }

	@Override
	public String getJndiName() {
		return jndiName;
	}

	@Override
	public String getHeuristicStatus() {
		String hs = super.getHeuristicStatus();

		if (heuristic != -1 && HeuristicStatus.UNKNOWN.name().equals(hs)) {
			hs = HeuristicStatus.intToStatus(heuristic).name();
		}

		return hs;
	}

    @Override
    public byte[] getGlobalTransactionId() {
        return xidImple.getGlobalTransactionId();
    }
    @Override
    public byte[] getBranchQualifier() {
        return xidImple.getBranchQualifier();
    }
    @Override
    public int getFormatId() {
        return xidImple.getFormatId();
    }
    @Override
    public String getNodeName() {
        return XATxConverter.getNodeName(xidImple.getXID());
    }
    @Override
    public int getHeuristicValue() {
        return heuristic;
    }
}
