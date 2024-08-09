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
    int getPeriodicRecoveryPeriod();

    int getRecoveryBackoffPeriod();

    boolean isRecoveryListener();

    int getRecoveryPort();

    String getRecoveryAddress();

    int getTransactionStatusManagerPort();

    String getTransactionStatusManagerAddress();

    int getExpiryScanInterval();

    int getTransactionStatusManagerExpiryTime();

    List<String> getExpiryScannerClassNames();

    List<String> getRecoveryModuleClassNames();

    List<String> getRecoveryActivatorClassNames();

    boolean isTimeoutSocket();

    boolean isWaitForRecovery();
}