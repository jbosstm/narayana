/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.jdbc.logging.jdbcLogger;

import jakarta.transaction.Synchronization;

/**
 * A synchronization to close the database connection when the transaction
 * has committed or rolled back.
 *
 * @version $Id: ConnectionSynchronization.java 2342 2006-03-30 13:06:17Z  $
 */

public class ConnectionSynchronization implements Synchronization
{

	public ConnectionSynchronization (ConnectionImple conn)
    {
	_theConnection = conn;
	_theConnection.incrementUseCount();
    }

    public void afterCompletion(int status)
    {
		try
		{
			if (_theConnection != null) {
				_theConnection.closeImpl();
			}
		}
		catch (Exception ex)
		{
			jdbcLogger.i18NLogger.warn_not_closed(ex);
		}
    }

    public void beforeCompletion()
    {
    }

    private ConnectionImple _theConnection = null;
}