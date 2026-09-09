/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.common;

import java.util.List;

/**
 * A JMX MBean interface containing configuration for the recovery system.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 */
public interface RecoveryEnvironmentBeanMBean
{
    /**
     * Returns the interval between recovery scans, in seconds.
     * <p>
     * Default: 120 seconds
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.periodicRecoveryPeriod
     *
     * @return the recovery scan period, in seconds.
     */
    int getPeriodicRecoveryPeriod();

    /**
     * Returns the interval between recovery phases within a recovery scan, in seconds.
     * <p>
     * Default: 10 seconds
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.recoveryBackoffPeriod
     *
     * @return the interval between recovery phases, in seconds.
     */
    int getRecoveryBackoffPeriod();

    /**
     * Returns true if the recovery system should listen on a network socket.
     * <p>
     * Default: true
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.recoveryListener
     *
     * @return true for network recovery, false for local JVM only use.
     */
    boolean isRecoveryListener();

    /**
     * Returns the network port number on which the recovery listener should bind.
     * <p>
     * Default: 0
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.recoveryPort
     *
     * @return the port number for recovery listener.
     */
    int getRecoveryPort();

    /**
     * Returns the hostname on which the recovery listener should bind.
     * <p>
     * Default: "localhost"
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.recoveryAddress
     *
     * @return the hostname on which the recovery system will listen.
     */
    String getRecoveryAddress();

    /**
     * Returns the network port number on which the transaction status manager should bind.
     * <p>
     * Default: 0
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.transactionStatusManagerPort
     *
     * @return the port number for the transaction status manager listener.
     */
    int getTransactionStatusManagerPort();

    /**
     * Returns the hostname on which the transaction status manager should bind.
     * <p>
     * Default: "localhost"
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.transactionStatusManagerAddress
     *
     * @return the hostname on which the transaction status manager will listen.
     */
    String getTransactionStatusManagerAddress();

    /**
     * Returns the interval on which the ObjectStore will be scanned for expired items, in hours.
     * <p>
     * Default: 12 hours
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.expiryScanInterval
     *
     * @return the interval between ObjectStore expiry checks, in hours.
     */
    int getExpiryScanInterval();

    /**
     * Returns the time period after which items will be considered to have expired, in hours.
     * <p>
     * Default: 12 hours
     * Equivalent deprecated property: com.arjuna.ats.arjuna.recovery.transactionStatusManagerExpiryTime
     *
     * @return the expiry timeout, in hours.
     */
    int getTransactionStatusManagerExpiryTime();

    /**
     * Returns a list of names of classes that implement ExpiryScanner.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * Default: empty list.
     * Equivalent deprecated property prefix: com.arjuna.ats.arjuna.recovery.expiryScanner
     *
     * @return a list of ExpiryScanner implementation class names.
     */
    List<String> getExpiryScannerClassNames();

    /**
     * Returns a list of names of classes that implement RecoveryModule.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * Default: empty list.
     * Equivalent deprecated property prefix:
     *
     * @return a list of RecoveryModule implementation class names.
     */
    List<String> getRecoveryModuleClassNames();

    /**
     * Returns a list of names of classes that implement RecoveryActivator.
     * The returned list is a copy. May return an empty list, will not return null.
     * <p>
     * Default: empty list.
     * Equivalent deprecated property prefix:
     *
     * @return a list of RecoveryActivator implementation class names.
     */
    List<String> getRecoveryActivatorClassNames();

    /**
     * Returns true if SO_TIMEOUT should be set on Listener socket instances.
     * <p>
     * Default: false
     * Equivalent deprecated property: com.arjuna.ats.internal.arjuna.recovery.listener.timeoutsocket
     *
     * @return true if SO_TIMEOUT should be used, false if not.
     */
    boolean isTimeoutSocket();

    /**
     * <p>This method gives information about the behaviour of
     * {@link com.arjuna.ats.arjuna.recovery.RecoveryManager} when suspending.
     * <p>The current list of Narayana-provided RecoveryModules that support this feature is:
     * <ul>
     *    <li>com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule</li>
     * </ul>
     *
     * @return true if {@link com.arjuna.ats.arjuna.recovery.RecoveryManager} should wait that all
     * {@link com.arjuna.ats.arjuna.recovery.RecoveryModule} overriding {@link com.arjuna.ats.arjuna.recovery.RecoveryModule#hasWorkLeftToDo()} recover all their
     * transactions before shutting down; false otherwise.
     */
    @Deprecated(forRemoval = true)
    boolean isWaitForWorkLeftToDo();
}