package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

@MXBeanDescription("")
public interface OSEntryBeanMBean extends ObjStoreItemMBean {
	String getType();
	String getId();

	@MXBeanPropertyDescription("Tell the Transaction Manager to remove this record")
	String remove();
}
