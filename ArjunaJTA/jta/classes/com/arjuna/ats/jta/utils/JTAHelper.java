/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jta.utils;

/**
 * Some useful utility routines.
 */

public class JTAHelper
{

    public static String stringForm (int status)
    {
	switch (status)
	{
	case jakarta.transaction.Status.STATUS_ACTIVE:
	    return "jakarta.transaction.Status.STATUS_ACTIVE";
	case jakarta.transaction.Status.STATUS_COMMITTED:
	    return "jakarta.transaction.Status.STATUS_COMMITTED";
	case jakarta.transaction.Status.STATUS_MARKED_ROLLBACK:
	    return "jakarta.transaction.Status.STATUS_MARKED_ROLLBACK";
	case jakarta.transaction.Status.STATUS_NO_TRANSACTION:
	    return "jakarta.transaction.Status.STATUS_NO_TRANSACTION";
	case jakarta.transaction.Status.STATUS_PREPARED:
	    return "jakarta.transaction.Status.STATUS_PREPARED";
	case jakarta.transaction.Status.STATUS_PREPARING:
            return "jakarta.transaction.Status.STATUS_PREPARING";
	case jakarta.transaction.Status.STATUS_ROLLEDBACK:
	    return "jakarta.transaction.Status.STATUS_ROLLEDBACK";
	case jakarta.transaction.Status.STATUS_ROLLING_BACK:
            return "jakarta.transaction.Status.STATUS_ROLLING_BACK";
	case jakarta.transaction.Status.STATUS_UNKNOWN:
	default:
	    return "jakarta.transaction.Status.STATUS_UNKNOWN";
	}
    }

}