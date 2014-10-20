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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.ObjectStoreIterator;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * This CommitMarkableResourceRecord assumes the following table has been
 * created:
 * 
 * create table xids (xid varbinary(255), transactionManagerID varchar(255))
 * (ora/syb/mysql) create table xids (xid bytea, transactionManagerID
 * varchar(255)) (psql) sp_configure "lock scheme",0,datarows (syb)
 * 
 * The CommitMarkableResourceRecord does not support nested transactions
 * 
 * TODO you have to set max_allowed_packet for large reaps on mysql
 */
public class CommitMarkableResourceRecordRecoveryModule implements
		RecoveryModule {
	// 'type' within the Object Store for AtomicActions.
	private static final String ATOMIC_ACTION_TYPE = RecoverConnectableAtomicAction.ATOMIC_ACTION_TYPE;
	private static final String CONNECTABLE_ATOMIC_ACTION_TYPE =
	    RecoverConnectableAtomicAction.CONNECTABLE_ATOMIC_ACTION_TYPE;

	private InitialContext context;

	private List<String> jndiNamesToContact = new ArrayList<String>();

	private Map<Xid, String> committedXidsToJndiNames = new HashMap<Xid, String>();

	private List<String> queriedResourceManagers = new ArrayList<String>();

	// Reference to the Object Store.
	private static RecoveryStore recoveryStore = null;

	/**
	 * This map contains items that were in the database, we use it in phase 2
	 * to work out what we can GC
	 */
	private Map<String, Map<Xid, Uid>> jndiNamesToPossibleXidsForGC = new HashMap<String, Map<Xid, Uid>>();

	/**
	 * Typically the whereFilter will restrict this node to recovering the
	 * database solely for itself but it is possible to recover for different
	 * nodes. It uses the JTAEnvironmentBean::getXaRecoveryNodes().
	 */
	private String whereFilter;

	private TransactionStatusConnectionManager transactionStatusConnectionMgr;

	private static JTAEnvironmentBean jtaEnvironmentBean = BeanPopulator
			.getDefaultInstance(JTAEnvironmentBean.class);
	private Map<String, String> commitMarkableResourceTableNameMap = jtaEnvironmentBean
			.getCommitMarkableResourceTableNameMap();
	private Map<String, List<Xid>> completedBranches = new HashMap<String, List<Xid>>();
    private boolean inFirstPass;
	private static String defaultTableName = jtaEnvironmentBean
			.getDefaultCommitMarkableTableName();

	public CommitMarkableResourceRecordRecoveryModule() throws NamingException,
			ObjectStoreException {
		context = new InitialContext();
		JTAEnvironmentBean jtaEnvironmentBean = BeanPopulator
				.getDefaultInstance(JTAEnvironmentBean.class);
		jndiNamesToContact.addAll(jtaEnvironmentBean
				.getCommitMarkableResourceJNDINames());
		
		if (tsLogger.logger.isTraceEnabled()) {
			tsLogger.logger
					.trace("CommitMarkableResourceRecordRecoveryModule::list to contact");
			for (String jndiName : jndiNamesToContact) {
				tsLogger.logger
						.trace("CommitMarkableResourceRecordRecoveryModule::in list: "
								+ jndiName);
			}
			tsLogger.logger
					.trace("CommitMarkableResourceRecordRecoveryModule::list to contact complete");
		}

		List<String> xaRecoveryNodes = jtaEnvironmentBean.getXaRecoveryNodes();
		if (xaRecoveryNodes
				.contains(NodeNameXAResourceOrphanFilter.RECOVER_ALL_NODES)) {
			whereFilter = "";
		} else {
			StringBuffer buffer = new StringBuffer();
			Iterator<String> iterator = xaRecoveryNodes.iterator();
			while (iterator.hasNext()) {
				buffer.append("\'" + iterator.next() + "\',");
			}
			whereFilter = " where transactionManagerID in ( "
					+ buffer.substring(0, buffer.length() - 1) + ")";
		}

		if (recoveryStore == null) {
			recoveryStore = StoreManager.getRecoveryStore();
		}
		transactionStatusConnectionMgr = new TransactionStatusConnectionManager();
	}

	public void notifyOfCompletedBranch(String commitMarkableResourceJndiName,
			Xid xid) {
		synchronized (completedBranches) {
			List<Xid> completedXids = completedBranches
					.get(commitMarkableResourceJndiName);
			if (completedXids == null) {
				completedXids = new ArrayList<Xid>();
				completedBranches.put(commitMarkableResourceJndiName,
						completedXids);
			}
			completedXids.add(xid);
		}
	}

	@Override
	public synchronized void periodicWorkFirstPass() {
        if (inFirstPass) {
            return;
        }
	    inFirstPass = true;
		// TODO - this is one shot only due to a
		// remove in the function, if this delete fails only normal
		// recovery is possible
		Map<String, List<Xid>> completedBranches2 = new HashMap<String, List<Xid>>();
		synchronized (completedBranches) {
            completedBranches2.putAll(completedBranches);
			completedBranches.clear();
		}

        for (Map.Entry<String, List<Xid>> e : completedBranches2.entrySet())
		    delete(e.getKey(), e.getValue());

		if (tsLogger.logger.isTraceEnabled()) {
			tsLogger.logger
					.trace("CommitMarkableResourceRecordRecoveryModule::periodicWorkFirstPass");
		}

		this.committedXidsToJndiNames.clear();
		this.queriedResourceManagers.clear();
		this.jndiNamesToPossibleXidsForGC.clear();

		// The algorithm occurs in three stages:
		// 1. We query the database to find all the branches that were committed
		// 3. We check for previously moved AtomicActions where the resource
		// manager was offline but is now online and move them back for
		// processing
		// 3. We check for in doubt AtomicActions that have incomplete branches
		// where the resource manager is now online and update them with the
		// outcome

		// Stage 1
		// Talk to all the known resource managers that support
		// CommitMarkableResourceRecord to find out what transactions have
		// committed
		try {
			Iterator<String> iterator = jndiNamesToContact.iterator();
			while (iterator.hasNext()) {
				String jndiName = iterator.next();
				try {
					if (tsLogger.logger.isTraceEnabled()) {
						tsLogger.logger
								.trace("CommitMarkableResourceRecordRecoveryModule::connecting to: " + jndiName);
					}
					DataSource dataSource = (DataSource) context
							.lookup(jndiName);
					Connection connection = dataSource.getConnection();
					try {
						Statement createStatement = connection
								.createStatement();
						try {
							String tableName = commitMarkableResourceTableNameMap
									.get(jndiName);
							if (tableName == null) {
								tableName = defaultTableName;
							}
							ResultSet rs = createStatement
									.executeQuery("SELECT xid,actionuid from "
											+ tableName + whereFilter);
							try {
								int i = 0;
								while (rs.next()) {
									i++;
									byte[] xidAsBytes = rs.getBytes(1);

									ByteArrayInputStream bais = new ByteArrayInputStream(
											xidAsBytes);
									DataInputStream dis = new DataInputStream(
											bais);
									XID _theXid = new XID();
									_theXid.formatID = dis.readInt();
									_theXid.gtrid_length = dis.readInt();
									_theXid.bqual_length = dis.readInt();
									int dataLength = dis.readInt();
									_theXid.data = new byte[dataLength];
									dis.read(_theXid.data, 0, dataLength);
									XidImple xid = new XidImple(_theXid);
									byte[] actionuidAsBytes = new byte[Uid.UID_SIZE];
									byte[] bytes = rs.getBytes(2);
									System.arraycopy(bytes, 0,
											actionuidAsBytes, 0, bytes.length);

									committedXidsToJndiNames.put(xid, jndiName);
									if (tsLogger.logger.isTraceEnabled()) {
										tsLogger.logger
												.trace("committedXidsToJndiNames.put"
														+ xid + " " + jndiName);
									}
									// Populate the map of possible GCable Xids
									Uid actionuid = new Uid(actionuidAsBytes);
									Map<Xid, Uid> map = jndiNamesToPossibleXidsForGC
											.get(jndiName);
									if (map == null) {
										map = new HashMap<Xid, Uid>();
										jndiNamesToPossibleXidsForGC.put(
												jndiName, map);
									}
									map.put(xid, actionuid);
								}
							} finally {
								try {
									rs.close();
								} catch (SQLException e) {
									tsLogger.logger.warn(
											"Could not close resultset", e);
								}
							}
						} finally {
							try {
								createStatement.close();
							} catch (SQLException e) {
								tsLogger.logger.warn(
										"Could not close statement", e);
							}
						}
						queriedResourceManagers.add(jndiName);
					} finally {
						try {
							connection.close();
						} catch (SQLException e) {
							tsLogger.logger.warn("Could not close connection",
									e);
						}

					}
				} catch (NamingException e) {
					tsLogger.logger
							.warn("Could not lookup CommitMarkableResource: "
									+ jndiName);
					tsLogger.logger.debug(
							"Could not lookup CommitMarkableResource: "
									+ jndiName, e);
				} catch (SQLException e) {
					tsLogger.logger.warn("Could not handle connection", e);
				} catch (IOException e) {
					tsLogger.logger.warn(
							"Could not lookup write data to select", e);
				}
			}

			// Stage 2
			// Look in the object store for atomic actions that had a connected
			// resource that was not online in a previous scan but is now.
			// Also look for CONNECTABLE_ATOMIC_ACTION_TYPE that have a matching
			// ATOMIC_ACTION_TYPE and remove the CONNECTABLE_ATOMIC_ACTION_TYPE
			// reference
			try {
				ObjectStoreIterator transactionUidEnum = new ObjectStoreIterator(
						recoveryStore, CONNECTABLE_ATOMIC_ACTION_TYPE);
				Uid currentUid = transactionUidEnum.iterate();
				while (Uid.nullUid().notEquals(currentUid)) {

					// Make sure it isn't garbage from a failure to move before:
					InputObjectState state = recoveryStore.read_committed(
							currentUid, ATOMIC_ACTION_TYPE);
					if (state != null) {
						if (!recoveryStore.remove_committed(currentUid,
                                CONNECTABLE_ATOMIC_ACTION_TYPE)) {
                            tsLogger.logger.debug("Could not remove a: "
									+ CONNECTABLE_ATOMIC_ACTION_TYPE + " uid: "
									+ currentUid);
						}
					} else {
    				    state = recoveryStore.read_committed(currentUid, CONNECTABLE_ATOMIC_ACTION_TYPE);
    				    // TX may have been in progress and cleaned up by now 
    	                if (state != null) {
    						RecoverConnectableAtomicAction rcaa = new RecoverConnectableAtomicAction(
    								CONNECTABLE_ATOMIC_ACTION_TYPE, currentUid, state);
    
    						if (rcaa.containsIncompleteCommitMarkableResourceRecord()) {
    							String commitMarkableResourceJndiName = rcaa
    									.getCommitMarkableResourceJndiName();
    							// Check if the resource manager is online yet
    							if (queriedResourceManagers
    									.contains(commitMarkableResourceJndiName)) {
    
    								// If it is remove the CRR and move it back and
    								// let
    								// the
    								// next stage update it
    								moveRecord(currentUid,
    										CONNECTABLE_ATOMIC_ACTION_TYPE,
    										ATOMIC_ACTION_TYPE);
    							}
    						} else {
    						    if (tsLogger.logger.isTraceEnabled()) {
    						        tsLogger.logger.trace("Moving " + currentUid + " back to being an AA");
    						    }
                                // It is now safe to move it back to being an AA so that it can call getNewXAResourceRecord
                                moveRecord(currentUid,
                                        CONNECTABLE_ATOMIC_ACTION_TYPE,
                                        ATOMIC_ACTION_TYPE);    						    
    						}
                        }
					}
					
					currentUid = transactionUidEnum.iterate();
				}
			} catch (ObjectStoreException | IOException ex) {
				tsLogger.logger.warn("Could not query objectstore: ", ex);
			}

			// Stage 3
			// Look for crashed AtomicActions that had a
			// CommitMarkableResourceRecord
			// and see if it is in the list from stage 1, will include all
			// records
			// moved in stage 2
			if (tsLogger.logger.isDebugEnabled()) {
				tsLogger.logger.debug("processing " + ATOMIC_ACTION_TYPE
						+ " transactions");
			}
			try {
				ObjectStoreIterator transactionUidEnum = new ObjectStoreIterator(
						recoveryStore, ATOMIC_ACTION_TYPE);
				Uid currentUid = transactionUidEnum.iterate();
				while (Uid.nullUid().notEquals(currentUid)) {

					// Retrieve the transaction status from its
					// original
					// process.
					if (!isTransactionInMidFlight(transactionStatusConnectionMgr
							.getTransactionStatus(ATOMIC_ACTION_TYPE,
									currentUid))) {

					    InputObjectState state = recoveryStore.read_committed(
	                            currentUid, ATOMIC_ACTION_TYPE);
	                    if (state != null) {
    	                    // Try to load it is a BasicAction that has a
    						// ConnectedResourceRecord
    						RecoverConnectableAtomicAction rcaa = new RecoverConnectableAtomicAction(
    								ATOMIC_ACTION_TYPE, currentUid, state);
    						// Check if it did have a ConnectedResourceRecord
    						if (rcaa.containsIncompleteCommitMarkableResourceRecord()) {
    							String commitMarkableResourceJndiName = rcaa
    									.getCommitMarkableResourceJndiName();
    							// If it did, check if the resource manager was
    							// online
    							if (!queriedResourceManagers
    									.contains(commitMarkableResourceJndiName)) {
    								// If the resource manager wasn't online, move
    								// it
    								moveRecord(currentUid, ATOMIC_ACTION_TYPE,
    										CONNECTABLE_ATOMIC_ACTION_TYPE);
    							} else {
    								// Update the completed outcome for the 1PC
    								// resource
                                    rcaa.updateCommitMarkableResourceRecord(committedXidsToJndiNames.get(rcaa.getXid()) != null);
                                    // Swap the type to avoid the rest of recovery round processing this TX as it already called getNewXAResourceRecord
                                    moveRecord(currentUid, ATOMIC_ACTION_TYPE,
                                            CONNECTABLE_ATOMIC_ACTION_TYPE);
                                    
    							}
    						}
	                    }
					}
					currentUid = transactionUidEnum.iterate();
				}
			} catch (ObjectStoreException | IOException ex) {
				tsLogger.logger.warn("Could not query objectstore: ", ex);
			}
		} catch (IllegalStateException e) {
			// Thrown when AS is shutting down and we attempt a lookup
			tsLogger.logger.debug(
					"Could not lookup datasource, AS is shutting down: "
							+ e.getMessage(), e);
		}
        inFirstPass = false;
	}

	@Override
	public synchronized void periodicWorkSecondPass() {
		/**
		 * This is the list of AtomicActions that were prepared but not
		 * completed.
		 */
		Set<Uid> preparedAtomicActions = new HashSet<Uid>();
		InputObjectState aa_uids = new InputObjectState();
		try {
			// Refresh our list of all the indoubt atomic actions
			if (recoveryStore.allObjUids(ATOMIC_ACTION_TYPE, aa_uids)) {
				preparedAtomicActions.addAll(convertToList(aa_uids));

				// Refresh our list of all the indoubt connectable atomic
				// actions
				if (recoveryStore.allObjUids(CONNECTABLE_ATOMIC_ACTION_TYPE,
						aa_uids)) {
					preparedAtomicActions.addAll(convertToList(aa_uids));

					// Iterate the list that we were able to contact
					Iterator<String> jndiNames = queriedResourceManagers
							.iterator();
					while (jndiNames.hasNext()) {
						String jndiName = jndiNames.next();
						List<Xid> toDelete = new ArrayList<Xid>();

						Map<Xid, Uid> map = jndiNamesToPossibleXidsForGC
								.get(jndiName);
						if (map != null) {
							for (Map.Entry<Xid, Uid> entry : map.entrySet()) {
								Xid next = entry.getKey();
								Uid uid = entry.getValue();
								if (!preparedAtomicActions.contains(uid)) {
									toDelete.add(next);
								}
							}
						}

						delete(jndiName, toDelete);
					}
				} else {
					tsLogger.logger
							.warn("Could not read data from object store");
				}
			} else {
				tsLogger.logger
						.warn("Could not read "
								+ CONNECTABLE_ATOMIC_ACTION_TYPE
								+ " from object store");
			}
		} catch (ObjectStoreException e) {
			tsLogger.logger.warn("Could not read " + ATOMIC_ACTION_TYPE
					+ " from object store", e);
		}
	}

	/**
	 * Can only be called after the first phase has executed
	 * 
	 * @param xid
	 * @return
	 */
	public synchronized boolean wasCommitted(String jndiName, Xid xid)
			throws ObjectStoreException {
		if (!queriedResourceManagers.contains(jndiName) || committedXidsToJndiNames.get(xid) == null) {
		    periodicWorkFirstPass();
		}
		if (!queriedResourceManagers.contains(jndiName)) {
            throw new ObjectStoreException(jndiName + " was not online");
        }
        String committed = committedXidsToJndiNames.get(xid);
		if (tsLogger.logger.isTraceEnabled()) {
			tsLogger.logger.trace("wasCommitted" + xid + " " + committed);
		}
		return committed != null;
	}

	private List<Uid> convertToList(InputObjectState aa_uids) {
		List<Uid> uids = new ArrayList<Uid>();

		boolean moreUids = true;

		while (moreUids) {
			Uid theUid = null;
			try {
				theUid = UidHelper.unpackFrom(aa_uids);

				if (theUid.equals(Uid.nullUid())) {
					moreUids = false;
				} else {
					Uid newUid = new Uid(theUid);

					if (tsLogger.logger.isDebugEnabled()) {
						tsLogger.logger.debug("found transaction " + newUid);
					}

					uids.add(newUid);
				}
			} catch (IOException ex) {
				moreUids = false;
			}
		}
		return uids;
	}

	private boolean isTransactionInMidFlight(int status) {
		boolean inFlight = false;

		switch (status) {
		// these states can only come from a process that is still alive
		case ActionStatus.RUNNING:
		case ActionStatus.ABORT_ONLY:
		case ActionStatus.PREPARING:
		case ActionStatus.COMMITTING:
		case ActionStatus.ABORTING:
		case ActionStatus.PREPARED:
			inFlight = true;
			break;

		// the transaction is apparently still there, but has completed its
		// phase2. should be safe to redo it.
		case ActionStatus.COMMITTED:
		case ActionStatus.H_COMMIT:
		case ActionStatus.H_MIXED:
		case ActionStatus.H_HAZARD:
		case ActionStatus.ABORTED:
		case ActionStatus.H_ROLLBACK:
			inFlight = false;
			break;

		// this shouldn't happen
		case ActionStatus.INVALID:
		default:
			inFlight = false;
		}

		return inFlight;
	}

	private void moveRecord(Uid uid, String from, String to)
			throws ObjectStoreException {
		RecoveryStore recoveryStore = StoreManager.getRecoveryStore();

		InputObjectState state = recoveryStore.read_committed(uid, from);
		if (state != null) {
			if (!recoveryStore.write_committed(uid, to, new OutputObjectState(
					state))) {
				tsLogger.logger.error("Could not move an: " + to + " uid: "
						+ uid);
			} else if (!recoveryStore.remove_committed(uid, from)) {
				tsLogger.logger.error("Could not remove a: " + from + " uid: "
						+ uid);
			}
		} else {
			tsLogger.logger
					.error("Could not read an: " + from + " uid: " + uid);
		}
	}

	private void delete(String jndiName, List<Xid> completedXids) {
		int batchSize = jtaEnvironmentBean
				.getCommitMarkableResourceRecordDeleteBatchSize();
		Integer integer = jtaEnvironmentBean
				.getCommitMarkableResourceRecordDeleteBatchSizeMap().get(
						jndiName);
		if (integer != null) {
			batchSize = integer;
		}
		try {
			while (completedXids.size() > 0) {

				int sendingSize = batchSize < 0 ? completedXids.size()
						: completedXids.size() < batchSize ? completedXids
								.size() : batchSize;

				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < sendingSize; i++) {
					buffer.append("?,");
				}
				if (buffer.length() > 0) {
					Connection connection = null;
					DataSource dataSource = (DataSource) context
							.lookup(jndiName);
					try {
						connection = dataSource.getConnection();
						connection.setAutoCommit(false);

						String tableName = commitMarkableResourceTableNameMap
								.get(jndiName);
						if (tableName == null) {
							tableName = defaultTableName;
						}
						String sql = "DELETE from " + tableName
								+ " where xid in ("
								+ buffer.substring(0, buffer.length() - 1)
								+ ")";
						if (tsLogger.logger.isTraceEnabled()) {
							tsLogger.logger
									.trace("Attempting to delete number of entries: "
											+ buffer.length());
						}
						PreparedStatement prepareStatement = connection
								.prepareStatement(sql);
						List<Xid> deleted = new ArrayList<Xid>();
						try {

							for (int i = 0; i < sendingSize; i++) {
								XidImple xid = (XidImple) completedXids
										.remove(0);
								deleted.add(xid);
								XID toSave = xid.getXID();
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								DataOutputStream dos = new DataOutputStream(
										baos);
								dos.writeInt(toSave.formatID);
								dos.writeInt(toSave.gtrid_length);
								dos.writeInt(toSave.bqual_length);
								dos.writeInt(toSave.data.length);
								dos.write(toSave.data);
								dos.flush();

								prepareStatement.setBytes(i + 1,
										baos.toByteArray());
							}
							int executeUpdate = prepareStatement
									.executeUpdate();
							if (executeUpdate != sendingSize) {
								tsLogger.logger
										.error("Update was not successful, expected: "
												+ sendingSize
												+ " actual:"
												+ executeUpdate);
								connection.rollback();
							} else {
								connection.commit();

                                committedXidsToJndiNames.keySet().removeAll(deleted);
							}
						} catch (IOException e) {
							tsLogger.logger
									.warn("Could not generate prepareStatement paramaters",
											e);
						} finally {
							try {
								prepareStatement.close();
							} catch (SQLException e) {
								tsLogger.logger
										.warn("Could not close the prepared statement",
												e);
							}
						}

					} catch (SQLException e) {
						tsLogger.logger.warn("Could not handle the connection",
								e);
						// the connection is unavailable so try again later
						break;
					} finally {
						if (connection != null) {
							try {
								connection.close();
							} catch (SQLException e) {
								tsLogger.logger.warn(
										"Could not close the connection", e);
							}
						}
					}
				}
			}
		} catch (NamingException e) {
			tsLogger.logger
					.warn("Could not lookup commitMarkable: " + jndiName);
			tsLogger.logger.debug("Could not lookup commitMarkable: "
					+ jndiName, e);
		} catch (IllegalStateException e) {
			// Thrown when AS is shutting down and we attempt a lookup
			tsLogger.logger.debug(
					"Could not lookup datasource, AS is shutting down: "
							+ e.getMessage(), e);
		}
	}
}
