package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBeanMBean;

@MXBeanDescription("Management view of a subordinate transaction")
public interface SubordinateActionBeanMBean extends ActionBeanMBean {
    @MXBeanPropertyDescription("A unique id for this transaction")
	String getXid();
    @MXBeanPropertyDescription("The (XA) node name assigned by the administrator of the server from which this transaction was propagated")
	String getParentNodeName();
}