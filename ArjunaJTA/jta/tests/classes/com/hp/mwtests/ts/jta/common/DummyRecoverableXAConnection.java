/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.common;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import jakarta.transaction.Transaction;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;

public class DummyRecoverableXAConnection implements RecoverableXAConnection
{

    public void close ()
    {
        // TODO Auto-generated method stub
        
    }

    public void closeCloseCurrentConnection () throws SQLException
    {
        // TODO Auto-generated method stub
        
    }

    public XAConnection getConnection () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public XAConnection getCurrentConnection () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public XADataSource getDataSource () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public XAResource getResource () throws SQLException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean inuse ()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean packInto (OutputObjectState os)
    {
        return true;
    }

    public void reset ()
    {
        // TODO Auto-generated method stub
        
    }

    public boolean setTransaction (Transaction tx)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean unpackFrom (InputObjectState os)
    {
        return true;
    }

    public boolean validTransaction (Transaction tx)
    {
        // TODO Auto-generated method stub
        return false;
    }
}