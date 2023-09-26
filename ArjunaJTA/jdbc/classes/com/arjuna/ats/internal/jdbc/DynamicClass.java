/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc;

import java.sql.SQLException;

import javax.sql.XADataSource;

/**
 * This class is responsible for maintaining connection information
 * in such a manner that we can recover the connection to the XA
 * database in the event of a failure.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: DynamicClass.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 2.0.
 */

public interface DynamicClass
{
    public XADataSource getDataSource (String dbName) throws SQLException;
}