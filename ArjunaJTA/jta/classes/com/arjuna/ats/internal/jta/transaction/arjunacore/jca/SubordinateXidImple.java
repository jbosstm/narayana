/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.transaction.arjunacore.jca;

import javax.transaction.xa.Xid;

import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

public class SubordinateXidImple extends XidImple {
	public SubordinateXidImple(Xid xid) {
		super(xid);
	}

	/**
	 * Test equality as being part of the same global transaction
	 */
	@Override
	public boolean equals(Object obj) {
		if (_theXid.formatID != XATxConverter.FORMAT_ID) {
			return super.equals(obj);
		}
		boolean toReturn = false;
		if (obj instanceof SubordinateXidImple) {
			toReturn = isSameTransaction(((SubordinateXidImple) obj));
		}
		return toReturn;
	}

	/**
	 * Generate the hash code for the xid, subordinates are diffed on the gtrid
	 * only.
	 * 
	 * @param xid
	 *            The xid.
	 * @return The hash code.
	 */
	@Override
	protected int getHash(final XID xid) {
		if (xid == null) {
			return 0;
		}
		if (_theXid.formatID != XATxConverter.FORMAT_ID) {
			return super.getHash(xid);
		}
		return generateHash(xid.formatID, xid.data, 0, xid.gtrid_length);
	}

}