/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreAPI;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.objectstore.jdbc.JDBCStore;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import java.sql.SQLException;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test that the store can be configured via a DataSource instead of a Database URL
 */
public class JDBCStoreDSTest {
	@BeforeEach
	public void before() {
		JdbcDataSource ds = new JdbcDataSource();

		ds.setURL("jdbc:h2:./JBTMDB"); // ends up under the target dir
		ds.setUser("sa");
		ds.setPassword("sa");

		ObjectStoreEnvironmentBean jdbcStoreEnvironmentBean = BeanPopulator
				.getDefaultInstance(ObjectStoreEnvironmentBean.class);

		jdbcStoreEnvironmentBean.setJdbcDataSource(ds);
		jdbcStoreEnvironmentBean.setDropTable(true);
		jdbcStoreEnvironmentBean.setCreateTable(true);
	}

	@AfterEach
	public void after() {
		ObjectStoreEnvironmentBean jdbcStoreEnvironmentBean = BeanPopulator
				.getDefaultInstance(ObjectStoreEnvironmentBean.class);

		jdbcStoreEnvironmentBean.setJdbcDataSource(null);
	}

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

		assertNull(api.read_committed(uid, "typeName"));

		assertTrue(api.write_uncommitted(uid, "typeName",
				new OutputObjectState()));

		assertTrue(api.commit_state(uid, "typeName"));

		assertNotNull(api.read_committed(uid, "typeName"));

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
		assertEquals(new String(read_state.unpackBytes()), toTest);
		assertEquals(read_state.type(), "typeName", read_state.type());

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
	}
}