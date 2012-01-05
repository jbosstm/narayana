package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;

@MXBeanDescription("")
public interface OSEntryBeanMBean extends ObjStoreItemMBean {
	String getType();
	String getId();
}
