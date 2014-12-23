package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XAResourceMBean;

/**
 * @Deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
@MXBeanDescription("Management view of an XAResource participating in a transaction")
public interface XAResourceRecordBeanMBean extends XAResourceMBean {
}
