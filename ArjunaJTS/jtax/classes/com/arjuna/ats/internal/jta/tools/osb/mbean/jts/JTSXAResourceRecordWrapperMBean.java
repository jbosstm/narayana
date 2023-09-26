/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.jta.tools.osb.mbean.jts;

import com.arjuna.ats.internal.jta.tools.osb.mbean.jta.XARecoveryResourceMBean;

/**
 * Created by tom on 01/11/2016.
 */
public interface JTSXAResourceRecordWrapperMBean extends XARecoveryResourceMBean {

    public void clearHeuristic() ;
}
