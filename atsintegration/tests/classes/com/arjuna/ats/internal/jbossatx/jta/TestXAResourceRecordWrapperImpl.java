package com.arjuna.ats.internal.jbossatx.jta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.XAResourceWrapper;
import org.junit.Test;

import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

public class TestXAResourceRecordWrapperImpl {

	@Test
	public void testReadAndWriteEISNameSameKey() throws IOException, ObjectStoreException {
		String xaNodeName = TxControl.getXANodeName();
		TxControl.setXANodeName("1");
		XAResourceRecordWrappingPluginImpl xaResourceRecordWrappingPluginImpl = new XAResourceRecordWrappingPluginImpl();
		MyWrapper myWrapper = new MyWrapper("Simple");
		Integer eisNameKey = xaResourceRecordWrappingPluginImpl.getEISName(myWrapper);

		MyWrapper otherMyWrapper = new MyWrapper("Simple");
		Integer shouldBeSameEisNameKey = xaResourceRecordWrappingPluginImpl.getEISName(otherMyWrapper);

		System.out.println(eisNameKey + " == " + shouldBeSameEisNameKey + "?");
		assertEquals(eisNameKey, shouldBeSameEisNameKey);
		assertEquals("Simple", xaResourceRecordWrappingPluginImpl.getEISName(eisNameKey));
		assertEquals("Simple", xaResourceRecordWrappingPluginImpl.getEISName(shouldBeSameEisNameKey));
		TxControl.setXANodeName(xaNodeName);
	}

	@Test
	public void testReadAndWriteEISNameDifferentKey() throws IOException, ObjectStoreException {
		String xaNodeName = TxControl.getXANodeName();
		TxControl.setXANodeName("1");
		XAResourceRecordWrappingPluginImpl xaResourceRecordWrappingPluginImpl = new XAResourceRecordWrappingPluginImpl();
		MyWrapper myWrapper = new MyWrapper("Simple1");
		Integer eisNameKey = xaResourceRecordWrappingPluginImpl.getEISName(myWrapper);

		MyWrapper otherMyWrapper = new MyWrapper("Simple2");
		Integer shouldBeSameEisNameKey = xaResourceRecordWrappingPluginImpl.getEISName(otherMyWrapper);

		System.out.println(eisNameKey + " == " + shouldBeSameEisNameKey + "?");
		assertFalse(eisNameKey.equals(shouldBeSameEisNameKey));
		assertEquals("Simple1", xaResourceRecordWrappingPluginImpl.getEISName(eisNameKey));
		assertEquals("Simple2", xaResourceRecordWrappingPluginImpl.getEISName(shouldBeSameEisNameKey));
		TxControl.setXANodeName(xaNodeName);
	}

	private class MyWrapper implements XAResourceWrapper {

		private String jndiName;

		public MyWrapper(String jndiName) {
			this.jndiName = jndiName;
		}

		@Override
		public String getJndiName() {
			return jndiName;
		}

		@Override
		public void commit(Xid arg0, boolean arg1) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public void end(Xid arg0, int arg1) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public void forget(Xid arg0) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public int getTransactionTimeout() throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isSameRM(XAResource arg0) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int prepare(Xid arg0) throws XAException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Xid[] recover(int arg0) throws XAException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void rollback(Xid arg0) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean setTransactionTimeout(int arg0) throws XAException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void start(Xid arg0, int arg1) throws XAException {
			// TODO Auto-generated method stub

		}

		@Override
		public XAResource getResource() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getProductName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getProductVersion() {
			// TODO Auto-generated method stub
			return null;
		}

	}
}
