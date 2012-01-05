package com.arjuna.ats.tools.perftest.product;

import org.apache.log4j.Logger;

import javax.transaction.*;
import javax.transaction.xa.XAException;
import javax.sql.DataSource;
import javax.management.JMException;
import java.sql.*;
import java.util.*;
import java.io.*;

import com.arjuna.ats.tools.perftest.DbWrapper;
import com.arjuna.ats.tools.perftest.TestRunner;
import com.arjuna.ats.tools.perftest.PerfTest;

/**
 * Base class that each transaction prodcut should subclass. The main method controls test execution -
 * if any of the default behaviours supplied by this base class are not appropriate each product should
 * override that behaviour. Please consult subclasses for examples.
 */
public abstract class Product
{
    /**
     * Provide a human readable name for the product being tested
     * @return name of the product
     */
    public abstract String getName();

    /**
     * A compliant product implements TransactionManager
     * @return the products implementation of TransactionManager
     */
    public abstract TransactionManager getTransactionManager();

    /**
     * Some products require their own wrappers around javax.sql.DataSource
     * This method may be overridden to support such a product
     * @param name a name for the resource
     * @param props set of properties to set on the underlying resource manager
     * @return a wrapped data source
     * @throws SQLException
     */
    protected abstract DataSource createDataSource(String name, Properties props) throws SQLException;

    /**
     * Tests whether the product supports a particular database
     * @param db the database name that was extracted from the connect URL
     * @return true if the product works with the database
     */
    protected abstract boolean supportsDb(String db);

    public static void main(String[] args) throws Exception
    {
//        DbWrapper.startH2Server();

        // set up the test
        initialize(args);

        // and test each product
        for (prodIndex = 0; prodIndex < products.size(); prodIndex++)
            products.get(prodIndex).runTest();

        // write out the test results
        if (results != null)
            results.close();

        if (csvWriter != null)
        {
            for (Object[] row : csv)
            {
                for (Object val : row)
                {
                    csvWriter.print(val);
                    csvWriter.print(',');
                }

                csvWriter.println();
            }

            csvWriter.close();
        }

        for (Properties p : urls.values())
            DbWrapper.shutdownDb(p);

//        DbWrapper.stopDbServer();
    }

    protected void enableJmx() throws JMException
    {
    }

    protected void init(Connection c, Properties props) throws SQLException
    {
    }

    protected void init(Properties props)
    {
    }

    protected UserTransaction createTx() throws SystemException, SQLException
    {
        return null;
    }

    protected void beginTx() throws SystemException, NotSupportedException, SQLException, XAException
    {
        if (ut != null)
            ut.begin();
    }

    protected void commitTx(Connection[] connections) throws Exception
    {
//        suspendTx();
//        resumeTx();
        if (log.isInfoEnabled()) log.info("commit: ut=" + ut);
        if (ut != null)
            ut.commit();
    }

    protected void rollbackTx() throws SystemException, NotSupportedException
    {
        if (log.isInfoEnabled()) log.info("rollback: ut=" + ut);
        if (ut != null)
            ut.rollback();
    }

    protected void suspendTx() throws SQLException, XAException, SystemException
    {
        tx = getTransactionManager().suspend();
    }

    protected void resumeTx() throws SQLException, XAException, SystemException, InvalidTransactionException
    {
        getTransactionManager().resume(tx);
    }

    protected void openConnections(Connection[] connections) throws SQLException
    {
        for (int i = 0; i < dataSources.length; i++)
        {
            connections[i] = dataSources[i].getConnection();
            connections[i].setAutoCommit(false);
        }
    }

    protected void closeConnection(Connection c) throws SQLException
    {
        if (c != null && !c.isClosed())
            c.close();
    }

    private void closeConnections(Connection[] connections)
    {
        for (Connection c : connections)
        {
            try
            {
                closeConnection(c);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void fini()
    {
    }

    protected void executeStatement(Connection c, String sql, Object ... args) throws SQLException
    {
        executeStatement(prepareStatement(c, sql), args);
    }

    protected void executeStatement(PreparedStatement s, Object ... args) throws SQLException
    {
        for (int i = 0; i < args.length; i++)
            s.setObject(i + 1, args[i]);

        s.executeUpdate();
    }

    protected PreparedStatement prepareStatement(Connection c, String sql) throws SQLException
    {
        PreparedStatement ps = c.prepareStatement(sql);

        statements.add(ps);

        return ps;
    }

    protected void executeSql(Connection c, String sql, boolean ignoreErrors) throws SQLException
    {
        Statement s = c.createStatement();

        statements.add(s);

        if (log.isDebugEnabled()) log.debug("executeSql() " + sql);
        try
        {
            s.execute(sql);
        }
        catch (SQLException e)
        {
            if (!ignoreErrors)
                throw e;

            if (log.isDebugEnabled()) log.debug("executeSql() " + e.getMessage());
        }
    }

    /**
     * Some products require an active tx for resources to be automatically
     * enlisted on open
     *
     * @return true if a tx must be active for automatic enlistment
     */
    protected boolean startTxBeforeOpen()
    {
        return false;
    }

    private static void addUrl(String url)
    {
        int i = url.indexOf('=') + 1;

        if (i == 0 || i == url.length())
            throw new IllegalArgumentException("Invalid jdbc url: " + url);

        urls.put(url.substring(i), new Properties());
    }

    private static int[] parseIntArray(String sa)
    {
        String[] va = sa.split(",");
        int[] values = new int[va.length];

        for (int i = 0; i < va.length; i++)
            values[i] = Integer.parseInt(va[i]);

        return values;
    }

    private static void initialize(String[] args) throws IllegalArgumentException
    {
        String[] users = null;
        String[] passwords = null;
        String xargs = System.getProperty("xargs");
        Set<String> dbs = new HashSet<String> ();
        Properties testProps = new Properties();
        Properties runProps = new Properties();

        for (String arg : args)
        {
            int ind = arg.indexOf('=');

            if (ind != -1 && ind + 1 != arg.length())
                runProps.setProperty(arg.substring(0, ind), arg.substring(ind + 1));
        }

        testProps.put("tx.count", "1");
        testProps.put("debug", "0");

        for (String arg : args)
        {
            if (arg.startsWith(TXCOUNT_PROP + '='))
                TXCOUNT = parseIntArray(arg.split("=")[1]);
            else if (arg.startsWith(THRCOUNT_PROP + '='))
                THRCOUNT = parseIntArray(arg.split("=")[1]);
            else if (arg.startsWith(DEBUG_PROP + '='))
                testProps.put(DEBUG_PROP, arg.split("=")[1]);
            else if (arg.startsWith(DEBUGLOG_PROP + '='))
                testProps.put(DEBUGLOG_PROP, arg.split("=")[1]);
            else if (arg.startsWith(PRODUCTS_PROP + '='))
                testProps.put(PRODUCTS_PROP, arg.split("=")[1]);
            else if (arg.startsWith(DB_URL_PROP + '='))
                addUrl(arg);
            else if (arg.startsWith(DB_USER_PROP + '='))
                users = arg.split("=")[1].split(",", -1);
            else if (arg.startsWith(DB_PASSWORD_PROP + '='))
                passwords = arg.split("=")[1].split(",", -1);
            else if (arg.startsWith(RESULT_FILE_PROP + '='))
                openResults(arg.split("=")[1]);
            else if (arg.startsWith(CSV_FILE_PROP + '='))
                csvWriter = openFileWriter(arg.split("=")[1], true);
        }

        if (xargs != null)
        {
            for (String arg : xargs.split("%"))
            {
            if (arg.startsWith(TXCOUNT_PROP + '='))
                TXCOUNT = parseIntArray(arg.split("=")[1]);
            else if (arg.startsWith(THRCOUNT_PROP + '='))
                THRCOUNT = parseIntArray(arg.split("=")[1]);
            else if (arg.startsWith(DEBUG_PROP + '='))
                testProps.put(DEBUG_PROP, arg.split("=")[1]);
            else if (arg.startsWith(DEBUGLOG_PROP + '='))
                testProps.put(DEBUGLOG_PROP, arg.split("=")[1]);
            else if (arg.startsWith(PRODUCTS_PROP + '='))
                testProps.put(PRODUCTS_PROP, arg.split("=")[1]);
            else if (arg.startsWith(DB_URL_PROP + '='))
                addUrl(arg);
            else if (arg.startsWith(DB_USER_PROP + '='))
                users = arg.split("=")[1].split(",", -1);
            else if (arg.startsWith(DB_PASSWORD_PROP + '='))
                passwords = arg.split("=")[1].split(",", -1);
            else if (arg.startsWith(RESULT_FILE_PROP + '='))
                openResults(arg.split("=")[1]);
            else if (arg.startsWith(CSV_FILE_PROP + '='))
                csvWriter = openFileWriter(arg.split("=")[1], false);
            else if (arg.startsWith(SYNC_PROP + '='))
                runProps.put(SYNC_PROP, arg.split("=")[1]);
            else if (arg.startsWith(STORE_PROP + '='))
                runProps.put(STORE_PROP, arg.split("=")[1]);
            else if (arg.startsWith(DELAY_PROP + '='))
                runProps.put(DELAY_PROP, arg.split("=")[1]);
            }
        }
/*
        if (urls.size() == 0)
            throw new RuntimeException("Please specify one or more jdbc connect urls using the syntax " + DB_URL_PROP + "=<connect url>");

        if (users == null || passwords == null || users.length != passwords.length || users.length < urls.size())
            throw new RuntimeException("The number of db urls, users and passwords are not equal");
*/
        int i = 0;

        for (Map.Entry<String, Properties> me : urls.entrySet())
        {
            Properties p = me.getValue();
            String url = me.getKey();

            p.put(DB_USER_PROP, users[i]);
            p.put(DB_PASSWORD_PROP, passwords[i++]);

            DbWrapper.parseUrl(url, p);

            dbs.add(DbWrapper.getDbType(p));
            p.putAll(testProps);
            DbWrapper.loadDriver(p);
        }

        // add the relevant drivers to the jdbc.drivers system property
/*
        StringBuilder sb = new StringBuilder();
        for (String db : dbs)
        {
            if (sb.length() != 0)
                sb.append(':');

            sb.append(db);
        }
        System.getProperties().put("jdbc.drivers", sb.toString());
*/

        String pprop = System.getProperty(PRODUCTS_PROP);

        if (pprop == null || pprop.length() == 0)
            pprop = (String) testProps.get(PRODUCTS_PROP);

        if (pprop != null && pprop.length() != 0)
        {
            for (String clazz : pprop.split(","))
            {
                Product p = null;
                boolean canRun = false;
                try
                {
                    p = loadProduct(clazz);
                    p.init(runProps);
                    canRun = true;

                    for (String type : dbs)
                    {
                        if (!p.supportsDb(type))
                        {
                            if (DbWrapper.isWarnEnabled(log)) log.warn("Product " + p.getName() + " does not support db type " + type);
                            canRun = false;
                        }
                    }
                }
                catch (IllegalArgumentException e)
                {
                    if (DbWrapper.isWarnEnabled(log)) log.warn("Error initializing product " + clazz + ": " + e.getMessage());
                    //e.printStackTrace();
                }

                if (canRun)
                {
                    try
                    {
                        p.enableJmx();
                        products.add(p);
                    }
                    catch (JMException e)
                    {
                        if (DbWrapper.isWarnEnabled(log)) log.warn("Product " + p.getName() + ": JMX error: " + e.getMessage());
                    }
                }
            }
        }

        if (csvWriter != null)
        {
            // create array to hold results. Add 1 to row count for the headers
            // and add 2 to col count to include thread and tx count columns
            csv = new Object[1 + THRCOUNT.length][2 + products.size()];
            csv[0][0] = "Threads";
            csv[0][1] = "Tx";

            for (i = 0; i < products.size(); i++)
                csv[0][i + 2] = products.get(i).getName();

            for (i = 1; i <= THRCOUNT.length; i++)
            {
                for (int j = 0; j < TXCOUNT.length; j++)
                {
                    csv[i][0] = THRCOUNT[i - 1];
                    csv[i][1] = TXCOUNT[j];
                }
            }
        }
    }

    private static Product loadProduct(String className) throws IllegalArgumentException
    {
        try
        {
            return (Product) Class.forName(className).newInstance();
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException(className + " does not extend Product");
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Unable to instantiate " + className + ": " + e.getMessage());
        }
    }

    private void setup() throws SQLException
    {
        int i = 0;

        dataSources = new DataSource[urls.size()];
        DataSource[] adminDs = new DataSource[urls.size()];

        for (Properties p : urls.values())
        {
            DataSource ds = new NonXAProduct().createDataSource("nonXA", p);
            Connection c = ds.getConnection();
            String name = DbWrapper.getDbName(p);

            c.setAutoCommit(false);

            executeSql(c, "CREATE SCHEMA " + name, true);
            executeSql(c, SQLT1.replace("$DB", name), true);
            executeSql(c, SQLT2.replace("$DB", name), true);

            init(c, p);
            c.commit();
            c.close();

            adminDs[i] = ds;

            dataSources[i] = createDataSource("ds" + i, p);
            i += 1;
        }
    }

    private void runTest()
    {
        if (log.isDebugEnabled()) log.debug("Testing product " + getName());

        try
        {
            setup();
            ut = createTx();

            try
            {

                test(new TestRunner(), 1, 1);

                for (int i = 0; i < THRCOUNT.length; i++)
                {
                    for (int j = 0; j < TXCOUNT.length; j++)
                    {
                        TestRunner tr = test(new TestRunner(), THRCOUNT[i], TXCOUNT[j]);

                        writeResults(tr, TXCOUNT[j]);
//                        csv[i + 1][prodIndex + 2] = (float) tr.getAvg() / TXCOUNT[j];
                        csv[i + 1][prodIndex + 2] = TXCOUNT[j] * 1000 / tr.getAvg();
                    }
                }
            }
            catch (SQLException e)
            {
                DbWrapper.logSQLException(log, e);
                rollbackTx();
            }
        }
        catch (SQLException e)
        {
            DbWrapper.logSQLException(log, e);
        }
        catch (Throwable e)
        {
            log.error("Fatal: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            releaseResources();
        }
    }

    private void releaseResources()
    {
        if (log.isInfoEnabled()) log.info("releasing resources");

        for (Statement statement : statements)
        {
            try
            {
                if (statement != null)
                    statement.close();
            }
            catch (SQLException sqle) {
                DbWrapper.logSQLException(log, sqle);
            }
        }

        fini();
    }


    private String[] replaceSql(String sql)
    {
        String[] sqla = new String[urls.size()];
        int i = 0;

        for (Properties p : urls.values())
            sqla[i++] = sql.replace("$DB", DbWrapper.getDbName(p));

        return sqla;
    }

    private TestRunner test(TestRunner tr, int threadCount, int txCount) throws Exception
    {
        for (int i = 0; i < threadCount; i++)
            tr.addTest(getTest(String.valueOf(i), txCount));

        tr.startTests();
        tr.waitOn();

        return tr;
    }

    private PerfTest getTest(final String name, final int txCount)
    {
        return new PerfTest() {
            private long rt = -1;
            private Exception exception;
            private String n = name;
            private int count = txCount;

            public long getResult()
            {
                return rt;
            }

            public String getName()
            {
                return n;
            }

            public int getTxCount()
            {
                return count;
            }

            public Exception getException()
            {
                return exception;
            }

            public void run()
            {
                try
                {
                    if (log.isInfoEnabled()) log.info("Test " + Thread.currentThread().getName() + " is starting ...");
                    String[] sql = replaceSql(SQLT1_I);
                    Connection[] connections = new Connection[dataSources.length];

                    if (!startTxBeforeOpen())
                        openConnections(connections);
                    
                    long t = System.currentTimeMillis();
                    PreparedStatement ps = null;

                    for (int cnt = 0; cnt < txCount; cnt++)
                    {
                        boolean started = false;

                        try
                        {
                            beginTx();
                            started = true;

                            if (startTxBeforeOpen())
                                openConnections(connections);

                            for (int i = 0; i < connections.length; i++)
                            {
                                try
                                {
                                    ps = prepareStatement(connections[i], sql[i]);
                                    executeStatement(ps, cnt, "Text");
                                }
                                catch (SQLException e)
                                {
                                    log.error("Transaction #" + cnt + " db: " + i);
                                    e.printStackTrace();
                                    throw e;
                                }
                                finally
                                {
                                    if (ps != null)
                                        ps.close();
                                }
                            }

                            if (cnt % 10 == 0 && cnt > 0)
                                Thread.yield();

                            if (startTxBeforeOpen())
                                closeConnections(connections);
                            commitTx(connections);
                            started = false;
                        }
                        finally
                        {
                            if (started)
                            {
                                try
                                {
                                    rollbackTx();
                                }
                                catch (Exception e)
                                {
                                    exception = e;
                                    log.error(e.getMessage());
                                }
                            }
                        }
                    }

                    rt = System.currentTimeMillis() - t;

                    if (!startTxBeforeOpen())
                        closeConnections(connections);
                }
                catch (Exception e)
                {
                    exception = e;
                    log.error(e.getMessage());
                }

                if (log.isInfoEnabled()) log.info("Test " + Thread.currentThread().getName() + " has finished");
            }
        };
    }

    private void writeResults(TestRunner tr, int txCount) throws SQLException
    {
        StringBuilder types = new StringBuilder();

        for (Properties p : urls.values())
        {
            if (types.length() != 0)
                types.append(',');
            types.append(DbWrapper.getDbType(p));
        }
/*
        String[] sql = replaceSql(SQLT2_I);

        for (int i = 0; i < sql.length; i++)
        {
            Connection c = adminDs[i].getConnection();
            executeStatement(c, sql[i], getName(), types.toString(), tr.getPassCount(), tr.getFailCount(), tr.getMax(), tr.getMin(), tr.getAvg(), txCount);
            c.commit();
            c.close();
        }
*/
        //%[argument_index$][flags][width][.precision]conversion
        int tp = (int) (txCount * 1000 / tr.getAvg());
        
        results.format("%1$-32s %2$-16s %3$-8d %4$-8d %5$-8d %6$-8d %7$-8d %8$-8d %9$-9d\n", // %10$-32s\n", //%9$7.2f\n",
                getName(), types.toString(), tr.getPassCount(), tr.getFailCount(), tr.getMax(), tr.getMin(), tr.getAvg(), txCount,
                tp); //, tr.getTimes()); //(float) tr.getAvg() / txCount);
        System.out.println(getName() + '\t' + txCount + '\t' + txCount * 1000 / tr.getAvg());
    }

    private static PrintWriter openFileWriter(String fileName, boolean append)
    {
        System.out.println("output file: " + fileName);
        try
        {
            return new PrintWriter(new FileOutputStream(fileName, append));
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException("Error opening resuts file: " + e.getMessage());
        }
    }

    private static void openResults(String fileName)
    {
        System.out.println("output file: " + fileName);
        try
        {
            File file = new File(fileName);
            boolean exists = file.exists();

            if ("true".equals(System.getProperty("results")))
            {
                if (exists)
                {
                    InputStream in = new FileInputStream(file);
                    OutputStream out = System.out;
                    byte[] buf = new byte[1024];
                    int len;

                    try
                    {
                        while ((len = in.read(buf)) > 0)
                            out.write(buf, 0, len);

                        in.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

                System.exit(0);
            }

            results = openFileWriter(fileName, true);

            if (!exists)
            {
                results.format("%1$-32s %2$-16s %3$-8s %4$-8s %5$-8s %6$-8s %7$-8s %8$-8s %9$8s\n",
                        "Product", "Database(s)", "Pass", "Fail", "Max", "Min", "Average", "Count", "tx/sec");
                results.format("%1$-32s %2$-16s %3$-8s %4$-8s %5$-8s %6$-8s %7$-8s %8$-8s %9$8s\n",
                        "------", "-----------", "----", "----", "---", "---", "-------", "-----", "------");
            }
        }
        catch (FileNotFoundException e)
        {
            throw new IllegalArgumentException("Error opening resuts file: " + e.getMessage());
        }
    }

    protected final static Logger log = Logger.getLogger(Product.class);

    private static String SQLT1 = "create table $DB.TEST(id int, value varchar(40))";
    private static String SQLT1_I = "insert into $DB.TEST values (?, ?)";
    private static String SQLT2 = "create table $DB.RESULT(product varchar(64), dbvendor varchar(32), pass int, fail int, maxt int, mint int, avgt int, count int)";
    private static String SQLT2_I = "insert into $DB.RESULT values (?, ?, ?, ?, ?, ?, ?, ?)";

    protected static final String DB_URL_PROP = "db.url";
    protected static final String DB_USER_PROP = "db.user";
    protected static final String DB_PASSWORD_PROP = "db.password";
    protected static final String PRODUCTS_PROP = "products";
    protected static final String DEBUG_PROP = "debug.level";
    protected static final String DEBUGLOG_PROP = "debug.file";
    protected static final String RESULT_FILE_PROP = "result.file";
    protected static final String CSV_FILE_PROP = "csv.file";
    protected static final String TXCOUNT_PROP = "tx.count";
    protected static final String THRCOUNT_PROP = "thread.count";
    protected static final String SYNC_PROP = "store.sync";
    protected static final String DELAY_PROP = "store.asyncTypes";
    protected static final String STORE_PROP = "store.types";
    protected static final String CSV_PROP = "csv";

    private static List<Product> products = new ArrayList<Product> ();
    private static Map<String, Properties> urls = new LinkedHashMap<String, Properties> ();

    private static int[] TXCOUNT = {100};
    private static int[] THRCOUNT = {1};

    private static PrintWriter results;
    private static PrintWriter csvWriter;
    private static Object[][] csv;
    private static int prodIndex;

    private List<Statement> statements = new ArrayList<Statement> ();
    private javax.transaction.UserTransaction ut;
    private Transaction tx;

    private DataSource[] dataSources;
}
