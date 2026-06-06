package com.arjuna.ats.tools.perftest.product;

import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.TransactionManagerImp;
import com.atomikos.icatch.admin.jmx.JmxTransactionService;
import com.arjuna.ats.tools.perftest.DbWrapper;

import javax.transaction.*;
import javax.transaction.xa.XAException;
import javax.sql.DataSource;
import javax.management.*;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Properties;
import java.lang.management.ManagementFactory;

public class AtomikosProduct extends Product
{
    public String getName()
    {   
        return "Atomikos";
    }

    protected UserTransaction createTx() throws SystemException, SQLException
    {
        if (log.isDebugEnabled()) log.debug("creating Atomikos transaction");
        
        return new com.atomikos.icatch.jta.UserTransactionImp();
    }

    protected void beginTx() throws SystemException, NotSupportedException, SQLException, XAException
    {
        super.beginTx();
    }

    public TransactionManager getTransactionManager()
    {
        return com.atomikos.icatch.jta.TransactionManagerImp.getTransactionManager();
    }

    protected void commitTx(Connection[] connections) throws Exception
    {

        super.commitTx(connections);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected boolean startTxBeforeOpen()
    {
        return true;
    }

    protected boolean supportsDb(String db)
    {
        return true;
    }

    protected void enableJmx() throws JMException
    {
        JmxTransactionService service = new JmxTransactionService();
        MBeanServer jmx = ManagementFactory.getPlatformMBeanServer();
        ObjectName mBeanName = new ObjectName("atomikos:type=Transactions");
        jmx.registerMBean(service , mBeanName);
    }

    protected DataSource createDataSource(String name, Properties props) throws SQLException
    {
        if (log.isDebugEnabled()) log.debug("creating Atomikos data source");
        com.atomikos.jdbc.AtomikosDataSourceBean ds = new com.atomikos.jdbc.AtomikosDataSourceBean();

        ds.setXaDataSourceClassName(DbWrapper.getDbXADsClass(props));
        ds.setUniqueResourceName(name);

        ds.setPoolSize(4);
        ds.setXaProperties(DbWrapper.getDbProperties(props));

        if (log.isDebugEnabled()) log.debug("Data source: " + ds.getXaDataSourceClassName());

        return ds;
    }
}
