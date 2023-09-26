/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.recovery.jts;

/**
 * Designed to be able to recover any XAResource.
 */

public class XARecoveryModule extends
		com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule
{

	public XARecoveryModule ()
	{
		super(new com.arjuna.ats.internal.jta.recovery.jts.XARecoveryResourceManagerImple(),
                "JTS XARecoveryModule");

		com.arjuna.ats.internal.jta.Implementationsx.initialise();
	}

}