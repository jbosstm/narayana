/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */


package io.narayana.lra.coordinator.tools.osb.mbean;

import com.arjuna.ats.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.arjuna.tools.osb.mbean.LogRecordWrapperMBean;

import java.net.URI;

@MXBeanDescription("Management view of an participant record of the LRA")
public interface LRAParticipantRecordWrapperMBean extends LogRecordWrapperMBean {

    URI getRecoveryURI();
    String getParticipantPath();

    String getCompensator();
    URI getEndNotificationUri();

    String getLRAStatus();
}