package com.arjuna.ats.arjuna.tools.osb.mbean;

import javax.management.MBeanException;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
@MXBeanDescription("Representation of the transaction logging mechanism")
public interface ObjStoreBrowserMBean extends ObjStoreItemMBean {
	@MXBeanPropertyDescription("See if any new transactions have been created or completed")
	void probe() throws MBeanException;

	@MXBeanPropertyDescription("Enable/disable viewing of Subordinate Atomic Actions (afterwards"
	    + " use the probe operation to rescan the store):"
	    + " WARNING THIS OPERATION WILL TRIGGER A RECOVERY ATTEMPT (recovery is normally performed"
	    + " by the Recovery Manager). Use the text \"true\" to enable")
	void viewSubordinateAtomicActions(boolean enable);

	@MXBeanPropertyDescription("By default only a subset of transaction logs are exposed as MBeans,"
	    + " this operation changes this default."
	    + "Use the text \"true\" to expose all logs as MBeans. You must invoke the probe method for the"
	    + " change to take effect")
	void setExposeAllRecordsAsMBeans(boolean exposeAllLogs);
}
