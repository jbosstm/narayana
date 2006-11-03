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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XAConnectionRecovery.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.recovery;

import java.sql.*;
import javax.sql.*;
import javax.transaction.*;
import javax.transaction.xa.*;

/**
 * To perform recovery on arbitrary connections we may need
 * to recreate those connections. Users can provide implementations
 * of this interface which we will use at recovery time to re-create
 * db connections and from them perform recovery.
 *
 * @deprecated As of ATS 3.3, replaced by @link XAResourceRecovery
 *
 * @since JTS 2.1.
 */

public interface XAConnectionRecovery
{

    /**
     * Get a connection to use for recovery purposes.
     *
     * @return a new XAConnection.
     */

    public XAConnection getConnection () throws SQLException;

    /**
     * Initialise with all properties required to create a connection.
     *
     * @param String p An arbitrary string from which initialization data
     * is obtained.
     *
     * @return <code>true</code> if initialization happened successfully,
     * <code>false</code> otherwise.
     */

    public boolean initialise (String p) throws SQLException;

    /**
     * Iterate through all of the connections this instance provides
     * access to.
     *
     * @return <code>true</code> if this instance can provide more
     * connections, <code>false</code> otherwise.
     */
 
    public boolean hasMoreConnections ();
 
}

