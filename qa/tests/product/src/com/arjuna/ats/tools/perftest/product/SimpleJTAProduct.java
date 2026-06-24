package com.arjuna.ats.tools.perftest.product;

import com.arjuna.ats.tools.perftest.product.Product;
import com.arjuna.ats.tools.perftest.DbWrapper;

import javax.transaction.*;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SimpleJTAProduct extends Product
{
    private static final String TMID = "TMGR.1";

    private Properties simpleJtaProps;

    public String getName()
    {
        return "SimpleJTA";
    }

    public TransactionManager getTransactionManager()
    {
        try
        {
            return org.simplejta.tm.SimpleTransactionManager.getTransactionManager(simpleJtaProps);
        }
        catch (SystemException e)
        {
            throw new RuntimeException(e);
        }
    }

    // WARNING putting a schema qualifier here means simpleJTA won't be able to find its logs
    // see  org.simplejta.tm.log.JDBCTransactionLog
    private static final String SJTA_CREATE1_SQL =
            "create table app.sjta_transactions (" +
                    "    tid bigint generated always as identity," +
                    "    tmid varchar(30)," +
                    "    formatid int," +
                    "    gtid varchar(64) for bit data," +
                    "    bqual varchar(64) for bit data," +
                    "    state smallint" +
                    ")";
    private static final String SJTA_CREATE2_SQL =
            "create table app.sjta_transaction_branches (" +
                    "       tid bigint," +
                    "       bid int," +
                    "       formatid int," +
                    "       gtid varchar(64) for bit data," +
                    "       bqual varchar(64) for bit data," +
                    "       state smallint," +
                    "       url varchar(128)," +
                    "       userid varchar(30)," +
                    "       password varchar(30)," +
                    "       typeid varchar(30)" +
                    ")";

    protected void init(Connection c, Properties props) throws SQLException
    {
        if (log.isDebugEnabled()) log.debug("init simpleJTA");

        String user = DbWrapper.getDbUser(props);
        String pass = DbWrapper.getDbPassword(props);
        String url = DbWrapper.getDbUrl(props);
        String db = DbWrapper.getDbName(props);

url = "jdbc:derby://localhost:1527/rc;create=true";
user = "app";
pass = "app";

//url = "jdbc:h2:tcp://localhost/rc;user=user1;password=pass1";
        

        simpleJtaProps = new Properties();

        simpleJtaProps.setProperty(org.simplejta.tm.SimpleTransactionManager.STM_TMGR_ID,
                TMID);
//        simpleJtaProps.setProperty(org.simplejta.tm.SimpleTransactionManager.STM_TMGR_RECO_USER,
//                user);
//        simpleJtaProps.setProperty(org.simplejta.tm.SimpleTransactionManager.STM_TMGR_RECO_PW,
//                pass);
        simpleJtaProps.setProperty(org.simplejta.tm.log.TransactionLogFactory.STM_TLOG_DRIVER,
                org.simplejta.tm.datasource.DerbyXAConnectionFactory.TYPEID);
        simpleJtaProps.setProperty(org.simplejta.tm.log.TransactionLogFactory.STM_TLOG_URL,
                url); //url); //DERBY_URL_PROP));
        simpleJtaProps.setProperty(org.simplejta.tm.log.TransactionLogFactory.STM_TLOG_USER,
                user);
        simpleJtaProps.setProperty(org.simplejta.tm.log.TransactionLogFactory.STM_TLOG_PASSWORD,
                pass);

// Never use an embedded db logging and recovery with SimpleJTA
//        c = DbWrapper.getConnection(url);
DbWrapper.loadDriver("org.apache.derby.jdbc.ClientDriver");
c = DriverManager.getConnection(url);
c.setAutoCommit(false);
        executeSql(c, SJTA_CREATE1_SQL, true);
        executeSql(c, SJTA_CREATE2_SQL, true);
c.commit();
c.close();
    }

    protected void fini()
    {
        org.simplejta.tm.SimpleTransactionManager tmgr;
        try
        {
            tmgr = org.simplejta.tm.SimpleTransactionManager.getTransactionManager(simpleJtaProps);
            tmgr.shutdown();
        }
        catch (SystemException e)
        {
            e.printStackTrace();
        }
    }

    protected UserTransaction createTx() throws SystemException, SQLException
    {
        return new org.simplejta.tm.ut.SimpleUserTransaction(simpleJtaProps);
    }

    protected boolean supportsDb(String db)
    {
        return DbWrapper.isDerby(db) || DbWrapper.isPostgresql(db) || DbWrapper.isOracle(db) || DbWrapper.isMssql(db);
    }

    protected DataSource createDataSource(String name, Properties props) throws SQLException
    {
        String type = DbWrapper.getDbType(props);
        String db = DbWrapper.getDbName(props);
        String user = DbWrapper.getDbUser(props);
        String pass = DbWrapper.getDbPassword(props);
        String url = DbWrapper.getDbUrl(props);

        if (DbWrapper.isDerby(type))
            return new org.simplejta.tm.datasource.SimpleDerbyXADataSource(TMID, db, user, pass);
        else if (DbWrapper.isPostgresql(type))
            return new org.simplejta.tm.datasource.SimplePostgreSqlXADataSource(TMID, url, user, pass); //TODO url -> properties
        else if (DbWrapper.isOracle(type))
            return new org.simplejta.tm.datasource.SimpleOracleXADataSource(TMID, url, user, pass);
        else if (DbWrapper.isMssql(type))
            return new org.simplejta.tm.datasource.SimpleMsSqlServerXADataSource(TMID, url, user, pass); //TODO url -> properties
        else
            return null; //new org.simplejta.tm.datasource.SimpleXADataSource(TMID, "need a typeid", url,user, pass);
    }

    protected boolean startTxBeforeOpen()
    {
        return true;
    }
}
