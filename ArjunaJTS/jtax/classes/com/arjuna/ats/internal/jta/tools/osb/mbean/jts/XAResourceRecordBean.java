/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.*;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

import java.io.IOException;

/**
 * MBean implementation of a transaction participant corresponding to a JTA XAResource
 *
 * @author Mike Musgrove
 */
/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public class XAResourceRecordBean extends LogRecordWrapper implements XAResourceRecordBeanMBean {
    JTSXAResourceRecordWrapper xares;
    String className = "unavailable";
    String eisProductName = "unavailable";
    String eisProductVersion = "unavailable";
    String jndiName = "unavailable";
    int timeout = 0;
    com.arjuna.ats.internal.jta.tools.osb.mbean.jts.JTSXAResourceRecordWrapper jtsXAResourceRecord;
    XidImple xidImple;
    int heuristic;

    public XAResourceRecordBean(UidWrapper w) {
        super(w.getUid());
        init();
    }
    public XAResourceRecordBean(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
        super(parent, rec, listType);
        init();
    }

	private void init() {
		jndiName = getUid().stringForm();
		className = "unavailable";
		eisProductName = "unavailable";
		eisProductVersion = "unavailable";
		timeout = 0;
        xares = new JTSXAResourceRecordWrapper(getUid());
        xidImple = xares.xidImple;
        heuristic = xares.heuristic;
	}

    public String getClassName() { return className; }
    public String getEisProductName() { return eisProductName; }
    public String getEisProductVersion() { return eisProductVersion; }
    public String getJndiName() { return jndiName; }
    public int getTimeout() { return timeout; }

    @Override
    public String getHeuristicStatus() {
        return HeuristicStatus.intToStatus(xares.heuristic).name();
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

    public void setJtsXAResourceRecord(com.arjuna.ats.internal.jta.tools.osb.mbean.jts.JTSXAResourceRecordWrapper jtsXAResourceRecord) {
        this.jtsXAResourceRecord = jtsXAResourceRecord;
    }

    @Override
    public String remove() {
        if (jtsXAResourceRecord != null && jtsXAResourceRecord.doRemove())
            jtsXAResourceRecord = null;

        return super.remove();
    }

    /**
     * Extension of an XAResource record for exposing the underlying XAResource which is protected
     */
    public class JTSXAResourceRecordWrapper extends com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord {
        XidImple xidImple;
        int heuristic;

        public JTSXAResourceRecordWrapper(Uid uid) {
            super(uid);

            xidImple = new XidImple(getXid());

            if (_theXAResource != null) {
                XAResourceRecordBean.this.className = _theXAResource.getClass().getName();
                XAResourceRecordBean.this.jndiName = callMethod(_theXAResource, "getJndiName");
                XAResourceRecordBean.this.eisProductName = callMethod(_theXAResource, "getProductName");
                XAResourceRecordBean.this.eisProductVersion = callMethod(_theXAResource, "getProductVersion");

                try {
                    timeout = _theXAResource.getTransactionTimeout();
                } catch (Exception e) {
                }
            }
        }

        public boolean restoreState(InputObjectState os) {
            InputObjectState copy = new InputObjectState(os);
            try {
                heuristic = copy.unpackInt();
            } catch (IOException e) {
            }

            return super.restoreState(os);
        }
    }
}
