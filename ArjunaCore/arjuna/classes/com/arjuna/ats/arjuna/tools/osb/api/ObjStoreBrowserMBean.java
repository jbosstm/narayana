/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.api;

import javax.management.MBeanException;

import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.ObjStoreItemMBean;

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
