/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.hp.mwtests.ts.jta.common;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.Transaction;
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
