/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author JBoss, a division of Red Hat.
 */
package com.arjuna.ats.jta.common;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;

import java.util.List;
import java.util.ArrayList;

/**
 * A JavaBean containing configuration properties for the JTA subsystem.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.jta.")
public class JTAEnvironmentBean implements JTAEnvironmentBeanMBean
{
    @FullPropertyName(name = "com.arjuna.ats.jta.common.propertiesFile")
    private volatile String propertiesFile = "";

    private volatile boolean supportSubtransactions = false;

    private volatile String jtaTMImplementation = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple";
    private volatile String jtaUTImplementation = "com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple";
    private volatile String jtaTSRImplementation = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple";

    private volatile int xaBackoffPeriod = 20000;

    @ConcatenationPrefix(prefix = "com.arjuna.ats.jta.xaRecoveryNode")
    private volatile List<String> xaRecoveryNodes = new ArrayList<String>();

    @ConcatenationPrefix(prefix = "com.arjuna.ats.jta.recovery.XAResourceRecovery")
    private volatile List<String> xaResourceRecoveryInstances = new ArrayList<String>();

    private volatile boolean xaRollbackOptimization = false;
    private volatile boolean xaAssumeRecoveryComplete = false;

    // com.arjuna.ats.jta.utils.
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.UTJNDIContext")
    private volatile String jtaUTJNDIContext = "java:/UserTransaction";
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.TMJNDIContext")
    private volatile String jtaTMJNDIContext =  "java:/TransactionManager";
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.TSRJNDIContext")
    private volatile String jtaTSRJNDIContext = "java:/TransactionSynchronizationRegistry";

    @ConcatenationPrefix(prefix = "com.arjuna.ats.jta.xaErrorHandler")
    private volatile List<String> xaErrorHandlers = new ArrayList<String>();

    private volatile boolean xaTransactionTimeoutEnabled = true;
    private volatile String lastResourceOptimisationInterface = null;

    /**
     * Returns the name of the properties file.
     *
     * Default: ""
     * Equivalent deprecated property: com.arjuna.ats.jta.common.propertiesFile
     *
     * @return the name of the properties file
     */
    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    /**
     * Sets the name of the properties file.
     *
     * @param propertiesFile the name of the properties file.
     */
    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

    /**
     * Returns true if subtransactions are allowed.
     * Warning: subtransactions are not JTA spec compliant and most XA resource managers don't understand them.
     *
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.jta.supportSubtransactions
     *
     * @return true if subtransactions are enabled, false otherwise.
     */
    public boolean isSupportSubtransactions()
    {
        return supportSubtransactions;
    }

    /**
     * Sets if subtransactions should be allowed.
     *
     * @param supportSubtransactions true to enable subtransactions, false to disable.
     */
    public void setSupportSubtransactions(boolean supportSubtransactions)
    {
        this.supportSubtransactions = supportSubtransactions;
    }

    /**
     * Returns the classname of the javax.transaction.TransactionManager implementation.
     *
     * Default: "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple"
     * Equivalent deprecated property: com.arjuna.ats.jta.jtaTMImplementation
     *
     * @return the name of the class implementing TransactionManager.
     */
    public String getJtaTMImplementation()
    {
        return jtaTMImplementation;
    }

    /**
     * Sets the classname of the javax.transaction.TransactionManager implementation.
     *
     * @param jtaTMImplementation the name of a class which implements TransactionManager.
     */
    public void setJtaTMImplementation(String jtaTMImplementation)
    {
        this.jtaTMImplementation = jtaTMImplementation;
    }

    /**
     * Returns the classname of the javax.transaction.UserTransaction implementation.
     *
     * Default: "com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple"
     * Equivalent deprecated property: com.arjuna.ats.jta.jtaUTImplementation
     *
     * @return the name of the class implementing javax.transaction.UserTransaction.
     */
    public String getJtaUTImplementation()
    {
        return jtaUTImplementation;
    }

    /**
     * Sets the classname of the javax.transaction.UserTransaction implementation.
     *
     * @param jtaUTImplementation the name of a class which implements UserTransaction.
     */
    public void setJtaUTImplementation(String jtaUTImplementation)
    {
        this.jtaUTImplementation = jtaUTImplementation;
    }

    /**
     * Returns the classname of the javax.transaction.TransactionSynchronizationRegistry implementation.
     *
     * Default: "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple"
     * Equivalent deprecated property: com.arjuna.ats.jta.jtaTSRImplementation
     *
     * @return the name of the class implementing javax.transaction.TransactionSynchronizationRegistry.
     */
    public String getJtaTSRImplementation()
    {
        return jtaTSRImplementation;
    }

    /**
     * Sets the classname of the javax.transaction.TransactionSynchronizationRegistry implementation.
     *
     * @param jtaTSRImplementation the name of a class which implements TransactionSynchronizationRegistry.
     */
    public void setJtaTSRImplementation(String jtaTSRImplementation)
    {
        this.jtaTSRImplementation = jtaTSRImplementation;
    }

    /**
     * Returns the XA backoff period, in milliseconds.
     *
     * Default: 20000 milliseconds
     * Equivalent deprecated property: com.arjuna.ats.jta.xaBackoffPeriod
     *
     * @deprecated I'm not unsed, remove me.
     * @return the XA backoff period, in milliseconds.
     */
    public int getXaBackoffPeriod()
    {
        return xaBackoffPeriod;
    }

    /**
     * Sets the XA backoff period, in milliseconds.
     *
     * @param xaBackoffPeriod the XA backoff period, in milliseconds.
     */
    public void setXaBackoffPeriod(int xaBackoffPeriod)
    {
        this.xaBackoffPeriod = xaBackoffPeriod;
    }

    /**
     * Returns the set of node identifiers for which recovery will be performed.
     * The returned list is a copy. May return an empty list, will not return null.
     *
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.jta.xaRecoveryNode
     *
     * @return the set of node identifiers for which to perform recovery.
     */
    public List<String> getXaRecoveryNodes()
    {
       return new ArrayList<String>(xaRecoveryNodes);
    }

    /**
     * Sets the node identifiers for which recovery will be performed.
     * The provided list will be copied, not retained.
     *
     * @param xaRecoveryNodes the set of node identifiers for which to perform recovery.
     */
    public void setXaRecoveryNodes(List<String> xaRecoveryNodes)
    {
        if(xaRecoveryNodes == null) {
            this.xaRecoveryNodes = new ArrayList<String>(); 
        } else {
            this.xaRecoveryNodes = new ArrayList<String>(xaRecoveryNodes);
        }
    }

    /**
     * Returns the set of XAResourceRecovery implementation classnames,
     * each of which may have configuration data appended to it.
     * The returned list is a copy. May return an empty list, will not return null.
     *
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.jta.recovery.XAResourceRecovery
     *
     * @return the set of XAResourceRecovery implementations with their configuration data. 
     */
    public List<String> getXaResourceRecoveryInstances()
    {
        return new ArrayList<String>(xaResourceRecoveryInstances);
    }

    /**
     * Sets the XAResourceRecovery implementations that will be used,
     * each optionally including trailing configuration data.
     * The provided list will be copied, not retained.
     *
     * @param xaResourceRecoveryInstances the XAResourceRecovery implementaion classnames and configuration.
     */
    public void setXaResourceRecoveryInstances(List<String> xaResourceRecoveryInstances)
    {
        if(xaResourceRecoveryInstances == null) {
            this.xaResourceRecoveryInstances = new ArrayList<String>();
        } else {
            this.xaResourceRecoveryInstances = new ArrayList<String>(xaResourceRecoveryInstances);
        }
    }

    /**
     * Returns if connections associated to XAResources that fail during prepare should be cleaned up immediately.
     * TODO move to JDBC module as it's only for our own connection manager?
     *
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.jta.xaRollbackOptimization
     *
     * @return true for cleanup during prepare, false for cleanup during phase two rollback.
     */
    public boolean isXaRollbackOptimization()
    {
        return xaRollbackOptimization;
    }

    /**
     * Sets if failed resources should be cleaned up during prepare or during phase two.
     *
     * @param xaRollbackOptimization true for immediate cleanup, false for phase two cleanup.
     */
    public void setXaRollbackOptimization(boolean xaRollbackOptimization)
    {
        this.xaRollbackOptimization = xaRollbackOptimization;
    }

    /**
     * Returns if XAResources that can't be recovered should be assumed to have completed.
     * WARNING: enabling this property is not recommended and may cause inconsistency if
     * your recovery configuration is incorrect or resource managers are not available.
     *
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.jta.xaAssumeRecoveryComplete
     *
     * @return true for assumed completion, false for no such assumption.
     */
    public boolean isXaAssumeRecoveryComplete()
    {
        return xaAssumeRecoveryComplete;
    }

    /**
     * Sets if XAResources that can't be recovered should be assumed to have completed.
     *
     * @param xaAssumeRecoveryComplete true to enable completion assumption, false to disable.
     */
    public void setXaAssumeRecoveryComplete(boolean xaAssumeRecoveryComplete)
    {
        this.xaAssumeRecoveryComplete = xaAssumeRecoveryComplete;
    }

    /**
     * Returns the JNDI bind name for the implementation of UserTransaction.
     *
     * Default: "java:/UserTransaction"
     * Equivalent deprecated property: com.arjuna.ats.jta.utils.UTJNDIContext
     *
     * @return the JNDI bind location for the UserTransaction interface.
     */
    public String getJtaUTJNDIContext()
    {
        return jtaUTJNDIContext;
    }

    /**
     * Sets the JNDI bind name for the implementation of UserTransaction.
     *
     * @param jtaUTJNDIContext the JNDI bind location for the UserTransaction interface.
     */
    public void setJtaUTJNDIContext(String jtaUTJNDIContext)
    {
        this.jtaUTJNDIContext = jtaUTJNDIContext;
    }

    /**
     * Returns the JNDI bind name for the implementation of TransactionManager.
     *
     * Default: "java:/TransactionManager"
     * Equivalent deprecated property: com.arjuna.ats.jta.utils.TMJNDIContext
     *
     * @return the JNDI bind location for the TransactionManager interface.
     */
    public String getJtaTMJNDIContext()
    {
        return jtaTMJNDIContext;
    }

    /**
     * Sets the JNDI bind name for the implementation of TransactionManager.
     *
     * @param jtaTMJNDIContext the JNDI bind location for the TransactionManager interface.
     */
    public void setJtaTMJNDIContext(String jtaTMJNDIContext)
    {
        this.jtaTMJNDIContext = jtaTMJNDIContext;
    }

    /**
     * Returns the JNDI bind name for the implementation of TransactionSynchronizationRegistry.
     *
     * Default: "java:/TransactionSynchronizationRegistry"
     * Equivalent deprecated property: com.arjuna.ats.jta.utils.TSRJNDIContext
     *
     * @return the JNDI bind location for the TransactionSynchronizationRegistry interface.
     */
    public String getJtaTSRJNDIContext()
    {
        return jtaTSRJNDIContext;
    }

    /**
     * Sets tje JNDI bind name for the implementation of TransactionSynchronizationRegistry.
     *
     * @param jtaTSRJNDIContext the JNDI bind location for the TransactionSynchronizationRegistry implementation.
     */
    public void setJtaTSRJNDIContext(String jtaTSRJNDIContext)
    {
        this.jtaTSRJNDIContext = jtaTSRJNDIContext;
    }

    /**
     * Returns the set of XAResourceMap implementation classnames used to configure XAException handling.
     * The returned list is a copy. May return an empty list, will not return null.
     *
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.jta.xaErrorHandler
     *
     * @return a set of classnames, each an implementation of XAResourceMap.
     */
    public List<String> getXaErrorHandlers()
    {
        return new ArrayList<String>(xaErrorHandlers);
    }

    /**
     * Sets the names of the XAResourceMap classes used for XAException handling.
     * The provided list will be copied, not retained.
     *
     * @param xaErrorHandlers a set of names of classes, each implementing XAResourceMap.
     */
    public void setXaErrorHandlers(List<String> xaErrorHandlers)
    {
        if(xaErrorHandlers == null) {
            this.xaErrorHandlers = new ArrayList<String>();
        } else {
            this.xaErrorHandlers = new ArrayList<String>(xaErrorHandlers);
        }
    }

    /**
     * Returns if the transaction timeout is passed on to the enlisted XAResources.
     *
     * Default: true.
     * Equivalent deprecated property: com.arjuna.ats.jta.xaTransactionTimeoutEnabled
     *
     * @return true to pass transaction timeout configuration on to the XAResources, false to skip setting resource timeout.
     */
    public boolean isXaTransactionTimeoutEnabled()
    {
        return xaTransactionTimeoutEnabled;
    }

    /**
     * Sets if the transaction timeout should be passed to the enlisted XAResource or not.
     *
     * @param xaTransactionTimeoutEnabled true to enable setting XAResource timeouts, false to disable.
     */
    public void setXaTransactionTimeoutEnabled(boolean xaTransactionTimeoutEnabled)
    {
        this.xaTransactionTimeoutEnabled = xaTransactionTimeoutEnabled;
    }

    /**
     * Returns the classname of the marker interface used to indicate a LastResource.
     *
     * Default: null.
     * Equivalent deprecated property: com.arjuna.ats.jta.lastResourceOptimisationInterface
     *
     * @return the classname of the market interface for LastResource handling.
     */
    public String getLastResourceOptimisationInterface()
    {
        return lastResourceOptimisationInterface;
    }

    /**
     * Sets the classname of the marker interface used to indicate a LastResource.
     *
     * @param lastResourceOptimisationInterface the classname of the marker interface.
     */
    public void setLastResourceOptimisationInterface(String lastResourceOptimisationInterface)
    {
        this.lastResourceOptimisationInterface = lastResourceOptimisationInterface;
    }
}
