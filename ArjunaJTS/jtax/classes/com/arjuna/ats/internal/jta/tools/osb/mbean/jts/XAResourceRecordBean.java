package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.ParticipantStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapper;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;

/**
 * MBean implementation of a transaction participant corresponding to a JTA XAResource
 */
public class XAResourceRecordBean extends LogRecordWrapper implements XAResourceRecordBeanMBean {
    JTSXAResourceRecordWrapper xares;
    String className = "unavailable";
    String eisProductName = "unavailable";
    String eisProductVersion = "unavailable";
    int timeout = 0;

    public XAResourceRecordBean(UidWrapper w) {
        super(w.getUid());
        xares = new JTSXAResourceRecordWrapper(w.getUid());
    }
    public XAResourceRecordBean(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
        super(parent, rec, listType);
        xares = new JTSXAResourceRecordWrapper(rec.order());
    }

    public String getClassName() { return className; }
    public String getEisProductName() { return eisProductName; }
    public String getEisProductVersion() { return eisProductVersion; }
    public int getTimeout() { return timeout; }

    /**
     * Extension of an XAResource record for exposing the underlying XAResource which is protected
     */
    public class JTSXAResourceRecordWrapper extends com.arjuna.ats.internal.jta.resources.jts.orbspecific.XAResourceRecord {
        public JTSXAResourceRecordWrapper(Uid uid) {
            super(uid);

            if (_theXAResource != null) {
                XAResourceRecordBean.this.className = _theXAResource.getClass().getName();
                XAResourceRecordBean.this.eisProductName = callMethod(_theXAResource, "getEisProductName");
                XAResourceRecordBean.this.eisProductVersion = callMethod(_theXAResource, "getEisProductVersion");

                try {
                    timeout = _theXAResource.getTransactionTimeout();
                } catch (Exception e) {
                }
            }
        }
    }
}
