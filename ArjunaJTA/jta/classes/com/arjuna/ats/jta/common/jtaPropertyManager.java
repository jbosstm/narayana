/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.jta.common;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Property manager wrapper for the JTA module.
 */
public class jtaPropertyManager
{
    public static JTAEnvironmentBean getJTAEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(JTAEnvironmentBean.class);
    }
}