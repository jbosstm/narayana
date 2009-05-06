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
 * (C) 2006, 2009
 * @author JBoss Inc.
 *
 * $Id$
 */

package com.arjuna.ats.internal.jdbc;

import java.sql.*;
import java.util.Properties;

/**
 * JDBC 3.0 extention to the Connection wrapper.
 * Since ConnectionImple now implements all the JDBC3 functions we don't
 * have much to do. This class only really exists to add the
 * 'implements Connection' clause.
 */
public class ConnectionImpleJDBC3 extends ConnectionImple implements Connection
{
	public ConnectionImpleJDBC3(String dbName, Properties info) throws SQLException {
		super(dbName, info);
	}
}
