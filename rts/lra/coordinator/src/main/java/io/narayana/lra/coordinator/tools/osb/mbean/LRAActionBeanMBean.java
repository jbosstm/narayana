/*
 * Copyright Red Hat
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.narayana.lra.coordinator.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.ActionBeanMBean;
import com.arjuna.ats.arjuna.tools.osb.mbean.OSEntryBeanMBean;

import java.net.URI;

@MXBeanDescription("Management view of an Long Running Action")
public interface LRAActionBeanMBean extends ActionBeanMBean, OSEntryBeanMBean {
    URI getLRAId();
    URI getParentLRAId();
    String getLRAClientId();
    String getLRAStatus();
    long getStartTime();
    long getFinishTime();
}
