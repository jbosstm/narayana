/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc;

import javax.sql.XADataSource;
import jakarta.transaction.Transaction;

import com.arjuna.ats.internal.jdbc.drivers.modifiers.ConnectionModifier;

interface ConnectionControl
{

    String user ();
    String password ();

    String url ();
    String dynamicClass ();

    String dataSourceName ();

    Transaction transaction ();

    void setModifier (ConnectionModifier cm);

    XADataSource xaDataSource ();
    
}