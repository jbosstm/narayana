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
package com.hp.mwtests.ts.arjuna.objectstore;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;

import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.objectstore.jdbc.JDBCAccess;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCActionStore;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStoreEnvironmentBean;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.accessors.accessor;

public class JDBCStoreTestPostGres {

	@BeforeClass
	public static void setUpClass() throws Exception {
		// Create initial context
		InitialContext ic = new InitialContext();

		ic.createSubcontext("java:");
		ic.createSubcontext("java:/comp");
		ic.createSubcontext("java:/comp/env");
		ic.createSubcontext("java:/comp/env/jdbc");

		// Construct DataSource
		PGSimpleDataSource ds = new org.postgresql.ds.PGSimpleDataSource();
		ds.setDatabaseName("JBTMDB");
		ds.setUser("sa");
		ds.setPassword("sa");
		ds.setServerName("localhost");
		ds.setPortNumber(5432);

		ic.bind("java:/comp/env/jdbc/JDBCObjectStoreDS", ds);
	}

	@Test
	public void testJDBCImple() throws ObjectStoreException, IOException {
		JDBCStoreEnvironmentBean jdbcStoreEnvironmentBean = new JDBCStoreEnvironmentBean();
		Object[] params = new Object[3];
		params[JDBCAccess.URL] = null;
		params[JDBCAccess.DROP_TABLE] = new Long(1);
		params[JDBCAccess.TABLE_NAME] = null;
		accessor a = new accessor();
		a.initialise(params);
		jdbcStoreEnvironmentBean.setJdbcUserDbAccess(a);
		ObjectStoreAPI api = new JDBCActionStore(jdbcStoreEnvironmentBean);
		OutputObjectState fluff = new OutputObjectState();
		Uid kungfuTx = new Uid();
		UidHelper.packInto(kungfuTx, fluff);
		api.write_committed(new Uid(), "foo", fluff);
		InputObjectState states = new InputObjectState();
		api.allObjUids("foo", states);

		do {
			Uid uid = UidHelper.unpackFrom(states);

			if (uid.notEquals(Uid.nullUid())) {
				System.out.println(uid);
			} else {
				break;
			}

		} while (true);

		api.stop();
	}
}
