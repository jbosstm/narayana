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
package com.arjuna.ats.arjuna.common;

import com.arjuna.common.internal.util.propertyservice.PropertyPrefix;
import com.arjuna.common.internal.util.propertyservice.FullPropertyName;
import com.arjuna.common.internal.util.propertyservice.ConcatenationPrefix;
import com.arjuna.ats.internal.arjuna.recovery.PeriodicRecovery;
import com.arjuna.ats.arjuna.utils.Utility;

import java.net.InetAddress;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * A JavaBean containing configuration properties for the recovery system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
@PropertyPrefix(prefix = "com.arjuna.ats.arjuna.recovery.")
public class RecoveryEnvironmentBean implements RecoveryEnvironmentBeanMBean
{
    private int periodicRecoveryPeriod = PeriodicRecovery._defaultRecoveryPeriod;
    private int recoveryBackoffPeriod = PeriodicRecovery._defaultBackoffPeriod;
    private boolean recoveryListener = true;
    private int recoveryPort = 0;
    private String recoveryAddress = "localhost";
    private int transactionStatusManagerPort = 0;
    private String transactionStatusManagerAddress = "localhost";

    private int expiryScanInterval = 12; // hours
    private int transactionStatusManagerExpiryTime = 12; // hours

    @ConcatenationPrefix(prefix = "com.arjuna.ats.arjuna.recovery.expiryScanner")
    private List<String> expiryScanners = Collections.emptyList();

    @ConcatenationPrefix(prefix = "com.arjuna.ats.arjuna.recovery.recoveryExtension")
    private List<String> recoveryExtensions = Collections.emptyList();

    @ConcatenationPrefix(prefix = "com.arjuna.ats.arjuna.recovery.recoveryActivator")
    private List<String> recoveryActivators = Collections.emptyList();

    @FullPropertyName(name = "com.arjuna.ats.internal.arjuna.recovery.listener.timeoutsocket")
    private boolean timeoutSocket = false;

    //    public static final String PERIODIC_RECOVERY_PERIOD = "com.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod" ;
    public int getPeriodicRecoveryPeriod()
    {
        return periodicRecoveryPeriod;
    }

    public void setPeriodicRecoveryPeriod(int periodicRecoveryPeriod)
    {
        this.periodicRecoveryPeriod = periodicRecoveryPeriod;
    }

//    public static final String RECOVERY_BACKOFF_PERIOD = "com.arjuna.ats.arjuna.recovery.recoveryBackoffPeriod" ;
    public int getRecoveryBackoffPeriod()
    {
        return recoveryBackoffPeriod;
    }

    public void setRecoveryBackoffPeriod(int recoveryBackoffPeriod)
    {
        this.recoveryBackoffPeriod = recoveryBackoffPeriod;
    }

//    public static final String RECOVERY_MANAGER_LISTENER = "com.arjuna.ats.arjuna.recovery.recoveryListener";
    public boolean isRecoveryListener()
    {
        return recoveryListener;
    }

    public void setRecoveryListener(boolean recoveryListener)
    {
        this.recoveryListener = recoveryListener;
    }

//    public static final String RECOVERY_MANAGER_PORT = "com.arjuna.ats.arjuna.recovery.recoveryPort";
    public int getRecoveryPort()
    {
        return recoveryPort;
    }

    public void setRecoveryPort(int recoveryPort)
    {
        Utility.validatePortRange(recoveryPort);
        this.recoveryPort = recoveryPort;
    }

//    public static final String RECOVERY_MANAGER_ADDRESS = "com.arjuna.ats.arjuna.recovery.recoveryAddress";
    public String getRecoveryAddress()
    {
        return recoveryAddress;
    }

    public void setRecoveryAddress(String recoveryAddress)
    {
        this.recoveryAddress = recoveryAddress;
    }

    public void setRecoveryInetAddress(InetAddress inetAddress) {
        setRecoveryAddress(inetAddress.getHostAddress());
    }

//    public static final String TRANSACTION_STATUS_MANAGER_PORT = "com.arjuna.ats.arjuna.recovery.transactionStatusManagerPort";
    public int getTransactionStatusManagerPort()
    {
        return transactionStatusManagerPort;
    }

    public void setTransactionStatusManagerPort(int transactionStatusManagerPort)
    {
        Utility.validatePortRange(transactionStatusManagerPort);
        this.transactionStatusManagerPort = transactionStatusManagerPort;
    }

//    public static final String TRANSACTION_STATUS_MANAGER_ADDRESS = "com.arjuna.ats.arjuna.recovery.transactionStatusManagerAddress";
    public String getTransactionStatusManagerAddress()
    {
        return transactionStatusManagerAddress;
    }

    public void setTransactionStatusManagerAddress(String transactionStatusManagerAddress)
    {
        this.transactionStatusManagerAddress = transactionStatusManagerAddress;
    }

    public void setTransactionStatusManagerInetAddress(InetAddress inetAddress) {
        setTransactionStatusManagerAddress(inetAddress.getHostAddress());
    }

//    public static final String EXPIRY_SCAN_INTERVAL    = "com.arjuna.ats.arjuna.recovery.expiryScanInterval";
    public int getExpiryScanInterval()
    {
        return expiryScanInterval;
    }

    public void setExpiryScanInterval(int expiryScanInterval)
    {
        this.expiryScanInterval = expiryScanInterval;
    }

//    public static final String TRANSACTION_STATUS_MANAGER_EXPIRY_TIME = "com.arjuna.ats.arjuna.recovery.transactionStatusManagerExpiryTime";
    public int getTransactionStatusManagerExpiryTime()
    {
        return transactionStatusManagerExpiryTime;
    }

    public void setTransactionStatusManagerExpiryTime(int transactionStatusManagerExpiryTime)
    {
        this.transactionStatusManagerExpiryTime = transactionStatusManagerExpiryTime;
    }

    public List<String> getExpiryScanners()
    {
        if(expiryScanners == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<String>(expiryScanners);
        }
    }

    public void setExpiryScanners(List<String> expiryScanners)
    {
        this.expiryScanners = new ArrayList<String>(expiryScanners);
    }

    public List<String> getRecoveryExtensions()
    {
        if(recoveryExtensions == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<String>(recoveryExtensions);
        }
    }

    public void setRecoveryExtensions(List<String> recoveryExtensions)
    {
        this.recoveryExtensions = new ArrayList<String>(recoveryExtensions);
    }

    public List<String> getRecoveryActivators()
    {
        if(recoveryActivators == null) {
            return Collections.emptyList();
        } else {
            return new ArrayList<String>(recoveryActivators);
        }
    }

    public void setRecoveryActivators(List<String> recoveryActivators)
    {
        this.recoveryActivators = new ArrayList<String>(recoveryActivators);
    }

    public boolean isTimeoutSocket()
    {
        return timeoutSocket;
    }

    public void setTimeoutSocket(boolean timeoutSocket)
    {
        this.timeoutSocket = timeoutSocket;
    }
}
