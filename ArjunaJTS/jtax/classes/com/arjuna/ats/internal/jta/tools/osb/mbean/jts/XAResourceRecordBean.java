/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import java.io.IOException;

import javax.management.MBeanException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.HeuristicStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapper;
import com.arjuna.ats.arjuna.tools.osb.mbean.ParticipantStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

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
        init(null);
    }
    public XAResourceRecordBean(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
        super(parent, rec, listType);
        init(rec);
    }

	private void init(AbstractRecord rec) {
		jndiName = getUid().stringForm();
		className = "unavailable";
		eisProductName = "unavailable";
		eisProductVersion = "unavailable";
		timeout = 0;
        xares = new JTSXAResourceRecordWrapper(rec, getUid());
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
    public boolean forget() {
       return xares.forgetHeuristic();
    }

    @Override
    public String remove() throws MBeanException {
        if (forget()) {
            if (jtsXAResourceRecord != null && jtsXAResourceRecord.doRemove())
                jtsXAResourceRecord = null;

            // resource#forget succeeded so now it is ok to remove the inlined ExtendedResourceRecord
            forgetRec = true;

            return super.remove();
        }

        return "Operation failed";
    }

    /**
     * Extension of an XAResource record for exposing the underlying XAResource which is protected
     */
    public class JTSXAResourceRecordWrapper extends com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord {
        XidImple xidImple;
        int heuristic;
        AbstractRecord rec;

        public JTSXAResourceRecordWrapper(AbstractRecord rec, Uid uid) {
            super(uid);

            xidImple = new XidImple(getXid());

            this.rec = rec;

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

        public JTSXAResourceRecordWrapper(AbstractRecord rec) {
            this(rec, rec.order());
            this.rec = rec;
        }

        public boolean restoreState(InputObjectState os) {
            InputObjectState copy = new InputObjectState(os);
            try {
                heuristic = copy.unpackInt();
            } catch (IOException e) {
            }

            return super.restoreState(os);
        }

        public boolean forgetHeuristic() {
            try {
                if (!isForgotten())
                    forget();
            } catch (org.omg.CORBA.SystemException ignore) {
            }

            if (isForgotten() || (rec != null && rec.forgetHeuristic()))
                return true;

            return arjPropertyManager.getObjectStoreEnvironmentBean().isIgnoreMBeanHeuristics();
        }
    }
}