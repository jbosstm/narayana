/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.utils.jts;

/**
 * Some useful utility routines.
 */

public class StatusConverter
{

	public static int convert(org.omg.CosTransactions.Status status)
	{
		switch (status.value())
		{
		case org.omg.CosTransactions.Status._StatusActive:
			return jakarta.transaction.Status.STATUS_ACTIVE;
		case org.omg.CosTransactions.Status._StatusCommitting:
			return jakarta.transaction.Status.STATUS_COMMITTING;
		case org.omg.CosTransactions.Status._StatusRollingBack:
			return jakarta.transaction.Status.STATUS_ROLLING_BACK;
		case org.omg.CosTransactions.Status._StatusPreparing:
			return jakarta.transaction.Status.STATUS_PREPARING;
		case org.omg.CosTransactions.Status._StatusCommitted:
			return jakarta.transaction.Status.STATUS_COMMITTED;
		case org.omg.CosTransactions.Status._StatusMarkedRollback:
			return jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
		case org.omg.CosTransactions.Status._StatusNoTransaction:
			return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		case org.omg.CosTransactions.Status._StatusPrepared:
			return jakarta.transaction.Status.STATUS_PREPARED;
		case org.omg.CosTransactions.Status._StatusRolledBack:
			return jakarta.transaction.Status.STATUS_ROLLEDBACK;
		case org.omg.CosTransactions.Status._StatusUnknown:
		default:
			return jakarta.transaction.Status.STATUS_UNKNOWN;
		}
	}

}