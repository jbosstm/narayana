/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.recovery;

import com.arjuna.ats.arjuna.AtomicAction;

/**
 * This class is a plug-in module for the recovery manager. This class is
 * responsible for the removing transaction logs that are too
 * old.
 */

public class AtomicActionExpiryScanner extends ExpiredTransactionScanner
{

	public AtomicActionExpiryScanner()
	{
		super(_transactionType, _transactionType + "/Expired");
	}

	// 'type' within the Object Store for AtomicActions.
	private static final String _transactionType = new AtomicAction().type();

}