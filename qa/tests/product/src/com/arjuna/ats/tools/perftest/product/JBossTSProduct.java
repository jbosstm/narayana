package com.arjuna.ats.tools.perftest.product;

import com.arjuna.ats.jdbc.TransactionalDriver;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.tools.perftest.DbWrapper;

import javax.sql.DataSource;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import java.io.PrintWriter;
import java.util.*;
import java.sql.SQLException;
import java.sql.Connection;

public class JBossTSProduct extends Product
{
    public String getName()
    {
        return "JBossTS (" + getStoreType() + ")";
    }

    public TransactionManager getTransactionManager()
    {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    protected UserTransaction createTx() throws SystemException, SQLException
    {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    protected boolean supportsDb(String db)
    {
        return true;
    }

    protected String getStoreType()
    {
        String st = arjPropertyManager.getObjectStoreEnvironmentBean().getObjectStoreType();

        int ind = st.lastIndexOf('.') + 1;

        return st.substring(ind == 0 ? 0 : ind);
    }

    protected DataSource createDataSource(String dsName, final Properties props) throws SQLException
    {
        String dbName = DbWrapper.getDbName(props);
        String jndiName = "jdbc/" + dbName;
        String jndiUrl = DbWrapper.initJndi(jndiName);

        String user = DbWrapper.getDbUser(props);
        String pass = DbWrapper.getDbPassword(props);

        final String txDriverUrl = TransactionalDriver.arjunaDriver + jndiName; // + ";reuseconnection=true";
        final DataSource ds = DbWrapper.createDataSource(dsName, props);

        try
        {
            DbWrapper.jndiBind(jndiUrl, jndiName, ds);
        }
        catch (NamingException e)
        {
            if (log.isInfoEnabled()) log.info(e.getMessage() + ": jndi bind " + jndiName + " at " + jndiUrl + " failed.");
            throw new RuntimeException(e);
        }

        props.setProperty(com.arjuna.ats.jdbc.TransactionalDriver.userName, user);
        props.setProperty(com.arjuna.ats.jdbc.TransactionalDriver.password, pass);
        props.setProperty("reuseconnection", "true");

        return new DataSource() {
            com.arjuna.ats.jdbc.TransactionalDriver txDriver = new com.arjuna.ats.jdbc.TransactionalDriver();

            public Connection getConnection() throws SQLException {
                return txDriver.connect(txDriverUrl, props);
            }
            public Connection getConnection(String u, String p) throws SQLException {
                props.setProperty(TransactionalDriver.userName, u);
                props.setProperty(TransactionalDriver.password, p);

                return txDriver.connect(txDriverUrl, props);
            }
            public PrintWriter getLogWriter() throws SQLException {return ds.getLogWriter();}
            public void setLogWriter(PrintWriter out) throws SQLException {ds.setLogWriter(out);}
            public void setLoginTimeout(int seconds) throws SQLException {ds.setLoginTimeout(seconds);}
            public int getLoginTimeout() throws SQLException {return ds.getLoginTimeout();}
        };
    }
}
