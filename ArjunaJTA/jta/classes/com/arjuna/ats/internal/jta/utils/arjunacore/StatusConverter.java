/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.utils.arjunacore;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;

public class StatusConverter
{

	public static int convert (int status)
	{
		switch (status)
		{
		case ActionStatus.RUNNING:
			return jakarta.transaction.Status.STATUS_ACTIVE;
		case ActionStatus.COMMITTED:
		case ActionStatus.H_COMMIT:
		case ActionStatus.H_HAZARD:  // not exactly true, but ...
		case ActionStatus.H_MIXED:
			return jakarta.transaction.Status.STATUS_COMMITTED;
		case ActionStatus.COMMITTING:
			return jakarta.transaction.Status.STATUS_COMMITTING;
		case ActionStatus.ABORT_ONLY:
			return jakarta.transaction.Status.STATUS_MARKED_ROLLBACK;
		case ActionStatus.NO_ACTION:
			return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
		case ActionStatus.PREPARED:
			return jakarta.transaction.Status.STATUS_PREPARED;
		case ActionStatus.ABORTED:
		case ActionStatus.H_ROLLBACK:
			return jakarta.transaction.Status.STATUS_ROLLEDBACK;
		case ActionStatus.INVALID:
			return jakarta.transaction.Status.STATUS_UNKNOWN;
		case ActionStatus.ABORTING:
			return jakarta.transaction.Status.STATUS_ROLLING_BACK;
		case ActionStatus.PREPARING:
			return jakarta.transaction.Status.STATUS_PREPARING;
		default:
			return jakarta.transaction.Status.STATUS_UNKNOWN;
		}
	}

}