/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jdbc.utils;



public interface DBPlugin
{
    public javax.sql.DataSource getDataSource(String[] args) throws java.sql.SQLException;
}