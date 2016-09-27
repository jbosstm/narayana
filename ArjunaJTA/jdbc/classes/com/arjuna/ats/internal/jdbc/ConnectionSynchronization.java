/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ConnectionSynchronization.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jdbc;

import com.arjuna.ats.jdbc.logging.jdbcLogger;

import javax.transaction.Synchronization;

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

