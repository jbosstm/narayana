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

/**
 * A JavaBean containing configuration properties for the JTA subsystem.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.jta.")
public class JTAEnvironmentBean
{
    @FullPropertyName(name = "com.arjuna.ats.jta.common.propertiesFile")
    private String propertiesFile = "";

    private boolean supportSubtransactions = false;

    private String jtaTMImplementation = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple";
    private String jtaUTImplementation = "com.arjuna.ats.internal.jta.transaction.arjunacore.UserTransactionImple";
    private String jtaTSRImplementation = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple";

    private int xaBackoffPeriod = 20000;
    private String xaRecoveryNode; // key only
    private boolean xaRollbackOptimization = false;
    private boolean xaAssumeRecoveryComplete = false;

    // com.arjuna.ats.jta.utils.
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.UTJNDIContext")
    private String jtaUTJNDIContext = "java:/UserTransaction";
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.TMJNDIContext")
    private String jtaTMJNDIContext =  "java:/TransactionManager";
    @FullPropertyName(name = "com.arjuna.ats.jta.utils.TSRJNDIContext")
    private String jtaTSRJNDIContext = "java:/TransactionSynchronizationRegistry";

    private String xaErrorHandler; // key only
    private boolean xaTransactionTimeoutEnabled = true;
    private String lastResourceOptimisationInterface = null;

//    public static final String PROPERTIES_FILE = "com.arjuna.ats.jta.common.propertiesFile";
    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

//    public static final String SUPPORT_SUBTRANSACTIONS = "com.arjuna.ats.jta.supportSubtransactions";
    public boolean isSupportSubtransactions()
    {
        return supportSubtransactions;
    }

    public void setSupportSubtransactions(boolean supportSubtransactions)
    {
        this.supportSubtransactions = supportSubtransactions;
    }

//    public static final String JTA_TM_IMPLEMENTATION = "com.arjuna.ats.jta.jtaTMImplementation";
    public String getJtaTMImplementation()
    {
        return jtaTMImplementation;
    }

    public void setJtaTMImplementation(String jtaTMImplementation)
    {
        this.jtaTMImplementation = jtaTMImplementation;
    }

//    public static final String JTA_UT_IMPLEMENTATION = "com.arjuna.ats.jta.jtaUTImplementation";
    public String getJtaUTImplementation()
    {
        return jtaUTImplementation;
    }

    public void setJtaUTImplementation(String jtaUTImplementation)
    {
        this.jtaUTImplementation = jtaUTImplementation;
    }

//    public static final String JTA_TSR_IMPLEMENTATION = "com.arjuna.ats.jta.jtaTSRImplementation";
    public String getJtaTSRImplementation()
    {
        return jtaTSRImplementation;
    }

    public void setJtaTSRImplementation(String jtaTSRImplementation)
    {
        this.jtaTSRImplementation = jtaTSRImplementation;
    }

//    public static final String XA_BACKOFF_PERIOD = "com.arjuna.ats.jta.xaBackoffPeriod";
    public int getXaBackoffPeriod()
    {
        return xaBackoffPeriod;
    }

    public void setXaBackoffPeriod(int xaBackoffPeriod)
    {
        this.xaBackoffPeriod = xaBackoffPeriod;
    }

//    public static final String XA_RECOVERY_NODE = "com.arjuna.ats.jta.xaRecoveryNode";
    public String getXaRecoveryNode()
    {
        return xaRecoveryNode;
    }

    public void setXaRecoveryNode(String xaRecoveryNode)
    {
        this.xaRecoveryNode = xaRecoveryNode;
    }

//    public static final String XA_ROLLBACK_OPTIMIZATION = "com.arjuna.ats.jta.xaRollbackOptimization";
    public boolean isXaRollbackOptimization()
    {
        return xaRollbackOptimization;
    }

    public void setXaRollbackOptimization(boolean xaRollbackOptimization)
    {
        this.xaRollbackOptimization = xaRollbackOptimization;
    }

//    public static final String XA_ASSUME_RECOVERY_COMPLETE = "com.arjuna.ats.jta.xaAssumeRecoveryComplete";
    public boolean isXaAssumeRecoveryComplete()
    {
        return xaAssumeRecoveryComplete;
    }

    public void setXaAssumeRecoveryComplete(boolean xaAssumeRecoveryComplete)
    {
        this.xaAssumeRecoveryComplete = xaAssumeRecoveryComplete;
    }

//    public static final String UT_JNDI_CONTEXT = "com.arjuna.ats.jta.utils.UTJNDIContext";
    public String getJtaUTJNDIContext()
    {
        return jtaUTJNDIContext;
    }

    public void setJtaUTJNDIContext(String jtaUTJNDIContext)
    {
        this.jtaUTJNDIContext = jtaUTJNDIContext;
    }

//    public static final String TM_JNDI_CONTEXT = "com.arjuna.ats.jta.utils.TMJNDIContext";
    public String getJtaTMJNDIContext()
    {
        return jtaTMJNDIContext;
    }

    public void setJtaTMJNDIContext(String jtaTMJNDIContext)
    {
        this.jtaTMJNDIContext = jtaTMJNDIContext;
    }

//    public static final String TSR_JNDI_CONTEXT = "com.arjuna.ats.jta.utils.TSRJNDIContext";
    public String getJtaTSRJNDIContext()
    {
        return jtaTSRJNDIContext;
    }

    public void setJtaTSRJNDIContext(String jtaTSRJNDIContext)
    {
        this.jtaTSRJNDIContext = jtaTSRJNDIContext;
    }

//    public static final String XA_ERROR_HANDLER = "com.arjuna.ats.jta.xaErrorHandler";
    public String getXaErrorHandler()
    {
        return xaErrorHandler;
    }

    public void setXaErrorHandler(String xaErrorHandler)
    {
        this.xaErrorHandler = xaErrorHandler;
    }

//    public static final String XA_TRANSACTION_TIMEOUT_ENABLED = "com.arjuna.ats.jta.xaTransactionTimeoutEnabled";
    public boolean isXaTransactionTimeoutEnabled()
    {
        return xaTransactionTimeoutEnabled;
    }

    public void setXaTransactionTimeoutEnabled(boolean xaTransactionTimeoutEnabled)
    {
        this.xaTransactionTimeoutEnabled = xaTransactionTimeoutEnabled;
    }

//    public static final String LAST_RESOURCE_OPTIMISATION_INTERFACE = "com.arjuna.ats.jta.lastResourceOptimisationInterface";
    public String getLastResourceOptimisationInterface()
    {
        return lastResourceOptimisationInterface;
    }

    public void setLastResourceOptimisationInterface(String lastResourceOptimisationInterface)
    {
        this.lastResourceOptimisationInterface = lastResourceOptimisationInterface;
    }
}
