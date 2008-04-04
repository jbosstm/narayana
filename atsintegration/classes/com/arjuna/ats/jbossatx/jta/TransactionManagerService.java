/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManagerService.java,v 1.5 2005/06/24 15:24:15 kconner Exp $
 */
package com.arjuna.ats.jbossatx.jta;

import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.system.server.Server;
import org.jboss.system.server.ServerConfig;
import org.jboss.tm.JBossXATerminator;
import org.jboss.tm.LastResource;
import org.jboss.tm.XAExceptionFormatter;

import com.arjuna.ats.internal.jbossatx.jta.jca.XATerminator;
import com.arjuna.ats.internal.jbossatx.agent.LocalJBossAgentImpl;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.ats.jta.common.Environment;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.coordinator.TxStats;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import com.arjuna.ats.internal.tsmx.mbeans.PropertyServiceJMXPlugin;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.logging.LogFactory;

import javax.management.InstanceNotFoundException;
import javax.management.Notification;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.net.Socket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * JBoss Transaction Manager Service.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: TransactionManagerService.java,v 1.5 2005/06/24 15:24:15 kconner Exp $
 */
public class TransactionManagerService extends ServiceMBeanSupport implements TransactionManagerServiceMBean, NotificationListener
{
    static {
		/*
		 * Override the defualt logging config, force use of the plugin that rewrites log levels to reflect app server level semantics.
		 * This must be done before the loading of anything that uses the logging, otherwise it's too late to take effect.
		 * Hence the static initializer block.
		 * see also http://jira.jboss.com/jira/browse/JBTM-20
		 */
		com.arjuna.ats.arjuna.common.arjPropertyManager.propertyManager.setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler");
		//System.setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler") ;
	}

	public final static String PROPAGATE_FULL_CONTEXT_PROPERTY = "com.arjuna.ats.jbossatx.jta.propagatefullcontext";

    private final static String SERVICE_NAME = "TransactionManagerService";
    private final static String PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextImporter";
    private final static String PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextExporter";
    private static final JBossXATerminator TERMINATOR = new XATerminator() ;

	private RecoveryManager _recoveryManager;
    private boolean _runRM = true;
    private int timeout ;
    private boolean started ;
    private byte[] startedLock = new byte[0] ;

    /**
     * Use the short class name as the default for the service name.
     */
    public String getName()
    {
        return SERVICE_NAME;
    }

    /**
     * Sub-classes should override this method to provide
     * custum 'start' logic.
     *
     * <p>This method is empty, and is provided for convenience
     *    when concrete service classes do not need to perform
     *    anything specific for this state change.
     */
    protected void startService() throws Exception
    {
	synchronized(startedLock)
	{
	    started = true ;
	}

        this.getLog().info("JBossTS Transaction Service (JTA version) - JBoss Inc.");

        this.getLog().info("Setting up property manager MBean and JMX layer");

        /** Set the tsmx agent implementation to the local JBOSS agent impl **/
        LocalJBossAgentImpl.setLocalAgent(this.getServer());
        System.setProperty(com.arjuna.ats.tsmx.TransactionServiceMX.AGENT_IMPLEMENTATION_PROPERTY,
                com.arjuna.ats.internal.jbossatx.agent.LocalJBossAgentImpl.class.getName());
        System.setProperty(Environment.LAST_RESOURCE_OPTIMISATION_INTERFACE, LastResource.class.getName()) ;
        System.setProperty(com.arjuna.ats.arjuna.common.Environment.SERVER_BIND_ADDRESS, System.getProperty(ServerConfig.SERVER_BIND_ADDRESS));
        
        if (timeout != 0)
        {
            TxControl.setDefaultTimeout(timeout);
        }

        /** Register management plugin **/
        com.arjuna.ats.arjuna.common.arjPropertyManager.propertyManager.addManagementPlugin(new PropertyServiceJMXPlugin());

        // Associate transaction reaper with our context classloader.
        TransactionReaper.create() ;

        /** Register propagation context manager **/
        try
        {
            /** Bind the propagation context manager **/
            bindRef(PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE, com.arjuna.ats.internal.jbossatx.jta.PropagationContextManager.class.getName());
            bindRef(PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE, com.arjuna.ats.internal.jbossatx.jta.PropagationContextManager.class.getName());
        }
        catch (Exception e)
        {
            this.getLog().fatal("Failed to create and register Propagation Context Manager", e);
        }

        /** Start the recovery manager **/
        try
        {
            if (_runRM)
            {
                registerNotification() ;

                this.getLog().info("Starting recovery manager");

                RecoveryManager.delayRecoveryManagerThread() ;
                _recoveryManager = RecoveryManager.manager() ;

                this.getLog().info("Recovery manager started");
            }
            else
            {
                if (isRecoveryManagerRunning())
                {
                    this.getLog().info("Using external recovery manager");
                }
                else
                {
                    this.getLog().fatal("Recovery manager not found - please refer to the JBossTS documentation for details");

                    throw new Exception("Recovery manager not found - please refer to the JBossTS documentation for details");
                }
            }
        }
        catch (Exception e)
        {
            this.getLog().fatal("Failed to start recovery manager", e);
            throw e;
        }

        /** Bind the transaction manager and tsr JNDI references **/
        this.getLog().info("Binding TransactionManager JNDI Reference");

        jtaPropertyManager.propertyManager.setProperty(Environment.JTA_TM_IMPLEMENTATION, TransactionManagerDelegate.class.getName());
        jtaPropertyManager.propertyManager.setProperty(Environment.JTA_UT_IMPLEMENTATION, UserTransactionImple.class.getName());

		jtaPropertyManager.propertyManager.setProperty(Environment.JTA_TSR_IMPLEMENTATION, TransactionSynchronizationRegistryImple.class.getName());
		// When running inside the app server, we bind TSR in the JNDI java:/ space, not its required location.
		// It's the job of individual components (EJB3, web, etc) to copy the ref to the java:/comp space)
        jtaPropertyManager.propertyManager.setProperty(Environment.TSR_JNDI_CONTEXT, "java:/TransactionSynchronizationRegistry");

		JNDIManager.bindJTATransactionManagerImplementation();
		JNDIManager.bindJTATransactionSynchronizationRegistryImplementation();
	}

    /**
     * Handle JMX notification.
     * @param notification The JMX notification event.
     * @param param The notification parameter.
     */
    public void handleNotification(final Notification notification, final Object param)
    {
        _recoveryManager.startRecoveryManagerThread() ;
    }

    private boolean isRecoveryManagerRunning() throws Exception
    {
        boolean active = false;
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");

        if ( pm != null )
        {
            BufferedReader in = null;
            PrintStream out = null;

            try
            {
                Socket sckt = RecoveryManager.getClientSocket(getRunInVMRecoveryManager());

                in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
                out = new PrintStream(sckt.getOutputStream());

                /** Output ping message **/
                out.println("PING");

                /** Receive pong message **/
                String inMessage = in.readLine();

                active = inMessage != null ? (inMessage.equals("PONG")) : false;
            }
            catch (Exception ex)
            {
                getLog().error("Failed to connect to recovery manager", ex);
                active = false;
            }
            finally
            {
                if ( in != null )
                {
                    in.close();
                }

                if ( out != null )
                {
                    out.close();
                }
            }
        }

        return active;
    }

    /**
     * Sub-classes should override this method to provide
     * custum 'destroy' logic.
     *
     * <p>This method is empty, and is provided for convenience
     *    when concrete service classes do not need to perform
     *    anything specific for this state change.
     */
    protected void destroyService() throws Exception
    {
        if (_runRM)
        {
            this.getLog().info("Stopping recovery manager");

            _recoveryManager.stop();
        }
    }

    /**
     * Set the default transaction timeout used by this transaction manager.
     *
     * @param timeout The default timeout in seconds for all transactions created
     * using this transaction manager.
     *
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setTransactionTimeout(int timeout) throws IllegalStateException
    {
	synchronized(startedLock)
	{
            if (started)
            {
        	if (this.timeout != timeout)
        	{
        	    throw new IllegalStateException("Cannot set transaction timeout once MBean has started") ;
        	}
            }
            else
            {
        	this.timeout = timeout ;
            }
	}
    }

    /**
     * Get the default transaction timeout used by this transaction manager.
     *
     * @return The default timeout in seconds for all transactions created
     * using this transaction manager.
     */
    public int getTransactionTimeout()
    {
	synchronized(startedLock)
	{
	    return (started ? timeout : TxControl.getDefaultTimeout()) ;
	}
    }

    /**
     * Retrieve a reference to the JTA transaction manager.
     *
     * @return A reference to the JTA transaction manager.
     */
    public TransactionManager getTransactionManager()
    {
        return com.arjuna.ats.jta.TransactionManager.transactionManager();
    }

    /**
     * Get the XA Terminator
     *
     * @return the XA Terminator
     */
    public JBossXATerminator getXATerminator()
    {
       return TERMINATOR ;
    }

    /**
     * Retrieve a reference to the JTA user transaction manager.
     *
     * @return A reference to the JTA user transaction manager.
     */
    public UserTransaction getUserTransaction()
    {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    /**
     * Set whether the transaction propagation context manager should propagate a
     * full PropagationContext (JTS) or just a cut-down version (for JTA).
     *
     * @param propagateFullContext
     */
    public void setPropagateFullContext(boolean propagateFullContext)
    {
    }

    /**
     * Retrieve whether the transaction propagation context manager should propagate a
     * full PropagationContext (JTS) or just a cut-down version (for JTA).
     */
    public boolean getPropagateFullContext()
    {
    		return false ;
    }

    /**
     * Sets whether the transaction service should collate transaction service statistics.
     *
     * @param enabled
     */
    public void setStatisticsEnabled(boolean enabled)
    {
        System.setProperty(com.arjuna.ats.arjuna.common.Environment.ENABLE_STATISTICS, enabled ? "YES" : "NO");
    }

    /**
     * Retrieves whether the statistics are enabled.
     * @return true if enabled, false otherwise.
     */
    public boolean getStatisticsEnabled()
    {
        boolean enabled = System.getProperty(com.arjuna.ats.arjuna.common.Environment.ENABLE_STATISTICS, "NO").equals("YES");

        return enabled;
    }

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    public void registerXAExceptionFormatter(Class c, XAExceptionFormatter f)
    {
        this.getLog().warn("XAExceptionFormatters are not supported by the JBossTS Transaction Service - this warning can safely be ignored");
    }

    /**
     * This method has been put in here so that it is compatible with the JBoss standard Transaction Manager.
     * As we do not support exception formatters just display a warning for the moment.
     */
    public void unregisterXAExceptionFormatter(Class c)
    {
        // Ignore
    }

    /**
     * Returns the number of active transactions
     * @return The number of active transactions.
     */
    public long getTransactionCount()
    {
        return TxStats.numberOfTransactions();
    }

    /**
     * Returns the number of committed transactions
     * @return The number of committed transactions.
     */
    public long getCommitCount()
    {
        return TxStats.numberOfCommittedTransactions();
    }

    /**
     * Returns the number of rolledback transactions
     * @return The number of rolledback transactions.
     */
    public long getRollbackCount()
    {
        return TxStats.numberOfAbortedTransactions();
    }

    /**
     * Set whether the recovery manager should be ran in the same VM as
     * JBoss.  If this is false the Recovery Manager is already expected to
     * be running when JBoss starts.
     * @param runRM
     *
     * @throws IllegalStateException If the MBean has already started.
     */
    public void setRunInVMRecoveryManager(boolean runRM)
        throws IllegalStateException
    {
	synchronized(startedLock)
	{
            if (started)
            {
        	if (this._runRM != runRM)
        	{
        	    throw new IllegalStateException("Cannot set run in VM recovery manager once MBean has started") ;
        	}
            }
            else
            {
        	_runRM = runRM;
            }
	}
    }

    /**
     * Get whether the recovery manager should be ran in the same VM as
     * JBoss.  If this is false the Recovery Manager is already expected to
     * be running when JBoss starts.
     *
     * @return true if the recover manager is running in the same VM, false otherwise.
     */
    public boolean getRunInVMRecoveryManager()
    {
	synchronized(startedLock)
	{
	    return _runRM ;
	}
    }

    /**
     * Set the object store directory.
     * @param objectStoreDir The object store directory.
     *
     * @throws IllegalStateException if the MBean has already started.
     */
    public void setObjectStoreDir(final String objectStoreDir)
    	throws IllegalStateException
    {
        synchronized(startedLock)
        {
            if (started)
            {
        	final String currentDir = getObjectStoreDir() ;
        	final boolean equal = (currentDir == null ? objectStoreDir == null : currentDir.equals(objectStoreDir)) ;
        	if (!equal)
        	{
        	    throw new IllegalStateException("Cannot set object store dir once MBean has started") ;
        	}
            }
            else
            {
        	System.setProperty(com.arjuna.ats.arjuna.common.Environment.OBJECTSTORE_DIR, objectStoreDir) ;
            }
        }
    }

    /**
     * Get the object store directory.
     * @return objectStoreDir The object store directory.
     */
    public String getObjectStoreDir()
    {
	return System.getProperty(com.arjuna.ats.arjuna.common.Environment.OBJECTSTORE_DIR) ;
    }

    private void registerNotification()
        throws InstanceNotFoundException
    {
        final NotificationFilterSupport notificationFilter = new NotificationFilterSupport() ;
        notificationFilter.enableType(Server.START_NOTIFICATION_TYPE) ;

        final ObjectName serverName = ObjectNameFactory.create("jboss.system:type=Server") ;

        getServer().addNotificationListener(serverName, this, notificationFilter, null) ;
    }

    private void bindRef(String jndiName, String className)
            throws Exception
    {
        Reference ref = new Reference(className, className, null);
        new InitialContext().bind(jndiName, ref);
    }
}
