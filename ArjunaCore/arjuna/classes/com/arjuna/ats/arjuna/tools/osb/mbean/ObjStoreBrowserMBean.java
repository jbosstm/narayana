package com.arjuna.ats.arjuna.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

@MXBeanDescription("Representation of the transaction logging mechanism")
public interface ObjStoreBrowserMBean extends ObjStoreItemMBean {
	@MXBeanPropertyDescription("See if any new transactions have been created or completed")
	void probe();
	@MXBeanPropertyDescription("Enable/disable viewing of Subordinate Atomic Actions (afterwards use the probe operation to rescan the store):"
            + " WARNING THIS OPERATION WILL TRIGGER A RECOVERY ATTEMPT (recovery is normally performed by the Recovery Manager). Use the text \"true\" to enable")
	void viewSubordinateAtomicActions(boolean enable);
}
