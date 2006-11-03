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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: XAModifier.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.xa;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;

import java.sql.*;
import javax.transaction.xa.Xid;
import javax.sql.*;
import javax.transaction.*;
import javax.transaction.xa.*;

import com.arjuna.ats.jta.exceptions.NotImplementedException;

/**
 * Instances of this class enable us to work around problems
 * in certain databases (specifically Oracle).
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: XAModifier.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.2.4.
 */

public interface XAModifier
{

    /**
     * Given an Arjuna xid, create a local driver representation. Some
     * drivers expect their own implementations to be used, despite the
     * fact that the JTA does not mention this!
     */

    public Xid createXid (XidImple xid) throws SQLException, NotImplementedException;

    /**
     * Return the xa_start parameters for this level.
     */

    public int xaStartParameters (int level) throws SQLException, NotImplementedException;
    
}

