/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package org.jboss.jbossts.qa.CrashRecovery13Impls;

import com.arjuna.ats.jta.recovery.XAResourceRecovery;

import javax.transaction.xa.XAResource;
import java.sql.SQLException;

public class DummyXARecoveryResource implements XAResourceRecovery
{

	public XAResource getXAResource() throws SQLException
	{
		count++;

		return new RecoveryXAResource();
	}

	/**
	 * Initialise with all properties required to create the resource(s).
	 *
	 * @param String p An arbitrary string from which initialization data
	 *               is obtained.
	 * @return <code>true</code> if initialization happened successfully,
	 *         <code>false</code> otherwise.
	 */

	public boolean initialise(String p) throws SQLException
	{
		return true;
	}

	/**
	 * Iterate through all of the resources this instance provides
	 * access to.
	 *
	 * @return <code>true</code> if this instance can provide more
	 *         resources, <code>false</code> otherwise.
	 */

	public boolean hasMoreResources()
	{
		if (count <= 1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private int count = 0;

}