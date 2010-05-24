package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XAResourceMBean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;

@MXBeanDescription("Management view of an XAResource participating in a transaction")
public interface XAResourceRecordBeanMBean extends XAResourceMBean {
}
