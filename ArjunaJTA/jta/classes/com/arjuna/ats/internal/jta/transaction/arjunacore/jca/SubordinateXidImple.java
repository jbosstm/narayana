/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
