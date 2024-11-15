/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.tools.osb.api;

import javax.management.MBeanException;

import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.OSEntryBeanMBean;

@MXBeanDescription("Management view of a transaction")
public interface ActionBeanMBean extends OSEntryBeanMBean {
	long getAgeInSeconds();
	String getCreationTime();
	@MXBeanPropertyDescription("Indicates whether this entry corresponds to a transaction participant")
	boolean isParticipant();

	@MXBeanPropertyDescription("Tell the Transaction Manager to remove this action")
	String remove() throws MBeanException;
}
