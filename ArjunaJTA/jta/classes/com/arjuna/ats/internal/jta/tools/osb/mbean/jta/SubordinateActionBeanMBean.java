/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.arjuna.tools.osb.api.ActionBeanMBean;
import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanPropertyDescription;

@MXBeanDescription("Management view of a subordinate transaction")
public interface SubordinateActionBeanMBean extends ActionBeanMBean {
    @MXBeanPropertyDescription("A unique id for this transaction")
	String getXid();
    @MXBeanPropertyDescription("The (XA) node name assigned by the administrator of the server from which this transaction was propagated")
	String getParentNodeName();
}
