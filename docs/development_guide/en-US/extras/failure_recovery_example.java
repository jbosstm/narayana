/*
 * Some XAResourceRecovery implementations will do their startup work here,
 * and then do little or nothing in setDetails. Since this one needs to know
 * dynamic class name, the constructor does nothing.
 */

public BasicXARecovery () throws SQLException
{
    numberOfConnections = 1;
    connectionIndex = 0;
    props = null;
}

/**
 * The recovery module will have chopped off this class name already. The
 * parameter should specify a property file from which the url, user name,
 * password, etc. can be read.
 * 
 * @message com.arjuna.ats.internal.jdbc.recovery.initexp An exception
 *          occurred during initialisation.
 */

public boolean initialise (String parameter) throws SQLException
{
    if (parameter == null) 
        return true;

    int breakPosition = parameter.indexOf(BREAKCHARACTER);
    String fileName = parameter;

    if (breakPosition != -1)
        {
            fileName = parameter.substring(0, breakPosition - 1);

            try
                {
                    numberOfConnections = Integer.parseInt(parameter
                                                           .substring(breakPosition + 1));
                }
            catch (NumberFormatException e)
                {
                    return false;
                }
        }

    try
        {
            String uri = com.arjuna.common.util.FileLocator
                .locateFile(fileName);
            jdbcPropertyManager.propertyManager.load(XMLFilePlugin.class
                                                     .getName(), uri);

            props = jdbcPropertyManager.propertyManager.getProperties();
        }
    catch (Exception e)
        {
            return false;
        }

    return true;
}

/**
 * @message com.arjuna.ats.internal.jdbc.recovery.xarec {0} could not find
 *          information for connection!
 */

public synchronized XAResource getXAResource () throws SQLException
{
    JDBC2RecoveryConnection conn = null;

    if (hasMoreResources())
        {
            connectionIndex++;

            conn = getStandardConnection();

            if (conn == null) conn = getJNDIConnection();
        }

    return conn.recoveryConnection().getConnection().getXAResource();
}

public synchronized boolean hasMoreResources ()
{
    if (connectionIndex == numberOfConnections) 
        return false;
    else
        return true;
}

private final JDBC2RecoveryConnection getStandardConnection ()
    throws SQLException
{
    String number = new String("" + connectionIndex);
    String url = new String(dbTag + number + urlTag);
    String password = new String(dbTag + number + passwordTag);
    String user = new String(dbTag + number + userTag);
    String dynamicClass = new String(dbTag + number + dynamicClassTag);

    Properties dbProperties = new Properties();

    String theUser = props.getProperty(user);
    String thePassword = props.getProperty(password);

    if (theUser != null)
        {
            dbProperties.put(TransactionalDriver.userName, theUser);
            dbProperties.put(TransactionalDriver.password, thePassword);

            String dc = props.getProperty(dynamicClass);

            if (dc != null)
                dbProperties.put(TransactionalDriver.dynamicClass, dc);

            return new JDBC2RecoveryConnection(url, dbProperties);
        }
    else
        return null;
}

private final JDBC2RecoveryConnection getJNDIConnection ()
    throws SQLException
{
    String number = new String("" + connectionIndex);
    String url = new String(dbTag + jndiTag + number + urlTag);
    String password = new String(dbTag + jndiTag + number + passwordTag);
    String user = new String(dbTag + jndiTag + number + userTag);

    Properties dbProperties = new Properties();

    String theUser = props.getProperty(user);
    String thePassword = props.getProperty(password);

    if (theUser != null)
        {
            dbProperties.put(TransactionalDriver.userName, theUser);
            dbProperties.put(TransactionalDriver.password, thePassword);

            return new JDBC2RecoveryConnection(url, dbProperties);
        }
    else
        return null;
}

private int numberOfConnections;
private int connectionIndex;
private Properties props;
private static final String dbTag = "DB_";
private static final String urlTag = "_DatabaseURL";
private static final String passwordTag = "_DatabasePassword";
private static final String userTag = "_DatabaseUser";
private static final String dynamicClassTag = "_DatabaseDynamicClass";
private static final String jndiTag = "JNDI_";

/*
 * Example:
 * 
 * DB2_DatabaseURL=jdbc\:arjuna\:sequelink\://qa02\:20001
 * DB2_DatabaseUser=tester2 DB2_DatabasePassword=tester
 * DB2_DatabaseDynamicClass=com.arjuna.ats.internal.jdbc.drivers.sequelink_5_1
 * 
 * DB_JNDI_DatabaseURL=jdbc\:arjuna\:jndi DB_JNDI_DatabaseUser=tester1
 * DB_JNDI_DatabasePassword=tester DB_JNDI_DatabaseName=empay
 * DB_JNDI_Host=qa02 DB_JNDI_Port=20000
 */
private static final char BREAKCHARACTER = ';'; // delimiter for parameters