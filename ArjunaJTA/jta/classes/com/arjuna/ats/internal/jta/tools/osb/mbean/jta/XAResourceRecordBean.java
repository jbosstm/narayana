package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapper;
import com.arjuna.ats.arjuna.tools.osb.mbean.ParticipantStatus;
import com.arjuna.ats.arjuna.tools.osb.mbean.UidWrapper;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;

/**
 * MBean implementation of a transaction participant corresponding to a JTA XAResource
 */
public class XAResourceRecordBean extends LogRecordWrapper implements XAResourceRecordBeanMBean {
    String className = "unavailable";
    String eisProductName = "unavailable";
    String eisProductVersion = "unavailable";
    String jndiName = "unavailable";
    int timeout = 0;

    public XAResourceRecordBean(UidWrapper w) {
        super(w.getUid());
    }
    public XAResourceRecordBean(ActionBean parent, AbstractRecord rec, ParticipantStatus listType) {
        super(parent, rec, listType);
//        xares = new JTAXAResourceRecordWrapper(rec.order());
    }

    public boolean activate() {
        boolean ok = super.activate();
        XAResource xares = (XAResource) rec.value();

        className = rec.getClass().getName();

        if (rec instanceof XAResourceRecord) {
            XAResourceRecord xarec = (XAResourceRecord) rec;

            eisProductName = xarec.getProductName();
            eisProductVersion = xarec.getProductVersion();
            jndiName = xarec.getJndiName();
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

    public String getClassName() { return className; }
    public String getEisProductName() { return eisProductName; }
    public String getEisProductVersion() { return eisProductVersion; }
    public String getJndiName() { return jndiName; }
    public int getTimeout() { return timeout; }
}
