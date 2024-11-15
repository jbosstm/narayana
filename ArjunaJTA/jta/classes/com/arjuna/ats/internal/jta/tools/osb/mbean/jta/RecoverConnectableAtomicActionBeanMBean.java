/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.tools.osb.api.ActionBeanMBean;
import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

public interface RecoverConnectableAtomicActionBeanMBean extends ActionBeanMBean {
    @MXBeanPropertyDescription("A unique id for this transaction")
	String toDo();
}
