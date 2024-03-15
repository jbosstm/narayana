/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.common;

import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecordWrappingPlugin;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;
import com.arjuna.ats.jta.resources.XAResourceMap;
import com.arjuna.common.internal.util.ClassloadingUtility;
import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;
import org.jboss.tm.usertx.UserTransactionOperationsProvider;
import org.jboss.tm.usertx.client.ServerVMClientUserTransactionOperationsProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A JavaBean containing configuration properties for the JTA subsystem.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.jta.")
public class JTAEnvironmentBean implements JTAEnvironmentBeanMBean {
    private volatile boolean supportSubtransactions = false;

    private volatile String transactionManagerClassName = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple";
    private volatile TransactionManager transactionManager = null;

    private volatile String userTransactionClassName = "com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple";
    private volatile UserTransaction userTransaction = null;

    private volatile String transactionSynchronizationRegistryClassName = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple";
    private volatile TransactionSynchronizationRegistry transactionSynchronizationRegistry = null;

    @ConcatenationPrefix(prefix = "com.arjuna.ats.jta.xaRecoveryNode")
    private volatile List<String> xaRecoveryNodes = new ArrayList<String>();

    @ConcatenationPrefix(prefix = "com.arjuna.ats.jta.recovery.XAResourceRecovery")
    private volatile List<String> xaResourceRecoveryClassNames = new ArrayList<String>();
    private volatile List<XAResourceRecovery> xaResourceRecoveries = null;

    private volatile List<String> xaResourceOrphanFilterClassNames = new ArrayList<String>();
    private volatile List<XAResourceOrphanFilter> xaResourceOrphanFilters = null;

    private volatile boolean xaAssumeRecoveryComplete = false;

    // com.arjuna.ats.jta.utils.
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.UTJNDIContext")
    private volatile String userTransactionJNDIContext = "java:/UserTransaction";
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.TMJNDIContext")
    private volatile String transactionManagerJNDIContext = "java:/TransactionManager";
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.TSRJNDIContext")
    private volatile String transactionSynchronizationRegistryJNDIContext = "java:/TransactionSynchronizationRegistry";

    @ConcatenationPrefix(prefix = "com.arjuna.ats.jta.xaErrorHandler")
    private volatile List<String> xaResourceMapClassNames = new ArrayList<String>();
    private volatile List<XAResourceMap> xaResourceMaps = null;

    private volatile boolean xaTransactionTimeoutEnabled = true;

    private volatile String lastResourceOptimisationInterfaceClassName = "com.arjuna.ats.jta.resources.LastResourceCommitOptimisation";
    private volatile Class lastResourceOptimisationInterface = null;

    private volatile String xaResourceRecordWrappingPluginClassName;
    private volatile XAResourceRecordWrappingPlugin xaResourceRecordWrappingPlugin;

    private int asyncCommitPoolSize = 10;

    private int orphanSafetyInterval = 20000;

    private String commitMarkableResourceTableName = "xids";

    private Map<String, String> commitMarkableResourceTableNameMap = new HashMap<String, String>();

    private List<String> commitMarkableResourceJNDINames = new ArrayList<String>();

    private boolean performImmediateCleanupOfCommitMarkableResourceBranches = false;

    private boolean notifyCommitMarkableResourceRecoveryModuleOfCompleteBranches = true;

    private int commitMarkableResourceRecordDeleteBatchSize = 30000;

    private Map<String, Boolean> performImmediateCleanupOfCommitMarkableResourceBranchesMap = new HashMap<String, Boolean>();

    private Map<String, Integer> commitMarkableResourceRecordDeleteBatchSizeMap = new HashMap<String, Integer>();

    private static final String defaultTransactionOperationsProviderClassName = ServerVMClientUserTransactionOperationsProvider.class.getName();
    private volatile String userTransactionOperationsProviderClassName = defaultTransactionOperationsProviderClassName;
    private volatile UserTransactionOperationsProvider userTransactionOperationsProvider = null;

    private boolean strictJTA12DuplicateXAENDPROTOErr = false;

    private boolean transactionToThreadListenersEnabled = false;

    private List<String> xaResourceIsSameRMClassNames = new ArrayList<>();

    /**
     * Returns true if subtransactions are allowed.
     * Warning: subtransactions are not JTA spec compliant and most XA resource managers don't understand them.
     * <p>
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.jta.supportSubtransactions
     *
     * @return true if subtransactions are enabled, false otherwise.
     */
    public boolean isSupportSubtransactions() {
        return supportSubtransactions;
    }

    /**
     * Sets if subtransactions should be allowed.
     *
     * @param supportSubtransactions true to enable subtransactions, false to disable.
     */
    public void setSupportSubtransactions(boolean supportSubtransactions) {
        this.supportSubtransactions = supportSubtransactions;
    }

    /**
     * Returns the class name of the jakarta.transaction.TransactionManager implementation.
     * <p>
     * Default: "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple"
     * Equivalent deprecated property: com.arjuna.ats.jta.transactionManagerClassName
     *
     * @return the name of the class implementing TransactionManager.
     */
    public String getTransactionManagerClassName() {
        return transactionManagerClassName;
    }

    /**
     * Sets the class name of the jakarta.transaction.TransactionManager implementation.
     *
     * @param transactionManagerClassName the name of a class which implements TransactionManager.
     */
    public void setTransactionManagerClassName(String transactionManagerClassName) {
        synchronized (this) {
            if (transactionManagerClassName == null) {
                this.transactionManager = null;
            } else if (!transactionManagerClassName.equals(this.transactionManagerClassName)) {
                this.transactionManager = null;
            }
            this.transactionManagerClassName = transactionManagerClassName;
        }
    }

    /**
     * Returns an instance of a class implementing jakarta.transaction.TransactionManager.
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a jakarta.transaction.TransactionManager implementation instance, or null.
     */
    public TransactionManager getTransactionManager() {
        if (transactionManager == null && transactionManagerClassName != null) {
            synchronized (this) {
                if (transactionManager == null && transactionManagerClassName != null) {
                    TransactionManager instance = ClassloadingUtility.loadAndInstantiateClass(TransactionManager.class, transactionManagerClassName, null);
                    transactionManager = instance;
                }
            }
        }

        return transactionManager;
    }

    /**
     * Sets the instance of jakarta.transaction.TransactionManager
     *
     * @param instance an Object that implements jakarta.transaction.TransactionManager, or null.
     */
    public void setTransactionManager(TransactionManager instance) {
        synchronized (this) {
            TransactionManager oldInstance = this.transactionManager;
            transactionManager = instance;

            if (instance == null) {
                this.transactionManagerClassName = null;
            } else if (instance != oldInstance) {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.transactionManagerClassName = name;
            }
        }
    }

    /**
     * Returns the class name of the jakarta.transaction.UserTransaction implementation.
     * <p>
     * Default: "com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple"
     * Equivalent deprecated property: com.arjuna.ats.jta.userTransactionClassName
     *
     * @return the name of the class implementing jakarta.transaction.UserTransaction.
     */
    public String getUserTransactionClassName() {
        return userTransactionClassName;
    }

    /**
     * Sets the class name of the jakarta.transaction.UserTransaction implementation.
     *
     * @param userTransactionClassName the name of a class which implements jakarta.transaction.UserTransaction.
     */
    public void setUserTransactionClassName(String userTransactionClassName) {
        synchronized (this) {
            if (userTransactionClassName == null) {
                this.userTransaction = null;
            } else if (!userTransactionClassName.equals(this.userTransactionClassName)) {
                this.userTransaction = null;
            }
            this.userTransactionClassName = userTransactionClassName;
        }
    }

    /**
     * Returns an instance of a class implementing jakarta.transaction.UserTransaction.
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a jakarta.transaction.UserTransaction implementation instance, or null.
     */
    public UserTransaction getUserTransaction() {
        if (userTransaction == null && userTransactionClassName != null) {
            synchronized (this) {
                if (userTransaction == null && userTransactionClassName != null) {
                    UserTransaction instance = ClassloadingUtility.loadAndInstantiateClass(UserTransaction.class, userTransactionClassName, null);
                    userTransaction = instance;
                }
            }
        }

        return userTransaction;
    }

    /**
     * Sets the instance of jakarta.transaction.UserTransaction
     *
     * @param instance an Object that implements jakarta.transaction.UserTransaction, or null.
     */
    public void setUserTransaction(UserTransaction instance) {
        synchronized (this) {
            UserTransaction oldInstance = this.userTransaction;
            userTransaction = instance;

            if (instance == null) {
                this.userTransactionClassName = null;
            } else if (instance != oldInstance) {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.userTransactionClassName = name;
            }
        }
    }

    /**
     * Returns the class name of the jakarta.transaction.TransactionSynchronizationRegistry implementation.
     * <p>
     * Default: "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple"
     * Equivalent deprecated property: com.arjuna.ats.jta.transactionSynchronizationRegistryClassName
     *
     * @return the name of the class implementing jakarta.transaction.TransactionSynchronizationRegistry.
     */
    public String getTransactionSynchronizationRegistryClassName() {
        return transactionSynchronizationRegistryClassName;
    }

    /**
     * Sets the class name of the jakarta.transaction.TransactionSynchronizationRegistry implementation.
     *
     * @param transactionSynchronizationRegistryClassName the name of a class which implements TransactionSynchronizationRegistry.
     */
    public void setTransactionSynchronizationRegistryClassName(String transactionSynchronizationRegistryClassName) {
        synchronized (this) {
            if (transactionSynchronizationRegistryClassName == null) {
                this.transactionSynchronizationRegistry = null;
            } else if (!transactionSynchronizationRegistryClassName.equals(this.transactionSynchronizationRegistryClassName)) {
                this.transactionSynchronizationRegistry = null;
            }
            this.transactionSynchronizationRegistryClassName = transactionSynchronizationRegistryClassName;
        }
    }

    /**
     * Returns an instance of a class implementing javax.transaction.transactionSynchronizationRegistry.
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a jakarta.transaction.TransactionSynchronizationRegistry implementation instance, or null.
     */
    public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() {
        if (transactionSynchronizationRegistry == null && transactionSynchronizationRegistryClassName != null) {
            synchronized (this) {
                if (transactionSynchronizationRegistry == null && transactionSynchronizationRegistryClassName != null) {
                    TransactionSynchronizationRegistry instance = ClassloadingUtility.loadAndInstantiateClass(TransactionSynchronizationRegistry.class, transactionSynchronizationRegistryClassName, null);
                    transactionSynchronizationRegistry = instance;
                }
            }
        }

        return transactionSynchronizationRegistry;
    }

    /**
     * Sets the instance of jakarta.transaction.TransactionSynchronizationRegistry
     *
     * @param instance an Object that implements jakarta.transaction.TransactionSynchronizationRegistry, or null.
     */
    public void setTransactionSynchronizationRegistry(TransactionSynchronizationRegistry instance) {
        synchronized (this) {
            TransactionSynchronizationRegistry oldInstance = this.transactionSynchronizationRegistry;
            transactionSynchronizationRegistry = instance;

            if (instance == null) {
                this.transactionSynchronizationRegistryClassName = null;
            } else if (instance != oldInstance) {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.transactionSynchronizationRegistryClassName = name;
            }
        }
    }

    /**
     * Returns the set of node identifiers for which recovery will be performed.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.jta.xaRecoveryNode
     *
     * @return the set of node identifiers for which to perform recovery.
     */
    public List<String> getXaRecoveryNodes() {
        return new ArrayList<String>(xaRecoveryNodes);
    }


    /**
     * Sets the node identifiers for which recovery will be performed.
     * The provided list will be copied, not retained.
     *
     * @param xaRecoveryNodes the set of node identifiers for which to perform recovery.
     */
    public void setXaRecoveryNodes(List<String> xaRecoveryNodes) {
        if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debugf("Setting up node identifiers '%s' for which recovery"
                    + " will be performed", xaRecoveryNodes);
        }

        if (xaRecoveryNodes == null) {
            this.xaRecoveryNodes = new ArrayList<String>();
        } else {
            this.xaRecoveryNodes = new ArrayList<String>(xaRecoveryNodes);
        }
    }

    /**
     * Returns the set of XAResourceRecovery implementation class names,
     * each of which may have configuration data appended to it.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.jta.recovery.XAResourceRecovery
     *
     * @return the set of XAResourceRecovery implementations with their configuration data.
     */
    public List<String> getXaResourceRecoveryClassNames() {
        synchronized (this) {
            return new ArrayList<String>(xaResourceRecoveryClassNames);
        }
    }

    /**
     * Sets the class names of the XAResourceRecovery implementations that will be used,
     * each optionally including trailing configuration data.
     * The provided list will be copied, not retained.
     *
     * @param xaResourceRecoveryClassNames the XAResourceRecovery implementation class names and configuration.
     */
    public void setXaResourceRecoveryClassNames(List<String> xaResourceRecoveryClassNames) {
        synchronized (this) {
            if (xaResourceRecoveryClassNames == null) {
                this.xaResourceRecoveries = null;
                this.xaResourceRecoveryClassNames = new ArrayList<String>();
            } else if (!xaResourceRecoveryClassNames.equals(this.xaResourceRecoveryClassNames)) {
                this.xaResourceRecoveries = null;
                this.xaResourceRecoveryClassNames = new ArrayList<String>(xaResourceRecoveryClassNames);
            }
        }
    }

    /**
     * Returns the set of XAResourceRecovery instances.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation of one or more
     * elements fails, this method will log an appropriate warning and return a non-null set with
     * fewer elements.
     *
     * @return the set of XAResourceRecovery instances.
     */
    public List<XAResourceRecovery> getXaResourceRecoveries() {
        synchronized (this) {
            if (xaResourceRecoveries == null) {
                List<XAResourceRecovery> instances = ClassloadingUtility.loadAndInstantiateClassesWithInit(XAResourceRecovery.class, xaResourceRecoveryClassNames);
                xaResourceRecoveries = instances;
            }
            return new ArrayList<XAResourceRecovery>(xaResourceRecoveries);
        }
    }

    /**
     * Sets the instances of XAResourceRecovery.
     * The provided list will be copied, not retained.
     *
     * @param xaResourceRecoveries the set of XAResourceRecovery instances.
     */
    public void setXaResourceRecoveries(List<XAResourceRecovery> xaResourceRecoveries) {
        synchronized (this) {
            if (xaResourceRecoveries == null) {
                this.xaResourceRecoveries = new ArrayList<XAResourceRecovery>();
                this.xaResourceRecoveryClassNames = new ArrayList<String>();
            } else {
                this.xaResourceRecoveries = new ArrayList<XAResourceRecovery>(xaResourceRecoveries);
                List<String> names = ClassloadingUtility.getNamesForClasses(this.xaResourceRecoveries);
                this.xaResourceRecoveryClassNames = names;
            }
        }
    }

    /**
     * Returns a list of names of classes that implement XAResourceOrphanFilter.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * Default: empty list.
     * Equivalent deprecated property prefix:
     *
     * @return a list of XAResourceOrphanFilter implementation class names.
     */
    public List<String> getXaResourceOrphanFilterClassNames() {
        synchronized (this) {
            return new ArrayList<String>(xaResourceOrphanFilterClassNames);
        }
    }

    /**
     * Sets the class names of XAResourceOrphanFilter implementations.
     * List elements should be names of classes that implement XAResourceOrphanFilter.
     * The provided list will be copied, not retained.
     *
     * @param xaResourceOrphanFilterClassNames a list of XAResourceOrphanFilter implementation classes.
     */
    public void setXaResourceOrphanFilterClassNames(List<String> xaResourceOrphanFilterClassNames) {
        synchronized (this) {
            if (xaResourceOrphanFilterClassNames == null) {
                this.xaResourceOrphanFilters = new ArrayList<XAResourceOrphanFilter>();
                this.xaResourceOrphanFilterClassNames = new ArrayList<String>();
            } else if (!xaResourceOrphanFilterClassNames.equals(this.xaResourceOrphanFilterClassNames)) {
                this.xaResourceOrphanFilters = null;
                this.xaResourceOrphanFilterClassNames = new ArrayList<String>(xaResourceOrphanFilterClassNames);
            }
        }
    }

    /**
     * Returns the set of XAResourceOrphanFilter instances.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation of one or more
     * elements fails, this method will log an appropriate warning and return a non-null set with
     * fewer elements.
     *
     * @return the set of XAResourceOrphanFilter instances.
     */
    public List<XAResourceOrphanFilter> getXaResourceOrphanFilters() {
        synchronized (this) {
            if (xaResourceOrphanFilters == null) {
                List<XAResourceOrphanFilter> instances = ClassloadingUtility.loadAndInstantiateClassesWithInit(XAResourceOrphanFilter.class, xaResourceOrphanFilterClassNames);
                xaResourceOrphanFilters = instances;
            }
            return new ArrayList<XAResourceOrphanFilter>(xaResourceOrphanFilters);
        }
    }

    /**
     * Sets the instances of XAResourceOrphanFilter.
     * The provided list will be copied, not retained.
     *
     * @param xaResourceOrphanFilters the set of XAResourceOrphanFilter instances.
     */
    public void setXaResourceOrphanFilters(List<XAResourceOrphanFilter> xaResourceOrphanFilters) {
        synchronized (this) {
            if (xaResourceOrphanFilters == null) {
                this.xaResourceOrphanFilters = new ArrayList<XAResourceOrphanFilter>();
                this.xaResourceOrphanFilterClassNames = new ArrayList<String>();
            } else {
                this.xaResourceOrphanFilters = new ArrayList<XAResourceOrphanFilter>(xaResourceOrphanFilters);
                List<String> names = ClassloadingUtility.getNamesForClasses(this.xaResourceOrphanFilters);
                this.xaResourceOrphanFilterClassNames = names;
            }
        }
    }

    /**
     * Deprecated: This method will be removed in the next major release
     * <p>
     * Returns if connections associated to XAResources that fail during prepare should be cleaned up immediately.
     * TODO move to JDBC module as it's only for our own connection manager?
     * <p>
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.jta.xaRollbackOptimization
     *
     * @return true for cleanup during prepare, false for cleanup during phase two rollback.
     */
    @Deprecated
    public boolean isXaRollbackOptimization() {
        return false;
    }

    /**
     * Deprecated: This method will be removed in the next major release
     * <p>
     * Sets if failed resources should be cleaned up during prepare or during phase two.
     *
     * @param xaRollbackOptimization true for immediate cleanup, false for phase two cleanup.
     */
    @Deprecated
    public void setXaRollbackOptimization(boolean xaRollbackOptimization) {
    }

    /**
     * Returns if XAResources that can't be recovered should be assumed to have completed.
     * WARNING: enabling this property is not recommended and may cause inconsistency if
     * your recovery configuration is incorrect or resource managers are not available.
     * <p>
     * Default: false.
     * Equivalent deprecated property: com.arjuna.ats.jta.xaAssumeRecoveryComplete
     *
     * @return true for assumed completion, false for no such assumption.
     */
    public boolean isXaAssumeRecoveryComplete() {
        return xaAssumeRecoveryComplete;
    }

    /**
     * Sets if XAResources that can't be recovered should be assumed to have completed.
     *
     * @param xaAssumeRecoveryComplete true to enable completion assumption, false to disable.
     */
    public void setXaAssumeRecoveryComplete(boolean xaAssumeRecoveryComplete) {
        this.xaAssumeRecoveryComplete = xaAssumeRecoveryComplete;
    }

    /**
     * Returns the JNDI bind name for the implementation of UserTransaction.
     * <p>
     * Default: "java:/UserTransaction"
     * Equivalent deprecated property: com.arjuna.ats.jta.utils.UTJNDIContext
     *
     * @return the JNDI bind location for the UserTransaction interface.
     */
    public String getUserTransactionJNDIContext() {
        return userTransactionJNDIContext;
    }

    /**
     * Sets the JNDI bind name for the implementation of UserTransaction.
     *
     * @param userTransactionJNDIContext the JNDI bind location for the UserTransaction interface.
     */
    public void setUserTransactionJNDIContext(String userTransactionJNDIContext) {
        this.userTransactionJNDIContext = userTransactionJNDIContext;
    }

    /**
     * Returns the JNDI bind name for the implementation of TransactionManager.
     * <p>
     * Default: "java:/TransactionManager"
     * Equivalent deprecated property: com.arjuna.ats.jta.utils.TMJNDIContext
     *
     * @return the JNDI bind location for the TransactionManager interface.
     */
    public String getTransactionManagerJNDIContext() {
        return transactionManagerJNDIContext;
    }

    /**
     * Sets the JNDI bind name for the implementation of TransactionManager.
     *
     * @param transactionManagerJNDIContext the JNDI bind location for the TransactionManager interface.
     */
    public void setTransactionManagerJNDIContext(String transactionManagerJNDIContext) {
        this.transactionManagerJNDIContext = transactionManagerJNDIContext;
    }

    /**
     * Returns the JNDI bind name for the implementation of TransactionSynchronizationRegistry.
     * <p>
     * Default: "java:/TransactionSynchronizationRegistry"
     * Equivalent deprecated property: com.arjuna.ats.jta.utils.TSRJNDIContext
     *
     * @return the JNDI bind location for the TransactionSynchronizationRegistry interface.
     */
    public String getTransactionSynchronizationRegistryJNDIContext() {
        return transactionSynchronizationRegistryJNDIContext;
    }

    /**
     * Sets tje JNDI bind name for the implementation of TransactionSynchronizationRegistry.
     *
     * @param transactionSynchronizationRegistryJNDIContext the JNDI bind location for the TransactionSynchronizationRegistry implementation.
     */
    public void setTransactionSynchronizationRegistryJNDIContext(String transactionSynchronizationRegistryJNDIContext) {
        this.transactionSynchronizationRegistryJNDIContext = transactionSynchronizationRegistryJNDIContext;
    }

    /**
     * Returns the set of XAResourceMap implementation class names used to configure XAException handling.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.jta.xaErrorHandler
     *
     * @return a set of class names, each an implementation of XAResourceMap.
     */
    public List<String> getXaResourceMapClassNames() {
        synchronized (this) {
            return new ArrayList<String>(xaResourceMapClassNames);
        }
    }

    /**
     * Sets the names of the XAResourceMap classes used for XAException handling.
     * The provided list will be copied, not retained.
     *
     * @param xaResourceMapClassNames a set of names of classes, each implementing XAResourceMap.
     */
    public void setXaResourceMapClassNames(List<String> xaResourceMapClassNames) {
        synchronized (this) {
            if (xaResourceMapClassNames == null) {
                this.xaResourceMaps = null;
                this.xaResourceMapClassNames = new ArrayList<String>();
            } else if (!xaResourceMapClassNames.equals(this.xaResourceMapClassNames)) {
                this.xaResourceMaps = null;
                this.xaResourceMapClassNames = new ArrayList<String>(xaResourceMapClassNames);
            }
        }
    }

    /**
     * Returns the set of XAResourceMap instances.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation of one or more
     * elements fails, this method will log an appropriate warning and return a non-null set with
     * fewer elements.
     *
     * @return the set of XAResourceMap instances.
     */
    public List<XAResourceMap> getXaResourceMaps() {
        synchronized (this) {
            if (xaResourceMaps == null) {
                List<XAResourceMap> instances = ClassloadingUtility.loadAndInstantiateClassesWithInit(XAResourceMap.class, xaResourceMapClassNames);
                xaResourceMaps = instances;
            }
            return new ArrayList<XAResourceMap>(xaResourceMaps);
        }
    }

    /**
     * Sets the instances of XAResourceMap.
     * The provided list will be copied, not retained.
     *
     * @param xaResourceMaps the set of XAResourceMap instances.
     */
    public void setXaResourceMaps(List<XAResourceMap> xaResourceMaps) {
        synchronized (this) {
            if (xaResourceMaps == null) {
                this.xaResourceMaps = new ArrayList<XAResourceMap>();
                this.xaResourceMapClassNames = new ArrayList<String>();
            } else {
                this.xaResourceMaps = new ArrayList<XAResourceMap>(xaResourceMaps);
                List<String> names = ClassloadingUtility.getNamesForClasses(this.xaResourceMaps);
                this.xaResourceMapClassNames = names;
            }
        }
    }

    /**
     * Returns if the transaction timeout is passed on to the enlisted XAResources.
     * <p>
     * Default: true.
     * Equivalent deprecated property: com.arjuna.ats.jta.xaTransactionTimeoutEnabled
     *
     * @return true to pass transaction timeout configuration on to the XAResources, false to skip setting resource timeout.
     */
    public boolean isXaTransactionTimeoutEnabled() {
        return xaTransactionTimeoutEnabled;
    }

    /**
     * Sets if the transaction timeout should be passed to the enlisted XAResource or not.
     *
     * @param xaTransactionTimeoutEnabled true to enable setting XAResource timeouts, false to disable.
     */
    public void setXaTransactionTimeoutEnabled(boolean xaTransactionTimeoutEnabled) {
        this.xaTransactionTimeoutEnabled = xaTransactionTimeoutEnabled;
    }

    /**
     * Returns the class name of the marker interface used to indicate a LastResource.
     * <p>
     * Default: null.
     * Equivalent deprecated property: com.arjuna.ats.jta.lastResourceOptimisationInterfaceClassName
     *
     * @return the class name of the market interface for LastResource handling.
     */
    public String getLastResourceOptimisationInterfaceClassName() {
        return lastResourceOptimisationInterfaceClassName;
    }

    /**
     * Sets the class name of the marker interface used to indicate a LastResource.
     *
     * @param lastResourceOptimisationInterfaceClassName the class name of the marker interface.
     */
    public void setLastResourceOptimisationInterfaceClassName(String lastResourceOptimisationInterfaceClassName) {
        synchronized (this) {
            if (lastResourceOptimisationInterfaceClassName == null) {
                this.lastResourceOptimisationInterface = null;
            } else if (!lastResourceOptimisationInterfaceClassName.equals(this.lastResourceOptimisationInterfaceClassName)) {
                this.lastResourceOptimisationInterface = null;
            }
            this.lastResourceOptimisationInterfaceClassName = lastResourceOptimisationInterfaceClassName;
        }
    }

    /**
     * Returns the Class representing the marker interface for LastResource.
     * <p>
     * If there is no Class set and loading fails, this method will log an appropriate warning
     * and return null, not throw an exception.
     *
     * @return the LastResource marker interface.
     */
    public Class getLastResourceOptimisationInterface() {
        if (lastResourceOptimisationInterface == null && lastResourceOptimisationInterfaceClassName != null) {
            synchronized (this) {
                if (lastResourceOptimisationInterface == null && lastResourceOptimisationInterfaceClassName != null) {
                    lastResourceOptimisationInterface = ClassloadingUtility.loadClass(lastResourceOptimisationInterfaceClassName);
                }
            }
        }

        return lastResourceOptimisationInterface;
    }

    /**
     * Sets a Class to use as the marker interface for LastResource
     *
     * @param clazz a marker interface Class, or null.
     */
    public void setLastResourceOptimisationInterface(Class clazz) {
        synchronized (this) {
            Class oldClazz = this.lastResourceOptimisationInterface;
            lastResourceOptimisationInterface = clazz;

            if (clazz == null) {
                this.lastResourceOptimisationInterfaceClassName = null;
            } else if (clazz != oldClazz) {
                String name = ClassloadingUtility.getNameForClass(clazz);
                this.lastResourceOptimisationInterfaceClassName = name;
            }
        }
    }

    /**
     * Returns the class name of the XAResourceRecordWrappingPlugin implementation.
     * <p>
     * Default: null
     *
     * @return the name of the class implementing XAResourceRecordWrappingPlugin.
     */
    public String getXaResourceRecordWrappingPluginClassName() {
        return xaResourceRecordWrappingPluginClassName;
    }

    /**
     * Sets the class name of the XAResourceRecordWrappingPlugin implementation.
     *
     * @param xaResourceRecordWrappingPluginClassName the name of a class which implements XAResourceRecordWrappingPlugin.
     */
    public void setXaResourceRecordWrappingPluginClassName(String xaResourceRecordWrappingPluginClassName) {
        synchronized (this) {
            if (xaResourceRecordWrappingPluginClassName == null) {
                this.xaResourceRecordWrappingPlugin = null;
            } else if (!xaResourceRecordWrappingPluginClassName.equals(this.xaResourceRecordWrappingPluginClassName)) {
                this.xaResourceRecordWrappingPlugin = null;
            }
            this.xaResourceRecordWrappingPluginClassName = xaResourceRecordWrappingPluginClassName;
        }
    }

    /**
     * Returns an instance of a class implementing XAResourceRecordWrappingPlugin.
     * <p>
     * If there is no pre-instantiated instance set and classloading or instantiation fails,
     * this method will log an appropriate warning and return null, not throw an exception.
     *
     * @return a XAResourceRecordWrappingPlugin implementation instance, or null.
     */
    public XAResourceRecordWrappingPlugin getXAResourceRecordWrappingPlugin() {
        if (xaResourceRecordWrappingPlugin == null && xaResourceRecordWrappingPluginClassName != null) {
            synchronized (this) {
                if (xaResourceRecordWrappingPlugin == null && xaResourceRecordWrappingPluginClassName != null) {
                    XAResourceRecordWrappingPlugin instance = ClassloadingUtility.loadAndInstantiateClass(XAResourceRecordWrappingPlugin.class, xaResourceRecordWrappingPluginClassName, null);
                    xaResourceRecordWrappingPlugin = instance;
                }
            }
        }

        return xaResourceRecordWrappingPlugin;
    }

    /**
     * Sets the instance of XAResourceRecordWrappingPlugin
     *
     * @param instance an Object that implements XAResourceRecordWrappingPlugin, or null.
     */
    public void setXAResourceRecordWrappingPlugin(XAResourceRecordWrappingPlugin instance) {
        synchronized (this) {
            XAResourceRecordWrappingPlugin oldInstance = this.xaResourceRecordWrappingPlugin;
            xaResourceRecordWrappingPlugin = instance;

            if (instance == null) {
                this.xaResourceRecordWrappingPluginClassName = null;
            } else if (instance != oldInstance) {
                String name = ClassloadingUtility.getNameForClass(instance);
                this.xaResourceRecordWrappingPluginClassName = name;
            }
        }
    }

    /**
     * Returns maximum size of a thread pool, used to execute asynchronous commits.
     *
     * @return maximum size of a thread pool, used to execute asynchronous commits.
     */
    public int getAsyncCommitPoolSize() {
        return asyncCommitPoolSize;
    }

    /**
     * Sets maximum size of a thread pool, used to execute asynchronous commits.
     *
     * @param asyncCommitPoolSize maximum size of a thread pool, used to execute asynchronous commits.
     */
    public void setAsyncCommitPoolSize(int asyncCommitPoolSize) {
        this.asyncCommitPoolSize = asyncCommitPoolSize;
    }

    public int getOrphanSafetyInterval() {
        return orphanSafetyInterval;
    }

    /**
     * Set the amount of time to wait before deciding if the Xid is orphaned.
     * <p>
     * It is important because if this is too short and a transaction completes
     * between the two recovery scan phases the xids from the RM will be considered
     * as orphaned. Although this does not cause data integrity issues it can
     * appear unsettling.
     *
     * @param orphanSafetyInterval
     */
    public void setOrphanSafetyInterval(int orphanSafetyInterval) {
        this.orphanSafetyInterval = orphanSafetyInterval;
    }

    /**
     * Get the name of the table to use for storing commit markable resources
     * commit state notifiers in.
     *
     * @return the default table name
     */
    public String getDefaultCommitMarkableTableName() {
        return commitMarkableResourceTableName;
    }

    /**
     * Set the name of the table to use for storing commit markable resources
     * commit state notifiers in.
     *
     * @param commitMarkableResourceTableName The name of the table.
     */
    public void setDefaultCommitMarkableResourceTableName(
            String commitMarkableResourceTableName) {
        this.commitMarkableResourceTableName = commitMarkableResourceTableName;
    }

    /**
     * Get the name of the table to use for storing commit markable resources
     * commit state notifiers in.
     *
     * @return the table name map
     */
    public Map<String, String> getCommitMarkableResourceTableNameMap() {
        synchronized (this) {
            return commitMarkableResourceTableNameMap;
        }
    }

    /**
     * Set the name of the table to use for storing commit markable resources
     * commit state notifiers in.
     *
     * @param commitMarkableResourceTableNameMap The name of the table.
     */
    public void setCommitMarkableResourceTableNameMap(
            Map<String, String> commitMarkableResourceTableNameMap) {
        synchronized (this) {
            if (commitMarkableResourceTableNameMap == null) {
                this.commitMarkableResourceTableNameMap = new HashMap<String, String>();
            } else if (!commitMarkableResourceTableNameMap
                    .equals(this.commitMarkableResourceTableNameMap)) {
                this.commitMarkableResourceTableNameMap = new HashMap<String, String>(
                        commitMarkableResourceTableNameMap);
            }
        }
    }

    /**
     * Retrieve the list of JNDI names that will be queried for committed 1PC
     * resources that were enlisted in 2PC transactions.
     *
     * @return The list of JNDI names
     */
    public List<String> getCommitMarkableResourceJNDINames() {
        synchronized (this) {
            return new ArrayList<String>(commitMarkableResourceJNDINames);
        }
    }

    /**
     * Set the list of JNDI names to apply a special LastResource algorithm that
     * allows us to store data about the transaction in the resource manager to
     * determine the outcome of the resource after crash/
     * <p>
     * If you change the list of jndinames you _can't_ change the JNDI name of
     * the connections until they have been recovered.
     *
     * @param commitMarkableResourceJNDINames The list of JNDI names
     */
    public void setCommitMarkableResourceJNDINames(
            List<String> commitMarkableResourceJNDINames) {
        synchronized (this) {
            if (commitMarkableResourceJNDINames == null) {
                this.commitMarkableResourceJNDINames = new ArrayList<String>();
            } else if (!commitMarkableResourceJNDINames
                    .equals(this.commitMarkableResourceJNDINames)) {
                this.commitMarkableResourceJNDINames = new ArrayList<String>(
                        commitMarkableResourceJNDINames);
            }
        }
    }

    /**
     * If this returns true, a synchronization is registered by the
     * CommitMarkableResourceRecord to delete records as soon as the transaction
     * completes.
     *
     * @return Whether to perform immediate cleanup of CRRs
     */
    public boolean isPerformImmediateCleanupOfCommitMarkableResourceBranches() {
        return performImmediateCleanupOfCommitMarkableResourceBranches;
    }

    /**
     * Notify the transaction manager to delete resource records immediately
     * after a transaction commits.
     *
     * @param performImmediateCleanupOfCommitMarkableResourceBranches
     */
    public void setPerformImmediateCleanupOfCommitMarkableResourceBranches(
            boolean performImmediateCleanupOfCommitMarkableResourceBranches) {
        this.performImmediateCleanupOfCommitMarkableResourceBranches = performImmediateCleanupOfCommitMarkableResourceBranches;
    }

    /**
     * Allow the default policy of whether to use a synchronization to remove
     * the branch should be overriden.
     *
     * @return whether to perform immediate cleanup of branches or batch them
     */
    public Map<String, Boolean> getPerformImmediateCleanupOfCommitMarkableResourceBranchesMap() {
        synchronized (this) {
            return performImmediateCleanupOfCommitMarkableResourceBranchesMap;
        }
    }

    /**
     * Allow the default policy of whether to use a synchronization to remove
     * the branch should be overriden.
     *
     * @param performImmediateCleanupOfCommitMarkableResourceBranchesMap The name of the table.
     */
    public void setPerformImmediateCleanupOfCommitMarkableResourceBranchesMap(
            Map<String, Boolean> performImmediateCleanupOfCommitMarkableResourceBranchesMap) {
        synchronized (this) {
            if (performImmediateCleanupOfCommitMarkableResourceBranchesMap == null) {
                this.performImmediateCleanupOfCommitMarkableResourceBranchesMap = new HashMap<String, Boolean>();
            } else if (!performImmediateCleanupOfCommitMarkableResourceBranchesMap
                    .equals(this.performImmediateCleanupOfCommitMarkableResourceBranchesMap)) {
                this.performImmediateCleanupOfCommitMarkableResourceBranchesMap = new HashMap<String, Boolean>(
                        performImmediateCleanupOfCommitMarkableResourceBranchesMap);
            }
        }
    }

    /**
     * If this is a positive number, use a batch to delete
     * CommitMarkableResourceRecord from the database.
     *
     * @return -1 to prevent batching.
     */
    public int getCommitMarkableResourceRecordDeleteBatchSize() {
        return commitMarkableResourceRecordDeleteBatchSize;
    }

    /**
     * Alter the default batch size or set to -1 to disable batch deletion of
     * CommitMarkableResourceRecord from the database.
     *
     * @param batchSize -1 to prevent batching.
     */
    public void setCommitMarkableResourceRecordDeleteBatchSize(int batchSize) {
        this.commitMarkableResourceRecordDeleteBatchSize = batchSize;
    }

    /**
     * Allow the default policy of a batch size to delete
     * CommitMarkableResourceRecord from the database.
     *
     * @return the maximum batch size to clean up
     */
    public Map<String, Integer> getCommitMarkableResourceRecordDeleteBatchSizeMap() {
        synchronized (this) {
            return commitMarkableResourceRecordDeleteBatchSizeMap;
        }
    }

    /**
     * Allow the default policy of a batch size to delete
     * CommitMarkableResourceRecord from the database.
     *
     * @param commitMarkableResourceRecordDeleteBatchSizeMap size
     */
    public void setCommitMarkableResourceRecordDeleteBatchSizeMap(
            Map<String, Integer> commitMarkableResourceRecordDeleteBatchSizeMap) {
        synchronized (this) {
            if (commitMarkableResourceRecordDeleteBatchSizeMap == null) {
                this.commitMarkableResourceRecordDeleteBatchSizeMap = new HashMap<String, Integer>();
            } else if (!commitMarkableResourceRecordDeleteBatchSizeMap
                    .equals(this.commitMarkableResourceRecordDeleteBatchSizeMap)) {
                this.commitMarkableResourceRecordDeleteBatchSizeMap = new HashMap<String, Integer>(
                        commitMarkableResourceRecordDeleteBatchSizeMap);
            }
        }
    }

    /**
     * If this is enabled we will tell the recovery module when we complete
     * branches. This means they will not in normal mode need fetching from the
     * database before we can delete them.
     *
     * @return whether to notify CMR module
     */
    public boolean isNotifyCommitMarkableResourceRecoveryModuleOfCompleteBranches() {
        return notifyCommitMarkableResourceRecoveryModuleOfCompleteBranches;
    }

    /**
     * Allow the default policy of whether to use a synchronization to remove
     * the branch should be overriden.
     */
    public void setNotifyCommitMarkableResourceRecoveryModuleOfCompleteBranches(
            boolean notifyCommitMarkableResourceRecoveryModuleOfCompleteBranches) {
        this.notifyCommitMarkableResourceRecoveryModuleOfCompleteBranches = notifyCommitMarkableResourceRecoveryModuleOfCompleteBranches;
    }

    /**
     * <p>
     * Setting of class name that defines {@link UserTransactionOperationsProvider}.
     * The provider is later used to get more info about {@link UserTransaction}.
     * <p>
     * When null is set then default provider implementation is used which is
     * {@code #defaultTransactionOperationsProviderClassName}.
     *
     * @param providerClassName class name implementing {@link UserTransactionOperationsProvider}
     */
    public void setUserTransactionOperationsProviderClassName(String providerClassName) {
        synchronized (this) {
            if (!userTransactionOperationsProviderClassName.equals(providerClassName)) {
                userTransactionOperationsProvider = null;
            }

            if (providerClassName == null) {
                this.userTransactionOperationsProviderClassName = defaultTransactionOperationsProviderClassName;
            } else {
                userTransactionOperationsProviderClassName = providerClassName;
            }
        }
    }

    /**
     * Get class name that is used as {@link UserTransactionOperationsProvider}
     *
     * @return class name implementing {@link UserTransactionOperationsProvider}
     */
    public String getUserTransactionOperationsProviderClassName() {
        return this.userTransactionOperationsProviderClassName;
    }

    /**
     * Returning singleton instance of {@link UserTransactionOperationsProvider} instantiated based
     * based on name specified by {@link #setUserTransactionOperationsProviderClassName(String)}.<br>
     * When class name is redefined during runtime there should be instantiated new provider.
     *
     * @return instance of class implementing {@link UserTransactionOperationsProvider}
     */
    public UserTransactionOperationsProvider getUserTransactionOperationsProvider() {

        if (userTransactionOperationsProvider == null && userTransactionOperationsProviderClassName != null) {
            synchronized (this) {
                if (userTransactionOperationsProvider == null && userTransactionOperationsProviderClassName != null) {
                    userTransactionOperationsProvider = ClassloadingUtility.loadAndInstantiateClass(UserTransactionOperationsProvider.class,
                            userTransactionOperationsProviderClassName, null);
                }
            }
        }

        return userTransactionOperationsProvider;
    }

    /**
     * Returns true if we would treat XAER_PROTO out of duplicate xar as an error.
     * <p>
     * Default: false.
     *
     * @return true if we treat XAER_PROTO out of duplicate xar as errors
     */

    public boolean isStrictJTA12DuplicateXAENDPROTOErr() {
        return strictJTA12DuplicateXAENDPROTOErr;
    }

    /**
     * Sets if to not allow non-JTA 1.2 XAR to return XAER_PROTO out of duplicate XAR xa_end calls
     *
     * @param strictJTA12DuplicateXAENDPROTOErr true to enable, false to disable.
     */
    public void setStrictJTA12DuplicateXAENDPROTOErr(boolean strictJTA12DuplicateXAENDPROTOErr) {
        this.strictJTA12DuplicateXAENDPROTOErr = strictJTA12DuplicateXAENDPROTOErr;
    }

    /**
     * Returns true if the listeners announcing of the change of the thread transaction association happens.
     * This functionality was deprecated with introduction of WFTC for WFLY. WFTC implements the listeners differently.
     * The thread transaction association listener is needed by JPA WFLY integration.
     *
     * @return true to if listeners handling is permitted, false to when is not enabled
     */
    public boolean isTransactionToThreadListenersEnabled() {
        return transactionToThreadListenersEnabled;
    }

    /**
     * Define if transaction to thread associtaion listeners should be enabled.
     *
     * @param transactionToThreadListenersEnabled true to enable listeners handling, false to disable
     */
    public void setTransactionToThreadListenersEnabled(boolean transactionToThreadListenersEnabled) {
        this.transactionToThreadListenersEnabled = transactionToThreadListenersEnabled;
    }

    /**
     * Returns the class names of the XAResource types whose isSameRM method is overridden to return false.
     * This override is useful because some drivers do not implement isSameRM correctly.
     * <p>
     * Default: empty list.
     *
     * @return the class names of XAResource types that should be overriden.
     */
    public List<String> getXaResourceIsSameRMClassNames() {
        synchronized (this) {
            return new ArrayList<String>(xaResourceIsSameRMClassNames);
        }
    }

    /**
     * Sets the class names of the XAResource types whose isSameRM method is overridden to return false.
     * This override is useful because some drivers do not implement isSameRM correctly.
     *
     * @param xaResourceIsSameRMClassNames the class names of XAResource types that should be overriden.
     */
    public void setXaResourceIsSameRMClassNames(List<String> xaResourceIsSameRMClassNames) {
        synchronized (this) {
            if (xaResourceIsSameRMClassNames == null) {
                this.xaResourceIsSameRMClassNames = new ArrayList<>();
            } else if (!xaResourceIsSameRMClassNames.equals(this.xaResourceIsSameRMClassNames)) {
                this.xaResourceIsSameRMClassNames = new ArrayList<>(xaResourceIsSameRMClassNames);
            }
        }
    }
}