/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Property manager wrapper for the recovery system.
 *
 */
public class recoveryPropertyManager
{
    public static RecoveryEnvironmentBean getRecoveryEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(RecoveryEnvironmentBean.class);
    }
}