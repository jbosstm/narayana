package com.arjuna.ats.tools.perftest.product;

import com.arjuna.ats.tools.perftest.product.Product;
import com.arjuna.ats.tools.perftest.DbWrapper;

import javax.sql.DataSource;
import javax.transaction.*;
import java.util.Properties;
import java.sql.SQLException;
import java.sql.Connection;

public class NonXAProduct extends Product
{
    public String getName()
    {
        return "NonXA";
    }

    protected void commitTx(Connection[] connections) throws Exception
    {
        for (Connection c : connections)
            c.commit();
    }

    protected void closeConnection(Connection c) throws SQLException
    {
        c.commit();
        c.close();
    }

    protected boolean supportsDb(String db)
    {
        return true;
    }

    protected DataSource createDataSource(String name, Properties props) throws SQLException
    {
        return DbWrapper.createDataSource(name, props);
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
