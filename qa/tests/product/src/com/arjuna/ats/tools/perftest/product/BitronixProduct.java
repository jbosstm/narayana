package com.arjuna.ats.tools.perftest.product;

import bitronix.tm.resource.jdbc.JdbcConnectionHandle;

import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

import com.arjuna.ats.tools.perftest.DbWrapper;

public class BitronixProduct extends Product
{
    public String getName()
    {
        return "Bitronix";
    }

    public TransactionManager getTransactionManager()
    {
        return bitronix.tm.TransactionManagerServices.getTransactionManager();
    }

    protected UserTransaction createTx() throws SystemException, SQLException
    {
        return bitronix.tm.TransactionManagerServices.getTransactionManager();
    }

    protected boolean supportsDb(String db)
    {
        return true;
    }

    protected DataSource createDataSource(String name, Properties props) throws SQLException
    {
        bitronix.tm.resource.jdbc.PoolingDataSource ds = new bitronix.tm.resource.jdbc.PoolingDataSource();

        ds.setClassName(DbWrapper.getDbXADsClass(props));
        ds.setUniqueName(name);
        ds.setMinPoolSize(1);
        ds.setMaxPoolSize(3);
        ds.setAutomaticEnlistingEnabled(true);
        ds.setAllowLocalTransactions(false);
        ds.setDriverProperties(DbWrapper.getDbProperties(props));
        ds.setPreparedStatementCacheSize(8);
        ds.init();  // eagerly initialise the datasource

        return ds;
    }  
}
