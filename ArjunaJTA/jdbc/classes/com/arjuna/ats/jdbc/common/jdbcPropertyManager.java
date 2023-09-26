/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.ats.jdbc.common;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * Property manager wrapper for the JDBC module.
 */
public class jdbcPropertyManager
{
    public static JDBCEnvironmentBean getJDBCEnvironmentBean()
    {
        return BeanPopulator.getDefaultInstance(JDBCEnvironmentBean.class);
    }
}