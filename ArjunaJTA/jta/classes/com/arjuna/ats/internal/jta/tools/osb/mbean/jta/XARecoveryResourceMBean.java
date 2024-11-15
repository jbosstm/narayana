/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.tools.osb.mbean.jta;

import com.arjuna.ats.internal.arjuna.tools.osb.annotation.MXBeanDescription;
import com.arjuna.ats.internal.arjuna.tools.osb.mbean.OSEntryBeanMBean;

/**
 * MBean for XAResourceRecords
 *
 * @author Mike Musgrove
 */
@MXBeanDescription("Management view of a JTS XARecoveryResource participating in a transaction")
public interface XARecoveryResourceMBean extends OSEntryBeanMBean {
    byte[] getGlobalTransactionId();
    byte[] getBranchQualifier();
    int getFormatId();
    String getNodeName() ;
    int getHeuristicValue();
//    boolean isCommitted();
}
