/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
 *
 * Arjuna Technologies Ltd.
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionManagerService.java,v 1.17 2005/06/24 15:24:14 kconner Exp $
 */
package com.arjuna.ats.jbossatx.jts;

import org.jboss.system.ServiceMBeanSupport;
import org.jboss.iiop.CorbaORBService;
import org.jboss.tm.JBossXATerminator;
import org.jboss.tm.XAExceptionFormatter;
import com.arjuna.ats.internal.jbossatx.jts.PropagationContextWrapper;
import com.arjuna.ats.internal.jbossatx.jts.jca.XATerminator;
import com.arjuna.ats.internal.jbossatx.agent.LocalJBossAgentImpl;
import com.arjuna.ats.internal.arjuna.recovery.RecoveryManagerImple;
import com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple;
import com.arjuna.ats.jta.utils.JNDIManager;
import com.arjuna.ats.jta.common.Environment;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxStats;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.OA;

import com.arjuna.ats.internal.tsmx.mbeans.PropertyServiceJMXPlugin;
import com.arjuna.ats.internal.jts.recovery.RecoveryORBManager;
import com.arjuna.common.util.propertyservice.PropertyManagerFactory;
import com.arjuna.common.util.propertyservice.PropertyManager;

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
 * @version $Id: TransactionManagerService.java,v 1.17 2005/06/24 15:24:14 kconner Exp $
 */
public class TransactionManagerService extends ServiceMBeanSupport implements TransactionManagerServiceMBean
{
    public final static String PROPAGATE_FULL_CONTEXT_PROPERTY = "com.arjuna.ats.jbossatx.jts.propagatefullcontext";

    private final static String SERVICE_NAME = "TransactionManagerService";
    private final static String PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextImporter";
    private final static String PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE = "java:/TransactionPropagationContextExporter";
    private static final JBossXATerminator TERMINATOR = new XATerminator() ;

    private RecoveryManagerImple _recoveryManager;
    private boolean _initialised = false;
    private boolean _runRM = true;

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
        ORB orb = null;

        this.getLog().info("JBossTS Transaction Service - JBoss Inc.");

        this.getLog().info("Setting up property manager MBean and JMX layer");

        /** Set the tsmx agent implementation to the local JBOSS agent impl **/
        LocalJBossAgentImpl.setLocalAgent(this.getServer());
        System.setProperty(com.arjuna.ats.tsmx.TransactionServiceMX.AGENT_IMPLEMENTATION_PROPERTY,
                com.arjuna.ats.internal.jbossatx.agent.LocalJBossAgentImpl.class.getName());

        /** Register management plugin **/
        com.arjuna.ats.arjuna.common.arjPropertyManager.propertyManager.addManagementPlugin(new PropertyServiceJMXPlugin());

        /** Register propagation context manager **/
        try
        {
            /** Bind the propagation context manager **/
            bindRef(PROPAGATION_CONTEXT_IMPORTER_JNDI_REFERENCE, com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager.class.getName());
            bindRef(PROPAGATION_CONTEXT_EXPORTER_JNDI_REFERENCE, com.arjuna.ats.internal.jbossatx.jts.PropagationContextManager.class.getName());

            /** Create an ORB portability wrapper around the CORBA ORB services orb **/
            orb = ORB.getInstance("jboss-atx");

            /** Retrieve ORB service's ORB and root POA **/
            InitialContext ctx = new InitialContext();
            org.omg.CORBA.ORB orbImpl = (org.omg.CORBA.ORB) ctx.lookup("java:/" + CorbaORBService.ORB_NAME);
            org.omg.PortableServer.POA rootPOA = (org.omg.PortableServer.POA) ctx.lookup("java:/" + CorbaORBService.POA_NAME);

            orb.setOrb(orbImpl);
            OA oa = OA.getRootOA(orb);
            oa.setPOA(rootPOA);

            RecoveryORBManager.setORB(orb);
            RecoveryORBManager.setPOA(oa);
        }
        catch (Exception e)
        {
            this.getLog().fatal("Failed to create and register ORB/OA", e);
        }

        /** Start the recovery manager **/
        try
        {
            if (_runRM)
            {
                this.getLog().info("Starting recovery manager");

                _recoveryManager = new RecoveryManagerImple(true);

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

        /** Bind the transaction manager JNDI reference **/
        this.getLog().info("Binding TransactionManager JNDI Reference");

        jtaPropertyManager.propertyManager.setProperty(Environment.JTA_TM_IMPLEMENTATION, TransactionManagerDelegate.class.getName());
        jtaPropertyManager.propertyManager.setProperty(Environment.JTA_UT_IMPLEMENTATION, UserTransactionImple.class.getName());

        JNDIManager.bindJTATransactionManagerImplementation();

        /** Signal that the transaction manager has been bound **/
        _initialised = true;
    }

    private boolean isRecoveryManagerRunning() throws Exception
    {
        boolean active = false;
        int port = 0;
        PropertyManager pm = PropertyManagerFactory.getPropertyManager("com.arjuna.ats.propertymanager", "recoverymanager");

        if ( pm != null )
        {
            String portStr = pm.getProperty(com.arjuna.ats.arjuna.common.Environment.RECOVERY_MANAGER_PORT);

            if (portStr != null)
            {
                try
                {
                    port = Integer.parseInt(portStr);
                }
                catch (Exception ex)
                {
                    port = -1;
                }
            }
            else
            {
                throw new Exception("The transaction status manager port is not set - please refer to the JBossTS documentation");
            }

            BufferedReader in = null;
            PrintStream out = null;

            try
            {
                getLog().info("Connecting to recovery manager on port "+port);

                Socket sckt = new Socket(InetAddress.getLocalHost(),port);

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
     */
    public void setTransactionTimeout(int timeout) throws javax.transaction.SystemException
    {
        if (timeout >= 0)
        {
            jtsPropertyManager.propertyManager.setProperty(com.arjuna.ats.jts.common.Environment.DEFAULT_TIMEOUT, Integer.toString(timeout));
        }
        else
        {
            throw new javax.transaction.SystemException("Transaction Timeout < 0");
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
        PropagationContextWrapper.setPropagateFullContext(propagateFullContext);
    }

    /**
     * Retrieve whether the transaction propagation context manager should propagate a
     * full PropagationContext (JTS) or just a cut-down version (for JTA).
     */
    public boolean getPropagateFullContext()
    {
        return PropagationContextWrapper.getPropagateFullContext();
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
     * @return
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
     * @return
     */
    public long getTransactionCount()
    {
        return TxStats.numberOfTransactions();
    }

    /**
     * Returns the number of committed transactions
     * @return
     */
    public long getCommitCount()
    {
        return TxStats.numberOfCommittedTransactions();
    }

    /**
     * Returns the number of rolledback transactions
     * @return
     */
    public long getRollbackCount()
    {
        return TxStats.numberOfAbortedTransactions();
    }

    /**
     * Returns whether the recovery manager should be ran in the same VM as
     * JBoss.  If this is false the Recovery Manager is already expected to
     * be running when JBoss starts.
     * @param runRM
     */
    public void setRunInVMRecoveryManager(boolean runRM)
    {
        _runRM = runRM;
    }

    private void bindRef(String jndiName, String className)
            throws Exception
    {
        Reference ref = new Reference(className, className, null);
        new InitialContext().bind(jndiName, ref);
    }
}
