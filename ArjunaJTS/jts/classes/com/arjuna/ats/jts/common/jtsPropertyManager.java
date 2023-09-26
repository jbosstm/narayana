/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.common;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Property manager wrapper for the JTS module.
 */
public class jtsPropertyManager
{
    public static JTSEnvironmentBean getJTSEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(JTSEnvironmentBean.class);
    }
}