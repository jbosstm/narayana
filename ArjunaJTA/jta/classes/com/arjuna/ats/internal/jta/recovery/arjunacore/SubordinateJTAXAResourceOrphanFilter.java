/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.transaction.arjunacore.subordinate.jca.SubordinateAtomicAction;
import com.arjuna.ats.jta.common.jtaPropertyManager;
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
		List<String> _xaRecoveryNodes = jtaPropertyManager.getJTAEnvironmentBean().getXaRecoveryNodes();

		if(_xaRecoveryNodes == null || _xaRecoveryNodes.isEmpty()) {
			jtaLogger.i18NLogger.info_recovery_noxanodes();
			return Vote.ABSTAIN;
		}

		String nodeName = XATxConverter.getSubordinateNodeName(new XidImple(xid).getXID());

		if (jtaLogger.logger.isDebugEnabled()) {
			jtaLogger.logger.debug("subordinate node name of " + xid + " is " + nodeName);
		}

		if (!_xaRecoveryNodes.contains(nodeName)) {
			// It either doesn't have a subordinate node name or isn't for this server
			return Vote.ABSTAIN;
		}

		// It does have an XID
		if (nodeName != null) {
			if (transactionLog(xid, nodeName)) {
				// it's owned by a logged transaction which
				// will recover it top down in due course
				return Vote.ABSTAIN;
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
							jtaLogger.i18NLogger.warn_unpacking_xid_state(theXid, recoveryStore, transactionType, ex);

							finished = true;
						}

						if (uid.notEquals(Uid.nullUid())) {
							SubordinateAtomicAction tx = new SubordinateAtomicAction(uid, true);
							XidImple transactionXid = (XidImple) tx.getXid();
							if (transactionXid != null && transactionXid.isSameTransaction(recoveredResourceXid)) {
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
				jtaLogger.i18NLogger.warn_reading_from_object_store(recoveryStore, theXid, e);
			} catch (IOException e) {
				jtaLogger.i18NLogger.warn_reading_from_object_store(recoveryStore, theXid, e);
			}
		}
		return false;
	}
}