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
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DummyXARecoveryResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.recovery;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.ats.jta.recovery.*;
import com.arjuna.ats.jta.xa.*;

import com.arjuna.ats.internal.jta.utils.*;

import com.hp.mwtests.ts.jta.common.*;

import java.util.Vector;

import java.sql.*;

import javax.sql.*;
import javax.transaction.*;
import javax.transaction.xa.*;

public class DummyXARecoveryResource implements XAResourceRecovery
{

    public XAResource getXAResource () throws SQLException
    {
	count++;
	
	return new RecoveryXAResource();
    }

    /**
     * Initialise with all properties required to create the resource(s).
     *
     * @param String p An arbitrary string from which initialization data
     * is obtained.
     *
     * @return <code>true</code> if initialization happened successfully,
     * <code>false</code> otherwise.
     */

    public boolean initialise (String p) throws SQLException
    {
	return true;
    }

    /**
     * Iterate through all of the resources this instance provides
     * access to.
     *
     * @return <code>true</code> if this instance can provide more
     * resources, <code>false</code> otherwise.
     */
 
    public boolean hasMoreResources ()
    {
	if (count <= 1)
	    return true;
	else
	    return false;
    }

    private int count = 0;
    
}
