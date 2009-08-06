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
package com.arjuna.ats.jts.common;

import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;

import java.net.InetAddress;

/**
 * A JavaBean containing configuration properties for the JTS system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.jts.")
public class JTSEnvironmentBean implements JTSEnvironmentBeanMBean
{
    @FullPropertyName(name= "com.arjuna.ats.jts.common.propertiesFile")
    private String propertiesFile;

    private boolean transactionManager = false;
    private boolean needTranContext = false;
    private boolean alwaysPropagateContext = false;
    private String interposition = null;
    private boolean checkedTransactions = false;
    private boolean supportSubtransactions = true;
    private boolean supportRollbackSync = true;
    private boolean supportInterposedSynchronization = false;
    private int defaultTimeout = 60; // deprecated
    private boolean propagateTerminator = false;
    private String contextPropMode = null;
    private int recoveryManagerPort = 4711;
    private String recoveryManagerAddress = "";

    @FullPropertyName(name = "com.arjuna.ats.jts.ots_1_0.timeoutPropagation")
    private boolean timeoutPropagation;


//    public static final String PROPERTIES_FILE = "com.arjuna.ats.jts.common.propertiesFile";
    public String getPropertiesFile()
    {
        return propertiesFile;
    }

    public void setPropertiesFile(String propertiesFile)
    {
        this.propertiesFile = propertiesFile;
    }

//    public static final String TRANSACTION_MANAGER = "com.arjuna.ats.jts.transactionManager";
    public boolean isTransactionManager()
    {
        return transactionManager;
    }

    public void setTransactionManager(boolean transactionManager)
    {
        this.transactionManager = transactionManager;
    }

//    public static final String NEED_TRAN_CONTEXT = "com.arjuna.ats.jts.needTranContext";
    public boolean isNeedTranContext()
    {
        return needTranContext;
    }

    public void setNeedTranContext(boolean needTranContext)
    {
        this.needTranContext = needTranContext;
    }

//    public static final String ALWAYS_PROPAGATE_CONTEXT = "com.arjuna.ats.jts.alwaysPropagateContext";
    public boolean isAlwaysPropagateContext()
    {
        return alwaysPropagateContext;
    }

    public void setAlwaysPropagateContext(boolean alwaysPropagateContext)
    {
        this.alwaysPropagateContext = alwaysPropagateContext;
    }

//    public static final String INTERPOSITION = "com.arjuna.ats.jts.interposition";
    public String getInterposition()
    {
        return interposition;
    }

    public void setInterposition(String interposition)
    {
        this.interposition = interposition;
    }

//    public static final String CHECKED_TRANSACTIONS = "com.arjuna.ats.jts.checkedTransactions";
    public boolean isCheckedTransactions()
    {
        return checkedTransactions;
    }

    public void setCheckedTransactions(boolean checkedTransactions)
    {
        this.checkedTransactions = checkedTransactions;
    }

//    public static final String SUPPORT_SUBTRANSACTIONS = "com.arjuna.ats.jts.supportSubtransactions";
    public boolean isSupportSubtransactions()
    {
        return supportSubtransactions;
    }

    public void setSupportSubtransactions(boolean supportSubtransactions)
    {
        this.supportSubtransactions = supportSubtransactions;
    }

//    public static final String SUPPORT_ROLLBACK_SYNC = "com.arjuna.ats.jts.supportRollbackSync";
    public boolean isSupportRollbackSync()
    {
        return supportRollbackSync;
    }

    public void setSupportRollbackSync(boolean supportRollbackSync)
    {
        this.supportRollbackSync = supportRollbackSync;
    }

//    public static final String SUPPORT_INTERPOSED_SYNCHRONIZATION = "com.arjuna.ats.jts.supportInterposedSynchronization";
    public boolean isSupportInterposedSynchronization()
    {
        return supportInterposedSynchronization;
    }

    public void setSupportInterposedSynchronization(boolean supportInterposedSynchronization)
    {
        this.supportInterposedSynchronization = supportInterposedSynchronization;
    }

//    public static final String DEFAULT_TIMEOUT = "com.arjuna.ats.jts.defaultTimeout"; // deprecated
    public int getDefaultTimeout()
    {
        return defaultTimeout;
    }

    public void setDefaultTimeout(int defaultTimeout)
    {
        this.defaultTimeout = defaultTimeout;
    }

//    public static final String PROPAGATE_TERMINATOR = "com.arjuna.ats.jts.propagateTerminator";
    public boolean isPropagateTerminator()
    {
        return propagateTerminator;
    }

    public void setPropagateTerminator(boolean propagateTerminator)
    {
        this.propagateTerminator = propagateTerminator;
    }

//    public static final String CONTEXT_PROP_MODE = "com.arjuna.ats.jts.contextPropMode";
    public String getContextPropMode()
    {
        return contextPropMode;
    }

    public void setContextPropMode(String contextPropMode)
    {
        this.contextPropMode = contextPropMode;
    }

//    public static final String RECOVERY_MANAGER_ORB_PORT = "com.arjuna.ats.jts.recoveryManagerPort";
    public int getRecoveryManagerPort()
    {
        return recoveryManagerPort;
    }

    public void setRecoveryManagerPort(int recoveryManagerPort)
    {
        this.recoveryManagerPort = recoveryManagerPort;
    }

//    public static final String RECOVERY_MANAGER_ADDRESS = "com.arjuna.ats.jts.recoveryManagerAddress";
    public String getRecoveryManagerAddress()
    {
        return recoveryManagerAddress;
    }

    public void setRecoveryManagerAddress(String recoveryManagerAddress)
    {
        this.recoveryManagerAddress = recoveryManagerAddress;
    }

    public void setRecoveryManagerInetAddress(InetAddress inetAddress) {
        setRecoveryManagerAddress(inetAddress.getHostAddress());
    }

//    public static final String OTS_1_0_TIMEOUT_PROPAGATION = "com.arjuna.ats.jts.ots_1_0.timeoutPropagation";
    public boolean isTimeoutPropagation()
    {
        return timeoutPropagation;
    }

    public void setTimeoutPropagation(boolean timeoutPropagation)
    {
        this.timeoutPropagation = timeoutPropagation;
    }
}
