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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoverableXAConnection.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.xa;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import java.sql.*;
import javax.sql.*;
import javax.transaction.*;
import javax.transaction.xa.*;

import com.arjuna.ats.jta.exceptions.NotImplementedException;

/**
 * XA connection implementations must provide an implementation
 * of this class.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: RecoverableXAConnection.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

// check

public interface RecoverableXAConnection
{

    public static final int AUTO_RECOVERY = 0;
    public static final int OBJECT_RECOVERY = 1;

    public boolean packInto (OutputObjectState os);
    public boolean unpackFrom (InputObjectState os);

    public XAResource getResource () throws SQLException;
    public XAConnection getConnection () throws SQLException;
    public XAConnection getCurrentConnection () throws SQLException;
    public XADataSource getDataSource () throws SQLException;
    void closeCloseCurrentConnection() throws SQLException;

    /**
     * @return true if the connection is being used within a transaction,
     * false otherwise.
     */

    public boolean inuse ();

    /**
     * @since JTS 2.2.
     */
	   
    public void reset ();  // reset the connection for new useage

    /**
     * @since JTS 2.2.
     */

    public void close ();  // close the connection and make available for gc
 
    /**
     * Remember the transaction we are being used within.
     *
     * @since JTS 2.2.
     */

    public boolean setTransaction (javax.transaction.Transaction tx);

    /**
     * Is this the same transaction?
     *
     * @return true if the connection can be used by this
     * transaction, false otherwise.
     * @since JTS 2.2.
     */

    public boolean validTransaction (javax.transaction.Transaction tx);
 
}

