package com.arjuna.ats.tools.perftest;

import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.tools.perftest.product.Product;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.xa.Xid;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.Hashtable;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;

import org.apache.log4j.Logger;

public class DbWrapper
{
    private final static Logger log = Logger.getLogger(Product.class);
    private static final String JNDIBASE = "jndi/";

    public static DataSource initMysqlDS(XADataSource xds, String user, String pass, String url)
    {
        log.fatal("Mysql support is disabled - please uncomment the implementation of " + DbWrapper.class.getCanonicalName() + ".initMysqlDS");

        return null;
/*
        com.mysql.jdbc.jdbc2.optional.MysqlXADataSource ds = (com.mysql.jdbc.jdbc2.optional.MysqlXADataSource) xds;
        ds.setUser(user);
        ds.setPassword(pass);
        ds.setURL(url);

        return ds;
*/
    }

    public static DataSource initDerbyDS(String dsName, final Properties props, String host, String user, String pass)
    {
        log.fatal("Derby support is disabled - please uncomment the implementation of " + DbWrapper.class.getCanonicalName() + ".initDerbyDS");
        return null;
/*
        String dbName = getDbName(props);
        Integer port = getDbPort(props);

        if (host != null)
        {
            org.apache.derby.jdbc.ClientXADataSource cds = new org.apache.derby.jdbc.ClientXADataSource();

            cds.setServerName (host);
            cds.setDatabaseName(dbName);
            if (port != null)
                cds.setPortNumber(port);
            cds.setUser(user);
            cds.setPassword(pass);
            cds.setDataSourceName(dsName);
            cds.setConnectionAttributes((String) props.get("connectionAttributes"));
            if (props.get("traceLevel") != null)
            {
                cds.setTraceFile((String) props.get("debug.file"));
                cds.setTraceLevel(Integer.parseInt((String) props.get("debug.level")));
            }
            return cds;
        }
        else
        {
            org.apache.derby.jdbc.EmbeddedXADataSource eds = new org.apache.derby.jdbc.EmbeddedXADataSource();

            eds.setDatabaseName(dbName);
            eds.setUser(user);
            eds.setPassword(pass);
            eds.setDataSourceName(dsName);
//                eds.setConnectionAttributes((String) props.get("connectionAttributes"));
            eds.setConnectionAttributes((String) props.get("db.opts"));

            return eds;
        }
*/
    }

    public static DataSource createDataSource(String dsName, final Properties props) throws SQLException
    {
        String db = DbWrapper.getDbType(props);
        String url = DbWrapper.getDbUrl(props);
        String user = DbWrapper.getDbUser(props);
        String pass = DbWrapper.getDbPassword(props);
        String host = getDbHost(props);
        boolean embedded = host == null;

        if (isMysql(db))
        {
            XADataSource xds = createDataSource(getDbXADsClass(db, embedded));

            return initMysqlDS(xds, user, pass, url);
        }
        else if (isH2(db))
        {
            org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();

            ds.setUser(user);
            ds.setPassword(pass);

            int i = url.indexOf(';');

            if (i != -1)
                url = url.substring(0, i);

            ds.setURL(url);

            return ds;
        }
        else if (isDerby(db))
        {
            return initDerbyDS(dsName, props, host, user, pass);
        }
        else
        {
            return null;
        }
    }

    public static java.sql.Connection getConnection(String url) throws SQLException
    {
        Properties props = new Properties();

        parseUrl(url, props);

        DataSource ds = createDataSource("", props);

        return ds.getConnection();
    }

    public static Properties getDbProperties(Properties props)
    {
        Properties p = new Properties();
        String type = getDbType(props);
        String name = getDbName(props);

        if (isDerby(type))
        {
            // NB derby.system.durability=test disables I/O synchronization
            p.put("databaseName", name);
            p.put("createDatabase", "true");
            p.put("shutdownDatabase", "false");
            p.put("dataSourceName", name);
//            p.put("description", name);
//            String opts = getDbOpts(p);
//            if (opts != null)
//                p.put("connectionAttributes", opts.replace('&', ';'));
        }
        else if (isH2(type))
        {
            p.put("user", getDbUser(props));
            p.put("password", getDbPassword(props));
            p.put("URL", getDbUrl(props));
        }
        else if (isMysql(type))
        {
            p.put("user", getDbUser(props));
            p.put("password", getDbPassword(props));
        }

        return p;
    }

    /*
     * decode a jdbc connectin url

    jdbc:h2:tcp://localhost/rc;user=user1;password=pass1
    jdbc:h2:db2"
    jdbc:derby:db1;create=true
    jdbc:derby://localhost:1527/db1;create=true
    jdbc:mysql://localhost:3306/db1?createDatabaseIfNotExist=true

     */
    private static final String[] URLS = {
            "jdbc:h2:tcp:",
            "jdbc:h2:",
            "jdbc:derby:net:",
            "jdbc:derby://",
            "jdbc:derby:",
            "jdbc:mysql:",
            "jdbc:postgresql:",
    };

    public static void parseUrl(String url, Properties props)
    {
        props.put("db.url", url);

        url = url.replace("jdbc:", "").replaceFirst(";", "?").replace(';', '&');

        if (url.startsWith("h2:tcp:"))
            url = url.replace(":tcp", "");

        try
        {
            URI uri = new URI(url);

            props.put("db.type", uri.getScheme());
            if (uri.getHost() != null) props.put("db.host", uri.getHost());
            if (uri.getPort() != -1) props.put("db.port", uri.getPort());
            if (uri.getPath() != null)
                props.put("db.name", uri.getPath().replace("/", ""));
            else
                props.put("db.name", parseDbName(uri.getSchemeSpecificPart()));
            if (uri.getQuery() != null)
                props.put("db.opts", uri.getQuery());
            else if (url.indexOf('?') != -1 && url.indexOf('?') + 1 != url.length())
                props.put("db.opts", url.substring(url.indexOf('?') + 1));
            else
                props.put("db.opts", "");

            props.put("db.driver", getDbDriver(getDbType(props), getDbHost(props) == null));
            props.put("db.xaclass", getDbXADsClass(getDbType(props), getDbHost(props) == null));

            String opts = (String) props.get("db.opts");
            for (String opt : opts.split("&"))
            {
                String[] nvl = opt.split("=");
//                props.put(nvl[0], nvl[1]);
                if ("user".equals(nvl[0]))
                    props.put("db.user", nvl[1]);
                if ("password".equals(nvl[0]))
                    props.put("db.password", nvl[1]);
            }

            props.put("db.opts", opts);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    private static String parseDbName(String s)
    {   //jdbc:derby:db2
        int i = s.indexOf('?');

        return (i == -1 ? s : s.substring(0, i));
    }

    public static String getDbType(Properties props)
    {
        return (String) props.get("db.type");
    }
    public static String getDbHost(Properties props)
    {
        return (String) props.get("db.host");
    }
    public static Integer getDbPort(Properties props)
    {
        return (Integer) props.get("db.port");
    }
    public static String getDbName(Properties props)
    {
        return (String) props.get("db.name");
    }
    public static String getDbOpts(Properties props)
    {
        return (String) props.get("db.opts");
    }
    public static String getDbDriver(Properties props)
    {
        return (String) props.get("db.driver");
    }
    public static String getDbXADsClass(Properties props)
    {
        return (String) props.get("db.xaclass");
    }
    public static String getDbUrl(Properties props)
    {
        return (String) props.get("db.url");
    }
    public static String getDbUser(Properties props)
    {
        return (String) props.get("db.user");
    }
    public static boolean isEmbedded(Properties props)
    {
        return getDbHost(props) == null;
    }
    public static String getDbPassword(Properties props)
    {
        return (String) props.get("db.password");
    }
    public static void shutdownDb(Properties props)
    {
        if (isDerby(getDbType(props)) && isEmbedded(props))
        {
            String db = getDbName(props);

            try
            {
                DriverManager.getConnection("jdbc:derby:" + db + ";shutdown=true");
            }
            catch (SQLException e)
            {
                // XJ015 and 08006 error codes indicate successfull shutdown
                if (!"XJ015".equals(e.getSQLState()) && !"08006".equals(e.getSQLState()))
                    logSQLException(log, e);
                else if (log.isInfoEnabled())
                    log.info(e.getMessage());
            }
        }
    }

    public static Xid createJJHXid() {
        return new XidImple(new Uid());
    }

    public static void loadDriver(String driverClass)
    {
        createInstance(driverClass);
    }

    public static void loadDriver(Properties props)
    {
        loadDriver(getDbDriver(props));
    }

    public static XADataSource createDataSource(String className)
    {
        return (XADataSource) createInstance(className);
    }

    private static Object createInstance(String className)
    {
        try
        {
            return Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String getDbDriver(String db, boolean isEmbedded)
    {
        if (DbWrapper.isMysql(db))
            return "com.mysql.jdbc.Driver";
        else if (DbWrapper.isDerby(db))
            return isEmbedded ? "org.apache.derby.jdbc.EmbeddedDriver" : "org.apache.derby.jdbc.ClientDriver";
        else if (DbWrapper.isH2(db))
            return "org.h2.Driver";
        else
            return "";
//        return mysql
    }

    public static String getDbXADsClass(String db, boolean isEmbedded)
    {
        if (DbWrapper.isMysql(db))
            return "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
        else if (DbWrapper.isPostgresql(db))
            return "org.postgresql.xa.PGXADataSource";
        else if (DbWrapper.isDerby(db))
            return isEmbedded ? "org.apache.derby.jdbc.EmbeddedXADataSource" : "org.apache.derby.jdbc.ClientXADataSource";
        else if (DbWrapper.isH2(db))
            return "org.h2.jdbcx.JdbcDataSource";
        else
            return "";
    }

    public static boolean isDerby(String db)
    {
        return "derby".equals(db);
    }
    public static boolean isH2(String db)
    {
        return "h2".equals(db);
    }

    public static boolean isMysql(String db)
    {
        return "mysql".equals(db);
    }

    public static boolean isPostgresql(String db)
    {
        return "postgresql".equals(db);
    }

    public static boolean isMssql(String db)
    {
        return "mssql".equals(db);
    }

    public static boolean isOracle(String db)
    {
        return "oracle".equals(db);
    }

    public static String initJndi(String bindName)
    {
        File jndi = new File(new File(JNDIBASE + bindName).getParent());

        jndi.mkdirs();

        String jndiUrl = "file://" + new File(JNDIBASE).getAbsolutePath();

        // some products take their jndi setting kfrom system properties:
        System.setProperty("Context.INITIAL_CONTEXT_FACTORY", "com.sun.jndi.fscontext.RefFSContextFactory");
        System.setProperty("Context.PROVIDER_URL", jndiUrl);

        if (log.isInfoEnabled()) log.info("jndi url: " + jndiUrl);

        return jndiUrl;
    }

    public static javax.naming.InitialContext jndiBind(String url, String bindName, Object obj) throws NamingException
    {
        Hashtable<String, String> env = new Hashtable<String, String> ();

        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        env.put(javax.naming.Context.PROVIDER_URL, url);

        javax.naming.InitialContext context = new javax.naming.InitialContext(env);

        context.rebind (bindName, obj);

        return context;
    }


    public static boolean isWarnEnabled(Logger log)
    {
        return log.isEnabledFor(org.apache.log4j.Priority.WARN);
    }

    public static void logSQLException(Logger log, SQLException e)
    {
        // Unwraps the entire exception chain to unveil the real cause of the exception

        if (isWarnEnabled(log))
        {
            while (e != null)
            {
                log.info("\n----- SQLException -----");
                log.info("  SQL State:  " + e.getSQLState());
                log.info("  Error Code: " + e.getErrorCode());
                log.info("  Message:    " + e.getMessage());

                e.printStackTrace();
                e = e.getNextException();
            }
        }
    }

    private static org.h2.tools.Server server;
    private static String[] xh2DbUrl = {"jdbc:h2:tcp://172.16.130.129/rc"};
    static String[] h2DbUrl = {"jdbc:h2:tcp://localhost/rc;user=user1;password=pass1"}; // h2.baseDir sets the base dir for the db

    public static synchronized void startH2Server() throws SQLException
    {
        startH2Server(h2DbUrl);
    }

    public static synchronized void startH2Server(String[] url) throws SQLException
    {
        if (server == null)
            server = org.h2.tools.Server.createTcpServer(url).start();

//        org.h2.jdbcx.JdbcDataSource ds = new org.h2.jdbcx.JdbcDataSource();
//        ds.setURL(h2DbUrl[0]);

//        ds.setUser("user1");
//        ds.setPassword("pass1");

//        ds.getConnection();
        boolean test = false;

        if (test)
        {
            java.sql.Connection c = getConnection(h2DbUrl[0]);
            java.sql.Statement s = c.createStatement();

            s.execute("create table TEST(id int, value varchar(40))");
            s.close();
            c.close();
        }
    }

    public static synchronized boolean stopDbServer()
    {
        if (server != null && server.isRunning(true))
            server.stop();

        server = null;

        return true;
    }
}
