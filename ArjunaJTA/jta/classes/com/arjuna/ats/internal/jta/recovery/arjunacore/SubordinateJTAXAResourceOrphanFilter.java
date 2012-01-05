/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.internal.jta.recovery.arjunacore;

import java.io.IOException;
import java.util.Stack;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

/**
 * An XAResourceOrphanFilter which uses detects orphaned subordinate XA
 * Resources.
 */
public class SubordinateJTAXAResourceOrphanFilter implements XAResourceOrphanFilter {
	public static final int RECOVER_ALL_NODES = 0;

	@Override
	public Vote checkXid(Xid xid) {
		String nodeName = XATxConverter.getSubordinateNodeName(new XidImple(xid).getXID());

		if (jtaLogger.logger.isDebugEnabled()) {
			jtaLogger.logger.debug("subordinate node name of " + xid + " is " + nodeName);
		}

		// It does have an XID
		if (nodeName != null) {
			if (transactionLog(xid, nodeName)) {
				// it's owned by a logged transaction which
				// will recover it top down in due course
				return Vote.LEAVE_ALONE;
			} else {
				return Vote.ROLLBACK;
			}
		} else {
			return Vote.ABSTAIN;
		}
	}

	/**
	 * Is there a log file for this transaction?
	 * 
	 * @param recoveredResourceXid
	 *            the transaction to check.
	 * 
	 * @return <code>boolean</code>true if there is a log file,
	 *         <code>false</code> if there isn't.
	 */
	private boolean transactionLog(Xid recoveredResourceXid, String recoveredResourceNodeName) {

		XidImple theXid = new XidImple(recoveredResourceXid);
		Uid u = theXid.getTransactionUid();

		if (jtaLogger.logger.isDebugEnabled()) {
			jtaLogger.logger.debug("Checking whether Xid " + theXid + " exists in ObjectStore.");
		}

		if (!u.equals(Uid.nullUid())) {
			RecoveryStore recoveryStore = StoreManager.getRecoveryStore();
			String transactionType = SubordinateAtomicAction.getType();

			if (jtaLogger.logger.isDebugEnabled()) {
				jtaLogger.logger.debug("Looking for " + u + " and " + transactionType);
			}

			InputObjectState states = new InputObjectState();
			try {
				if (recoveryStore.allObjUids(transactionType, states) && (states.notempty())) {
					Stack values = new Stack();
					boolean finished = false;

					do {
						Uid uid = null;

						try {
							uid = UidHelper.unpackFrom(states);
						} catch (IOException ex) {
							ex.printStackTrace();

							finished = true;
						}

						if (uid.notEquals(Uid.nullUid())) {
							SubordinateAtomicAction tx = new SubordinateAtomicAction(uid, true);
							XidImple transactionXid = (XidImple) tx.getXid();
							if (transactionXid.isSameTransaction(recoveredResourceXid)
									&& recoveredResourceNodeName.equals(XATxConverter.getSubordinateNodeName(transactionXid.getXID()))) {
								if (jtaLogger.logger.isDebugEnabled()) {
									jtaLogger.logger.debug("Found record for " + theXid);
								}
								return true;
							}
						} else
							finished = true;

					} while (!finished);
					if (jtaLogger.logger.isDebugEnabled()) {
						jtaLogger.logger.debug("No record found for " + theXid);
					}
				} else {
					jtaLogger.i18NLogger.info_recovery_notaxid(XAHelper.xidToString(recoveredResourceXid));
				}
			} catch (ObjectStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
}
