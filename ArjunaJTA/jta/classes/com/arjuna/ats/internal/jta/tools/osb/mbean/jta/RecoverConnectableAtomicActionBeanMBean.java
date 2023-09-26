/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanPropertyDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBeanMBean;

/**
 * @deprecated as of 5.0.5.Final In a subsequent release we will change packages names in order to 
 * provide a better separation between public and internal classes.
 */
@Deprecated // in order to provide a better separation between public and internal classes.
public interface RecoverConnectableAtomicActionBeanMBean extends ActionBeanMBean {
    @MXBeanPropertyDescription("A unique id for this transaction")
	String toDo();
}