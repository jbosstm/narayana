/*
 * Copyright The Narayana Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.jta.xa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.transaction.xa.Xid;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.CoreEnvironmentBeanException;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

public class XATxConverterTest {

	@Test
	public void testXAConverter() throws CoreEnvironmentBeanException {
		Uid uid = new Uid();
		boolean branch = true;
		Integer eisName = 97;
		int nodeNameLength = 28;
		char[] nodeName = new char[nodeNameLength];
		for (int i = 0; i < nodeNameLength; i++) {
			nodeName[i] = '.';
		}
		String nodeName1 = new String(nodeName);
		String nodeName2 = nodeName1;
		arjPropertyManager.getCoreEnvironmentBean()
				.setNodeIdentifier(nodeName1);

		XidImple rootXid = new XidImple(uid, branch, eisName);
		{
			assertTrue(rootXid.getGlobalTransactionId().length <= Xid.MAXGTRIDSIZE);
			assertTrue(rootXid.getBranchQualifier().length <= Xid.MAXBQUALSIZE);
			assertEquals(XATxConverter.getNodeName(rootXid.getXID()), nodeName1);
			assertEquals(XATxConverter.getNodeName(rootXid.getXID()).length(),
					nodeNameLength);
			assertEquals(XATxConverter.getEISName(rootXid.getXID()), eisName);
			assertEquals(
					XATxConverter.getSubordinateNodeName(rootXid.getXID()),
					null);
		}

		// TxControl.setXANodeName(2);
		XATxConverter.setSubordinateNodeName(rootXid.getXID(), nodeName2);
		XidImple subordinateXid = new XidImple(rootXid);
		{
			assertTrue(subordinateXid.getGlobalTransactionId().length <= Xid.MAXGTRIDSIZE);
			assertTrue(
					"Bquallength: "
							+ subordinateXid.getBranchQualifier().length,
					subordinateXid.getBranchQualifier().length <= Xid.MAXBQUALSIZE);
			assertEquals(XATxConverter.getNodeName(subordinateXid.getXID()),
					nodeName1);
			assertEquals(XATxConverter.getNodeName(subordinateXid.getXID())
					.length(), nodeNameLength);
			assertEquals(XATxConverter.getEISName(subordinateXid.getXID()),
					eisName);
			assertEquals(XATxConverter.getSubordinateNodeName(subordinateXid
					.getXID()), nodeName2);
			assertEquals(
					XATxConverter.getSubordinateNodeName(
							subordinateXid.getXID()).length(), nodeNameLength);
		}
	}

	@Test
	public void testForeignXID() {
		XidImple foreignXidImple = new XidImple(new MyForeignXID());

		assertEquals(XATxConverter.getNodeName(foreignXidImple.getXID()), null);
		assertTrue(XATxConverter.getEISName(foreignXidImple.getXID()) == -1);
		assertEquals(XATxConverter.getSubordinateNodeName(foreignXidImple.getXID()), null);
	}

	private class MyForeignXID implements Xid {

		@Override
		public int getFormatId() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public byte[] getGlobalTransactionId() {
			return "foo".getBytes();
		}

		@Override
		public byte[] getBranchQualifier() {
			return "bar".getBytes();
		}

	}
}
