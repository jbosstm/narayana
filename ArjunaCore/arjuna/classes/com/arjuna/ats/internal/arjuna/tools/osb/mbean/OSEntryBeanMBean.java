/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.tools.osb.mbean;

import javax.management.MBeanException;

import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

@MXBeanDescription("")
public interface OSEntryBeanMBean extends ObjStoreItemMBean {
	String getType();
	String getId();

	@MXBeanPropertyDescription("Tell the Transaction Manager to remove this record")
	String remove() throws MBeanException;
}
