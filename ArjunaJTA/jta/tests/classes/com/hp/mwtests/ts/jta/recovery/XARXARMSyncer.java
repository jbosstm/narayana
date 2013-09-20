package com.hp.mwtests.ts.jta.recovery;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;

public class XARXARMSyncer implements XAResource {
	private static List<Xid> xids = new ArrayList<Xid>();
	private XARecoveryModule xarm;

	private static boolean orphanDetected;

	public static boolean isOrphanDetected() {
		return orphanDetected;
	}

	public XARXARMSyncer(XARecoveryModule xarm) {
		orphanDetected = false;
		this.xarm = xarm;
	}

	public XARXARMSyncer() {
		orphanDetected = false;
	}

	public int prepare(Xid xid) throws XAException {
		System.out.println("**prepare " + xid);
		xids.add(xid);

		xarm.periodicWorkFirstPass();

		return XAResource.XA_OK;
	}

	public void commit(Xid xid, boolean onePhase) throws XAException {
		if (!xids.remove(xid)) {
			throw new XAException("unknown xid: " + xid);
		}
		System.out.println("**commit " + xid);
	}

	public void rollback(Xid xid) throws XAException {
		if (!xids.remove(xid)) {
			orphanDetected = true;
//			throw new XAException("unknown xid: " + xid);
		}
		System.out.println("**rollback " + xid);
	}

	public Xid[] recover(int flag) throws XAException {
		System.out.println("**xarecover " + flag);
		for (int i = 0; i < xids.size(); i++) {
			System.out.println("  **found " + xids.get(i));
		}
		System.out.println("**xarecover returning");

		return xids.toArray(new Xid[0]);
	}

	public void end(Xid xid, int flags) throws XAException {
	}

	public void forget(Xid xid) throws XAException {
	}

	public int getTransactionTimeout() throws XAException {
		return 0;
	}

	public boolean setTransactionTimeout(int seconds) throws XAException {
		return true;
	}

	public void start(Xid xid, int flags) throws XAException {
	}

	public boolean isSameRM(XAResource xares) throws XAException {
		return (xares == this);
	}

}
