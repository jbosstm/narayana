package com.arjuna.ats.tools.perftest.product;

import com.arjuna.ats.tools.perftest.DbWrapper;

import javax.transaction.*;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.*;

public class XAProduct extends Product
{
    private List<XAResource> resources = new ArrayList<XAResource> ();
    private List<Xid> xids = new ArrayList<Xid> ();

    public String getName()
    {
        return "XA";
    }

    protected void beginTx() throws SystemException, NotSupportedException, SQLException, XAException
    {
        xids.clear();

        for (XAResource res : resources)
        {
            Xid xid = DbWrapper.createJJHXid();

            xids.add(xid);
            res.start(xid, XAResource.TMNOFLAGS);
        }
    }

    protected void suspendTx() throws SQLException, XAException, SystemException
    {
        for (int i = 0; i < resources.size(); i++)
            resources.get(i).end(xids.get(i), XAResource.TMSUSPEND);
    }

    protected void resumeTx() throws SQLException, XAException, SystemException, InvalidTransactionException
    {
        for (int i = 0; i < resources.size(); i++)
            resources.get(i).start(xids.get(i), XAResource.TMRESUME);
    }

    protected void commitTx(Connection[] connections) throws XAException
    {
        for (int i = 0; i < resources.size(); i++)
        {
            XAResource res = resources.get(i);
            Xid xid = xids.get(i);

            res.end(xid, XAResource.TMSUCCESS);
            res.commit(xid, true);
        }
    }

    protected void closeConnection(Connection c) throws SQLException
    {
        c.commit();
        c.close();
    }

    protected boolean supportsDb(String db)
    {
        return DbWrapper.isDerby(db) || DbWrapper.isMysql(db) || DbWrapper.isH2(db);
    }

    protected DataSource createDataSource(String dsName, final Properties props) throws SQLException
    {
        DataSource xads = DbWrapper.createDataSource(dsName, props);

        if (xads != null && xads instanceof XADataSource)
            resources.add(((XADataSource) xads).getXAConnection().getXAResource());

        return xads;
    }

    public TransactionManager getTransactionManager()
    {
        return new TransactionManager() {
            public void begin() throws NotSupportedException, SystemException {}
            public void commit() throws HeuristicMixedException, HeuristicRollbackException, IllegalStateException, RollbackException, SecurityException, SystemException {}
            public int getStatus() throws SystemException {return 0;}
            public Transaction getTransaction() throws SystemException {return null;}
            public void resume(Transaction transaction) throws IllegalStateException, InvalidTransactionException, SystemException {}
            public void rollback() throws IllegalStateException, SecurityException, SystemException {}
            public void setRollbackOnly() throws IllegalStateException, SystemException {}
            public void setTransactionTimeout(int i) throws SystemException {}
            public Transaction suspend() throws SystemException {return null;}
        };
    }
}
