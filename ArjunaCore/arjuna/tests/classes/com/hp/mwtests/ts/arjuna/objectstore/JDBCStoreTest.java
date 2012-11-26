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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

public class JDBCStoreTest {
	// public static void setUpClass() throws Exception {
	// com.microsoft.sqlserver.jdbc.SQLServerDataSource ds = new
	// com.microsoft.sqlserver.jdbc.SQLServerDataSource();
	// ds.setPortNumber(5000);
	// ds.setServerName("localhost");
	// ds.setUser("");
	// ds.setPassword("");
	// }

	@Test
	public void testStateMachine() throws SQLException, ObjectStoreException,
			Exception {
		ObjectStoreEnvironmentBean jdbcStoreEnvironmentBean = BeanPopulator
				.getDefaultInstance(ObjectStoreEnvironmentBean.class);

		ObjectStoreAPI api = new JDBCStore(jdbcStoreEnvironmentBean);

		InputObjectState states = new InputObjectState();
		api.allObjUids("typeName", states);
		Uid unpacked = UidHelper.unpackFrom(states);
		if (unpacked.notEquals(Uid.nullUid())) {
			fail("Did not expect uids to start with");
		}

		Uid uid = new Uid();

		assertTrue(api.read_committed(uid, "typeName") == null);

		assertTrue(api.write_uncommitted(uid, "typeName",
				new OutputObjectState()));

		assertTrue(api.commit_state(uid, "typeName"));

		assertTrue(api.read_committed(uid, "typeName") != null);

		assertFalse(api.commit_state(uid, "typeName"));

		assertTrue(api.hide_state(uid, "typeName"));

		assertTrue(api.reveal_state(uid, "typeName"));

		byte[] buff = new byte[10496000 + 1];
		OutputObjectState outputObjectState = new OutputObjectState(new Uid(),
				"tName");
		outputObjectState.packBytes(buff);
		assertFalse(api.write_uncommitted(uid, "typeName", outputObjectState));

		String toTest = "Hello - this is a test";
		buff = new String(toTest).getBytes();
		outputObjectState = new OutputObjectState();
		outputObjectState.packBytes(buff);
		assertTrue(api.write_committed(uid, "typeName", outputObjectState));

		InputObjectState read_state = api.read_committed(uid, "typeName");
		assertTrue(new String(read_state.unpackBytes()).equals(toTest));
		assertTrue(read_state.type(), read_state.type().equals("typeName"));

		states = new InputObjectState();
		api.allObjUids("typeName", states);
		boolean foundUid = false;
		do {
			Uid uidFound = UidHelper.unpackFrom(states);
			if (uidFound.notEquals(Uid.nullUid())) {
				assertTrue(uidFound.equals(uid));
				foundUid = true;
			} else {
				if (!foundUid) {
					fail("Did not find the UID");
				}
				break;
			}
		} while (true);

		assertFalse(api.remove_uncommitted(uid, "typeName"));

		assertTrue(api.remove_committed(uid, "typeName"));

		assertFalse(api.remove_committed(uid, "typeName"));
		api.stop();
		// }
	}
}
