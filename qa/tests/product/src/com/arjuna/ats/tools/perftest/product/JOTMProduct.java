package com.arjuna.ats.tools.perftest.product;

import com.arjuna.ats.tools.perftest.DbWrapper;

import javax.transaction.UserTransaction;
import javax.transaction.SystemException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;
import java.io.PrintWriter;

public class JOTMProduct extends Product
{
    private static final String JNDIBINDNAME = "UserTransaction";

    private org.objectweb.jotm.Jotm jotm;
    private boolean useJndi = true;
    private String jndiUrl;
    private javax.naming.InitialContext context;

    public String getName()
    {
        return "JOTM";
    }

    public TransactionManager getTransactionManager()
    {
        return jotm.getTransactionManager();
    }

    protected void init(Connection c, Properties props) throws SQLException
    {
        if (useJndi)
            jndiUrl = DbWrapper.initJndi(JNDIBINDNAME);
    }

    public void fini()
    {
        if (useJndi)
        {
            try
            {
                context.unbind(JNDIBINDNAME);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        org.objectweb.jotm.TimerManager.stop();
        jotm.stop();
    }

    protected UserTransaction createTx() throws SystemException, SQLException
    {
        if (useJndi)
        {
            try
            {
                return (UserTransaction) context.lookup(JNDIBINDNAME);
            }
            catch (NamingException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            try
            {
                return (UserTransaction) new InitialContext().lookup("UserTransaction");
            }
            catch (NamingException e)
            {
                throw new SystemException(e.getMessage());
            }
//            return jotm.getUserTransaction();
        }
    }

    protected boolean supportsDb(String db)
    {
        return true;
    }

    // StandardXAConnection does not support suspend and resume
    protected void suspendTx() throws SQLException, XAException, SystemException
    {
    }

    protected void resumeTx() throws SQLException, XAException, SystemException, InvalidTransactionException
    {
    }

    protected DataSource createDataSource(String name, final Properties props) throws SQLException
    {
        // Get a transction manager

        try
        {
            // creates an instance of JOTM with a local transaction factory which is not bound to a registry
            jotm = new org.objectweb.jotm.Jotm(true, false);
            if (useJndi)
                context = DbWrapper.jndiBind(jndiUrl, JNDIBINDNAME, jotm.getUserTransaction());
        }
        catch (NamingException e)
        {
            throw new RuntimeException(e);
        }

        final XADataSource xads = new org.enhydra.jdbc.standard.StandardXADataSource();

        try
        {
            ((org.enhydra.jdbc.standard.StandardXADataSource) xads).setDriverName(DbWrapper.getDbDriver(props));
            ((org.enhydra.jdbc.standard.StandardXADataSource) xads).setUrl(DbWrapper.getDbUrl(props));
            ((org.enhydra.jdbc.standard.StandardXADataSource) xads).setTransactionManager(jotm.getTransactionManager());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        return new DataSource() {
            public Connection getConnection() throws SQLException {
                return getConnection(DbWrapper.getDbUser(props), DbWrapper.getDbPassword(props));
            }
            public Connection getConnection(String u, String p) throws SQLException {
                return xads.getXAConnection(u, p).getConnection();
            }
            public PrintWriter getLogWriter() throws SQLException {return xads.getLogWriter();}
            public void setLogWriter(PrintWriter out) throws SQLException {xads.setLogWriter(out);}
            public void setLoginTimeout(int seconds) throws SQLException {xads.setLoginTimeout(seconds);}
            public int getLoginTimeout() throws SQLException {return xads.getLoginTimeout();}
        };
    }
}
