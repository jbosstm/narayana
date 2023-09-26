/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts.recovery ;

/**
 * Container for property names used by recovery
 * @deprecated use JTSEnvironmentBean instead
 */
@Deprecated
public class RecoveryEnvironment
{
    public static final String OTS_ISSUE_RECOVERY_ROLLBACK  = "com.arjuna.ats.jts.recovery.issueRecoveryRollback" ;
    public static final String COMMITTED_TRANSACTION_RETRY_LIMIT = "com.arjuna.ats.jts.recovery.commitTransactionRetryLimit";
    public static final String ASSUMED_OBJECT_NOT_EXIST = "com.arjuna.ats.jts.recovery.assumedObjectNotExist" ;
}