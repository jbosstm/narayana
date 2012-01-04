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
		Integer eisName = 1;
		arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");

		XidImple rootXid = new XidImple(uid, branch, eisName);
		{
			assertEquals(XATxConverter.getNodeName(rootXid.getXID()), "1");
			assertEquals(XATxConverter.getEISName(rootXid.getXID()), eisName);
			assertEquals(XATxConverter.getSubordinateNodeName(rootXid.getXID()), null);
		}

		// TxControl.setXANodeName(2);
		XATxConverter.setSubordinateNodeName(rootXid.getXID(), "1");
		XidImple subordinateXid = new XidImple(rootXid);
		{
			assertEquals(XATxConverter.getNodeName(subordinateXid.getXID()), "1");
			assertEquals(XATxConverter.getEISName(subordinateXid.getXID()), eisName);
			assertEquals(XATxConverter.getSubordinateNodeName(subordinateXid.getXID()), "1");
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
