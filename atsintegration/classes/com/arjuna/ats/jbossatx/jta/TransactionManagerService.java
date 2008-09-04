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

import org.jboss.system.server.ServerConfig;
import org.jboss.tm.*;

import org.jboss.logging.Logger;

import com.arjuna.ats.internal.jbossatx.jta.jca.XATerminator;
import com.arjuna.ats.internal.jbossatx.jta.XAResourceRecoveryHelperWrapper;
import com.arjuna.ats.internal.jbossatx.agent.LocalJBossAgentImpl;
import com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.ats.jta.common.Environment;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.coordinator.TxStats;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import com.arjuna.ats.internal.tsmx.mbeans.PropertyServiceJMXPlugin;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.common.util.logging.LogFactory;

import javax.management.*;
import javax.naming.Reference;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;

/**
 * JBoss Transaction Manager Service.
 *
 * @author Richard A. Begg (richard.begg@arjuna.com)
 * @version $Id: TransactionManagerService.java,v 1.5 2005/06/24 15:24:15 kconner Exp $
 */
public class TransactionManagerService implements TransactionManagerServiceMBean, XAResourceRecoveryRegistry
{
    /*
    deploy/transaction-jboss-beans.xml:

    <?xml version="1.0" encoding="UTF-8"?>
    <deployment xmlns="urn:jboss:bean-deployer:2.0">

    <bean name="TransactionManager" class="com.arjuna.ats.jbossatx.jta.TransactionManagerService">
        <annotation>@org.jboss.aop.microcontainer.aspects.jmx.JMX(name="jboss:service=TransactionManager", exposedInterface=com.arjuna.ats.jbossatx.jta.TransactionManagerServiceMBean.class, registerDirectly=true)</annotation>

        <property name="transactionTimeout">300</property>
        <property name="objectStoreDir">${jboss.server.data.dir}/tx-object-store</property>
        <property name="mbeanServer"><inject bean="JMXKernel" property="mbeanServer"/></property>
    </bean>

    </deployment>
     */



    static {
		/*
		 * Override the default logging config, force use of the plugin that rewrites log levels to reflect app server level semantics.
		 * This must be done before the loading of anything that uses the logging, otherwise it's too late to take effect.
		 * Hence the static initializer block.
		 * see also http://jira.jboss.com/jira/browse/JBTM-20
		 */
		com.arjuna.ats.arjuna.common.arjPropertyManager.propertyManager.setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler");
		//System.setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler") ;
	}

    private final Logger log = org.jboss.logging.Logger.getLogger(TransactionManagerService.class);

    public final static String PROPAGATE_FULL_CONTEXT_PROPERTY = "com.arjuna.ats.jbossatx.jta.propagatefullcontext";

    private final static String SERVICE_NAME = "TransactionManagerService";
    private final static String PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextImporter";
    private final static String PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextExporter";
    private static final JBossXATerminator TERMINATOR = new XATerminator() ;

	private RecoveryManager _recoveryManager;
    private boolean _runRM = true;
    private int timeout ;

    private boolean configured;
    private byte[] configuredLock = new byte[0] ;

    private boolean isTransactionStatusManagerBindAddressSet;
    private boolean isRecoveryManagerBindAddressSet;

    private MBeanServer mbeanServer;

    /**
     * Use the short class name as the default for the service name.
     */
    public String getName()
    {
        return SERVICE_NAME;
    }

    public TransactionManagerService() {}


    public void create() throws Exception
    {
        synchronized(configuredLock)
        {
            // if the app server's ServerBindingManager has NOT been used to configure the listen addresses
            // (which it should be by default via the binding.xml and transaction-jboss-beans.xml) then we
            // want to fall back to using the SERVER_BIND_ADDRESS in preference to the value from the
            // jbossjta-properties.xml. Belt and Braces...

            InetAddress serverInetAddress = InetAddress.getByName(System.getProperty(ServerConfig.SERVER_BIND_ADDRESS));
            if(!isRecoveryManagerBindAddressSet) {
                setRecoveryInetAddress(serverInetAddress);
            }
            if(!isTransactionStatusManagerBindAddressSet) {
                setTransactionStatusManagerInetAddress(serverInetAddress);
            }

            // we are now done configuring things. The transaction system gets unhappy if we change config
            // once it's running, so prevent any further alterations before we start it up...

            configured = true ;
        }

        log.info("JBossTS Transaction Service (JTA version) - JBoss Inc.");

        log.info("Setting up property manager MBean and JMX layer");

        /** Set the tsmx agent implementation to the local JBOSS agent impl **/
        LocalJBossAgentImpl.setLocalAgent( getMbeanServer() );
        System.setProperty(com.arjuna.ats.tsmx.TransactionServiceMX.AGENT_IMPLEMENTATION_PROPERTY,
                com.arjuna.ats.internal.jbossatx.agent.LocalJBossAgentImpl.class.getName());
        System.setProperty(Environment.LAST_RESOURCE_OPTIMISATION_INTERFACE, LastResource.class.getName()) ;

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
            log.fatal("Failed to create and register Propagation Context Manager", e);
        }

        /** Start the recovery manager **/
        try
        {
            if (_runRM)
            {
                log.info("Initializing recovery manager");

                RecoveryManager.delayRecoveryManagerThread() ;
                _recoveryManager = RecoveryManager.manager() ;

                log.info("Recovery manager configured");
            }
            else
            {
                if (isRecoveryManagerRunning())
                {
                    log.info("Using external recovery manager");
                }
                else
                {
                    log.fatal("Recovery manager not found - please refer to the JBossTS documentation for details");

                    throw new Exception("Recovery manager not found - please refer to the JBossTS documentation for details");
                }
            }
        }
        catch (Exception e)
        {
            log.fatal("Failed to initialize recovery manager", e);
            throw e;
        }

        /** Bind the transaction manager and tsr JNDI references **/
        log.info("Binding TransactionManager JNDI Reference");

        jtaPropertyManager.propertyManager.setProperty(Environment.JTA_TM_IMPLEMENTATION, TransactionManagerDelegate.class.getName());
        jtaPropertyManager.propertyManager.setProperty(Environment.JTA_UT_IMPLEMENTATION, UserTransactionImple.class.getName());

		jtaPropertyManager.propertyManager.setProperty(Environment.JTA_TSR_IMPLEMENTATION, TransactionSynchronizationRegistryImple.class.getName());
		// When running inside the app server, we bind TSR in the JNDI java:/ space, not its required location.
		// It's the job of individual components (EJB3, web, etc) to copy the ref to the java:/comp space)
        jtaPropertyManager.propertyManager.setProperty(Environment.TSR_JNDI_CONTEXT, "java:/TransactionSynchronizationRegistry");

		JNDIManager.bindJTATransactionManagerImplementation();
		JNDIManager.bindJTATransactionSynchronizationRegistryImplementation();
	}

    public void start()
    {
        log.info("Starting transaction recovery manager");

        // TODO: any point in breaking out the recovery mgr into its own bean w/ own lifecycle?
        // may impact recovery config e.g. beans injection instead of XAResourceRecoveryRegistry lookup?
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
                Socket sckt = RecoveryManager.getClientSocket();

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
                try
                {
                    InetAddress host = RecoveryManager.getRecoveryManagerHost();
                    int port = RecoveryManager.getRecoveryManagerPort();

                    log.error("Failed to connect to recovery manager on " + host.getHostAddress() + ':' + port);
                }
                catch (UnknownHostException e)
                {
                    log.error("Failed to connect to recovery manager", ex);
                }

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


    public void stop() throws Exception
    {
        if (_runRM)
        {
            log.info("Stopping transaction recovery manager");

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
	synchronized(configuredLock)
	{
            if (configured)
            {
        	if (this.timeout != timeout)
        	{
        	    throw new IllegalStateException("Cannot set transaction timeout once MBean has configured") ;
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
	synchronized(configuredLock)
	{
	    return (configured ? timeout : TxControl.getDefaultTimeout()) ;
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
        log.warn("XAExceptionFormatters are not supported by the JBossTS Transaction Service - this warning can safely be ignored");
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
     * Returns the total number of transactions started over the lifetime of the server
     * @return Returns the total number of transactions started over the lifetime of the server
     */
    public long getTransactionCount()
    {
        return TxStats.numberOfTransactions();
    }

    /**
     * Returns the total number of nested transactions started over the lifetime of the server.
     * This will usually be 0 even when stats are enabled, since JTA disallows nested tx by default.
     */
    public long getNestedTransactonCount() {
        return TxStats.numberOfNestedTransactions();
    }

    /**
     * Returns the number of heuristics that have occurred.
     * @return the number of heuristics that have occurred.
     */
    public long getHeuristicCount() {
        return TxStats.numberOfHeuristics();
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
     * Get the number of transactions that have begun but not yet terminated.
     * This count is approximate, particularly in recovery situations.
     */
    public long getRunningTransactionCount() {
        return TxStats.numberOfInflightTransactions();
    }

    /**
     * Returns the number of transactions that have been timed out by the transaction reaper.
     * @return the number of transactions that have been timed out by the transaction reaper.
     */
    public long getTimedoutCount() {
        return TxStats.numberOfTimedOutTransactions();
    }

    /**
     * Returns the number of transactions that have been rolled back by application request.
     * This includes those that timeout, since the timeout behaviour is considered an
     * attribute of the application configuration.
     * @return the number of transactions that have been rolled back by application request.
     */
    public long getApplicationRollbackCount() {
        return TxStats.numberOfApplicationRollbacks();
    }

    /**
     * Returns the number of transactions that rolled back due to resource failure.
     * @return the number of transactions that rolled back due to resource failure.
     */
    public long getResourceRollbackCount() {
        return TxStats.numberOfResourceRollbacks();
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
	synchronized(configuredLock)
	{
            if (configured)
            {
        	if (this._runRM != runRM)
        	{
        	    throw new IllegalStateException("Cannot set run in VM recovery manager once MBean has configured") ;
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
	synchronized(configuredLock)
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
        synchronized(configuredLock)
        {
            if (configured)
            {
        	final String currentDir = getObjectStoreDir() ;
        	final boolean equal = (currentDir == null ? objectStoreDir == null : currentDir.equals(objectStoreDir)) ;
        	if (!equal)
        	{
        	    throw new IllegalStateException("Cannot set object store dir once MBean has configured") ;
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



    public MBeanServer getMbeanServer()
    {
        return mbeanServer;
    }

    public void setMbeanServer(MBeanServer mbeanServer)
    {
        synchronized(configuredLock)
        {
            if (configured)
            {
                if (this.mbeanServer != mbeanServer)
                {
                    throw new IllegalStateException("Cannot set MBeanServer once MBean has configured") ;
                }
            }
            else
            {
                this.mbeanServer = mbeanServer;
            }
        }
    }

    public void addXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        if(_recoveryManager == null) {
            log.error("No recovery system in which to register XAResourceRecovery instance");
            throw new IllegalStateException("No recovery system present in this server");
        }

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector<RecoveryModule>)_recoveryManager.getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            log.error("No suitable recovery module in which to register XAResourceRecovery instance");
            throw new IllegalStateException("No xa recovery module present in this server");
        }

        xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelperWrapper(xaResourceRecovery));
    }

    public void removeXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        if(_recoveryManager == null) {
            log.warn("No recovery system from which to remove XAResourceRecovery instance");
            return;
        }

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector <RecoveryModule>)_recoveryManager.getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            log.warn("No suitable recovery module in which to register XAResourceRecovery instance");
            return;
        }

        xaRecoveryModule.removeXAResourceRecoveryHelper(new XAResourceRecoveryHelperWrapper(xaResourceRecovery));
    }


    private void bindRef(String jndiName, String className)
            throws Exception
    {
        Reference ref = new Reference(className, className, null);
        new InitialContext().bind(jndiName, ref);
    }

    ///////////////////

    // note that the address/port setters rely on having the property manager initialized first, or their settings
    // would be overridden by those in the config files when the properties are loaded from file. Fortunatly the static
    // block for the logging has the nice side effect of loading the config too, so it works out ok.

    public InetAddress getTransactionStatusManagerInetAddress()
    {
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
        try {
            return Utility.hostNameToInetAddress(pm.getProperty(com.arjuna.ats.arjuna.common.Environment.TRANSACTION_STATUS_MANAGER_ADDRESS), "");
        } catch(UnknownHostException e) {
            log.warn("UnknownHostException from getTransactionStatusManagerInetAddress, input was: "+pm.getProperty(com.arjuna.ats.arjuna.common.Environment.TRANSACTION_STATUS_MANAGER_ADDRESS));
            return null;
        }
    }

    public void setTransactionStatusManagerInetAddress(InetAddress tsmInetAddress) throws IllegalStateException
    {
        synchronized(configuredLock)
        {
            if (configured)
            {
                if(!tsmInetAddress.getAddress().equals(getTransactionStatusManagerInetAddress().getAddress())) {
                    throw new IllegalStateException("Cannot set transactionStatusManagerInetAddress once the MBean has configured");
                }
            }
            else
            {
                PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
                pm.setProperty(com.arjuna.ats.arjuna.common.Environment.TRANSACTION_STATUS_MANAGER_ADDRESS, tsmInetAddress.getHostAddress());
                isTransactionStatusManagerBindAddressSet = true;
            }
        }
    }

    public int getTransactionStatusManagerPort()
    {
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
        return Integer.parseInt(pm.getProperty(com.arjuna.ats.arjuna.common.Environment.TRANSACTION_STATUS_MANAGER_PORT));
    }

    public void setTransactionStatusManagerPort(int port) throws IllegalStateException
    {
        if (configured)
        {
            if(port != getTransactionStatusManagerPort()) {
                throw new IllegalStateException("Cannot set transactionStatusManagerPort once the MBean has configured");
            }
        }
        else
        {
            PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
            pm.setProperty(com.arjuna.ats.arjuna.common.Environment.TRANSACTION_STATUS_MANAGER_PORT, ""+port);
        }
    }



    public InetAddress getRecoveryInetAddress()
    {
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
        try {
            return Utility.hostNameToInetAddress(pm.getProperty(com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_ADDRESS), "");
        } catch(UnknownHostException e) {
            log.warn("UnknownHostException from getRecoveryInetAddress, input was: "+pm.getProperty(com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_ADDRESS));
            return null;
        }
    }

    public void setRecoveryInetAddress(InetAddress recoveryInetAddress) throws IllegalStateException
    {
        if (configured)
        {
            if(!recoveryInetAddress.getAddress().equals(getRecoveryInetAddress().getAddress())) {
                throw new IllegalStateException("Cannot set recoveryInetAddress once the MBean has configured");
            }
        }
        else
        {
            PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
            pm.setProperty(com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_ADDRESS, recoveryInetAddress.getHostAddress());
            isRecoveryManagerBindAddressSet = true;
        }
    }

    public int getRecoveryPort()
    {
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
        return Integer.parseInt(pm.getProperty(com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_PORT));
    }

    public void setRecoveryPort(int port) throws IllegalStateException
    {
        if (configured)
        {
            if(port != getRecoveryPort()) {
                throw new IllegalStateException("Cannot set recoveryPort once the MBean has configured");
            }
        }
        else
        {
            PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");
            pm.setProperty(com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_PORT, ""+port);
        }
    }


    public int getSocketProcessIdPort()
    {
        PropertyManager pm = arjPropertyManager.getPropertyManager();
        return Integer.parseInt(pm.getProperty(com.arjuna.ats.arjuna.common.Environment.SOCKET_PROCESS_ID_PORT));
    }

    public void setSocketProcessIdPort(int port) throws IllegalStateException
    {
        if (configured)
        {
            if(port != getSocketProcessIdPort()) {
                throw new IllegalStateException("Cannot set socketProcessIdPort once the MBean has configured");
            }
        }
        else
        {
            PropertyManager pm = arjPropertyManager.getPropertyManager();
            pm.setProperty(com.arjuna.ats.arjuna.common.Environment.SOCKET_PROCESS_ID_PORT, ""+port);
        }
    }
}
