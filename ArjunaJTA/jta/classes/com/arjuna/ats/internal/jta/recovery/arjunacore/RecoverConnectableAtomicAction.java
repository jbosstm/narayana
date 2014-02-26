/*
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2013
 * @author JBoss Inc.
 */
package com.arjuna.ats.internal.jta.recovery.arjunacore;

import java.io.IOException;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.Header;
import com.arjuna.ats.internal.jta.resources.arjunacore.CommitMarkableResourceRecord;
import com.arjuna.ats.jta.xa.XidImple;

public class RecoverConnectableAtomicAction extends AtomicAction {

	private String jndiName;
	private String recoveringAs;
	private Xid xid;
	private boolean hasCompleted;

	public RecoverConnectableAtomicAction(String type, Uid rcvUid)
			throws ObjectStoreException, IOException {
		super(rcvUid);
		this.recoveringAs = type;

		InputObjectState os = StoreManager.getParticipantStore()
				.read_committed(objectUid, type());
		// Unpack BasicAction::save_state preamble
		Header hdr = new Header();
		unpackHeader(os, hdr);
		os.unpackBoolean(); // FYI pastFirstParticipant

		// Take a look at the first record type
		int record_type = os.unpackInt();
		if (record_type == RecordType.COMMITMARKABLERESOURCE) {
			// Its one we are interested in
			jndiName = os.unpackString();
			xid = XidImple.unpack(os);
			hasCompleted = os.unpackBoolean();
		}
	}

	@Override
	public String type() {
		return recoveringAs;
	}

	public boolean containsIncompleteCommitMarkableResourceRecord() {
		return jndiName != null && !hasCompleted;
	}

	public String getCommitMarkableResourceJndiName() {
		return jndiName;
	}

	public Xid getXid() {
		return xid;
	}

	public void updateCommitMarkableResourceRecord(boolean committed) {
		activate();
		CommitMarkableResourceRecord peekFront = (CommitMarkableResourceRecord) preparedList
				.peekFront();
		peekFront.updateOutcome(committed);
		deactivate();
	}
}
