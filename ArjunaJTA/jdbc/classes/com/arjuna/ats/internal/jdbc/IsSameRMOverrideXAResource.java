package com.arjuna.ats.internal.jdbc;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.jdbc.common.JDBCEnvironmentBean;
import com.arjuna.ats.jdbc.common.jdbcPropertyManager;

public class IsSameRMOverrideXAResource implements XAResource {

	private XAResource wrappedXAResource;

	public IsSameRMOverrideXAResource(XAResource xaResource) {
		this.wrappedXAResource = xaResource;
	}

	@Override
	public void commit(Xid arg0, boolean arg1) throws XAException {
		wrappedXAResource.commit(arg0, arg1);
	}

	@Override
	public void end(Xid arg0, int arg1) throws XAException {
		wrappedXAResource.end(arg0, arg1);
	}

	@Override
	public void forget(Xid arg0) throws XAException {
		wrappedXAResource.forget(arg0);
	}

	@Override
	public int getTransactionTimeout() throws XAException {
		return wrappedXAResource.getTransactionTimeout();
	}

	@Override
	public boolean isSameRM(XAResource arg0) throws XAException {
		return false;
	}

	@Override
	public int prepare(Xid arg0) throws XAException {
		return wrappedXAResource.prepare(arg0);
	}

	@Override
	public Xid[] recover(int arg0) throws XAException {
		return wrappedXAResource.recover(arg0);
	}

	@Override
	public void rollback(Xid arg0) throws XAException {
		wrappedXAResource.rollback(arg0);
	}

	@Override
	public boolean setTransactionTimeout(int arg0) throws XAException {
		return wrappedXAResource.setTransactionTimeout(arg0);
	}

	@Override
	public void start(Xid arg0, int arg1) throws XAException {
		wrappedXAResource.start(arg0, arg1);
	}
}
