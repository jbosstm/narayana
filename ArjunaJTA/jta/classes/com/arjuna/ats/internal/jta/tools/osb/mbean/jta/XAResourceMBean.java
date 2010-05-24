package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapperMBean;

@MXBeanDescription("Management view of an XAResource participating in a transaction")
public interface XAResourceMBean extends LogRecordWrapperMBean {
	@MXBeanPropertyDescription("The java type that implements this XAResource")
	String getClassName();
	@MXBeanPropertyDescription("JNDI name of the JCA resource")
	String getEisProductName();
	@MXBeanPropertyDescription("JCA product version")
	String getEisProductVersion();
	@MXBeanPropertyDescription("The number of seconds before the resource can rollback the branch")
	int getTimeout();
}
