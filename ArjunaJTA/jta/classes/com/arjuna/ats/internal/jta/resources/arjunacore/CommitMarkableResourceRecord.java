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

package com.arjuna.ats.internal.jta.resources.arjunacore;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.tm.ConnectableResource;
import org.jboss.tm.XAResourceWrapper;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.recovery.arjunacore.CommitMarkableResourceRecordRecoveryModule;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.xa.XID;
import com.arjuna.ats.jta.common.JTAEnvironmentBean;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

/**
 * The CommitMarkableResourceRecord does not support nested transactions
 * 
 * If the database is down forever that a CommitMarkableResourceRecord is linked
 * to, it will have the side effect of never expiring a RecoverAtomicAction.
 * 
 * The CommitMarkableResourceRecord assumes the following table has been
 * created:
 * 
 * syb:
 * 
 * create table xids (xid varbinary(144), transactionManagerID varchar(64),
 * actionuid varbinary(28))
 * 
 * ora:
 * 
 * create table xids (xid RAW(144), transactionManagerID varchar(64), actionuid
 * RAW(28))
 * 
 * psql:
 * 
 * create table xids (xid bytea, transactionManagerID varchar(64), actionuid
 * bytea)
 * 
 * h2:
 * 
 * create table xids (xid varbinary(144), transactionManagerID varchar(64),
 * actionuid varbinary(28))
 * 
 * sybase notes: sp_configure "lock scheme",0,datarows
 */
public class CommitMarkableResourceRecord extends AbstractRecord {

	private final String tableName;
	private Xid xid;
	private ConnectableResource connectableResource;
	private boolean onePhase = false;
	private String commitMarkableJndiName;
	private boolean committed;
	private BasicAction basicAction;
	private String productName;
	private String productVersion;
	private boolean hasCompleted;
	private static CommitMarkableResourceRecordRecoveryModule commitMarkableResourceRecoveryModule;
	private static final JTAEnvironmentBean jtaEnvironmentBean = BeanPopulator
			.getDefaultInstance(JTAEnvironmentBean.class);
	private static final Map<String, String> commitMarkableResourceTableNameMap = jtaEnvironmentBean
			.getCommitMarkableResourceTableNameMap();
	private static final String defaultTableName = jtaEnvironmentBean
			.getDefaultCommitMarkableTableName();
	private boolean isPerformImmediateCleanupOfBranches = jtaEnvironmentBean
			.isPerformImmediateCleanupOfCommitMarkableResourceBranches();
	private Connection preparedConnection;
	private static final boolean isNotifyRecoveryModuleOfCompletedBranches = jtaEnvironmentBean
			.isNotifyCommitMarkableResourceRecoveryModuleOfCompleteBranches();
	private static final Map<String, Boolean> isPerformImmediateCleanupOfCommitMarkableResourceBranchesMap = jtaEnvironmentBean
			.getPerformImmediateCleanupOfCommitMarkableResourceBranchesMap();

	static {
		commitMarkableResourceRecoveryModule = null;
		RecoveryManager recMan = RecoveryManager.manager();
		Vector recoveryModules = recMan.getModules();
		if (recoveryModules != null) {
			Enumeration modules = recoveryModules.elements();

			while (modules.hasMoreElements()) {
				RecoveryModule m = (RecoveryModule) modules.nextElement();

				if (m instanceof CommitMarkableResourceRecordRecoveryModule) {
					commitMarkableResourceRecoveryModule = (CommitMarkableResourceRecordRecoveryModule) m;
					break;
				}
			}
		}
	}

	/**
	 * For recovery
	 */
	public CommitMarkableResourceRecord() {
		tableName = null;
	}

	public CommitMarkableResourceRecord(TransactionImple tx,
			ConnectableResource xaResource, final Xid xid,
			BasicAction basicAction) throws IllegalStateException,
			RollbackException, SystemException {
		super(new Uid(), null, ObjectType.ANDPERSISTENT);

		this.connectableResource = xaResource;
		XAResourceWrapper xaResourceWrapper = ((XAResourceWrapper) xaResource);
		this.commitMarkableJndiName = xaResourceWrapper.getJndiName();
		this.productName = xaResourceWrapper.getProductName();
		this.productVersion = xaResourceWrapper.getProductVersion();
		this.xid = xid;
		this.basicAction = basicAction;

		String tableName = commitMarkableResourceTableNameMap
				.get(commitMarkableJndiName);
		if (tableName != null) {
			this.tableName = tableName;
		} else {
			this.tableName = defaultTableName;
		}

		Boolean boolean1 = isPerformImmediateCleanupOfCommitMarkableResourceBranchesMap
				.get(commitMarkableJndiName);
		if (boolean1 != null) {
			isPerformImmediateCleanupOfBranches = boolean1;
		}

		if (isPerformImmediateCleanupOfBranches) {
			tx.registerSynchronization(new Synchronization() {

				@Override
				public void beforeCompletion() {

				}

				@Override
				public void afterCompletion(int status) {
					if (!onePhase && status == Status.STATUS_COMMITTED) {

						Connection connection = null;
						try {
							connection = ((Connection) connectableResource
									.getConnection());
							connection.setAutoCommit(false);
							String sql = "DELETE from "
									+ CommitMarkableResourceRecord.this.tableName
									+ " where xid in (?)";
							PreparedStatement prepareStatement = connection
									.prepareStatement(sql);
							try {

								XID toSave = ((XidImple) xid).getXID();
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								DataOutputStream dos = new DataOutputStream(
										baos);
								dos.writeInt(toSave.formatID);
								dos.writeInt(toSave.gtrid_length);
								dos.writeInt(toSave.bqual_length);
								dos.writeInt(toSave.data.length);
								dos.write(toSave.data);
								dos.flush();

								prepareStatement.setBytes(1, baos.toByteArray());

								if (prepareStatement.executeUpdate() != 1) {
									tsLogger.logger
											.error("Update was not successfull");
									connection.rollback();
								} else {
									connection.commit();
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
						} catch (Throwable e1) {
							tsLogger.logger
									.warn("Could not delete CommitMarkableResourceRecord entry, will rely on RecoveryModule",
											e1);
						} finally {
							if (connection != null) {
								try {
									connection.close();
								} catch (SQLException e) {
									tsLogger.logger
											.warn("Could not close the preparedConnection",
													e);
								}
							}
						}
					}
				}
			});
		} else if (isNotifyRecoveryModuleOfCompletedBranches) {
			tx.registerSynchronization(new Synchronization() {

				@Override
				public void beforeCompletion() {

				}

				@Override
				public void afterCompletion(int status) {
					if (!onePhase && status == Status.STATUS_COMMITTED) {
						commitMarkableResourceRecoveryModule
								.notifyOfCompletedBranch(
										commitMarkableJndiName, xid);
					}
				}
			});
		}
	}

	public String getProductName() {
		return productName;
	}

	public String getProductVersion() {
		return productVersion;
	}

	public String getJndiName() {
		return commitMarkableJndiName;
	}

	public void updateOutcome(boolean committed) {
		this.hasCompleted = true;
		this.committed = committed;
	}

	/**
	 * We need to save this so we know there was a ConnectableResource in the
	 * intentions list.
	 */
	public boolean doSave() {
		return true;
	}

	public boolean save_state(OutputObjectState os, int t) {
		boolean res = false;

		try {
			// We store these information so that during recovery we can query
			// the resource
			// manager to see if it had committed prior to any potential crash
			if (tsLogger.logger.isTraceEnabled()) {
				tsLogger.logger.trace("pack: " + commitMarkableJndiName);
			}
			os.packString(commitMarkableJndiName);
			if (tsLogger.logger.isTraceEnabled()) {
				tsLogger.logger.trace("pack: " + xid);
			}
			XidImple.pack(os, xid);
			os.packBoolean(hasCompleted);
			if (hasCompleted) {
				os.packBoolean(committed);
			}
			os.packString(productName);
			os.packString(productVersion);

			res = super.save_state(os, t);
		} catch (Exception e) {
			jtaLogger.logger.warn(
					"Could not save_state: " + XAHelper.xidToString(xid), e);
		}

		return res;
	}

	public boolean restore_state(InputObjectState os, int t) {
		boolean res = false;

		try {
			commitMarkableJndiName = os.unpackString();
			if (tsLogger.logger.isTraceEnabled()) {
				tsLogger.logger.trace("unpack: " + commitMarkableJndiName);
			}
			xid = XidImple.unpack(os);
			if (tsLogger.logger.isTraceEnabled()) {
				tsLogger.logger.trace("unpack: " + xid);
			}

			if (os.unpackBoolean()) {
				committed = os.unpackBoolean();
			} else {
				// This will return true if the
				// CommitMarkableRecoveryModule is
				// between phases and the XID
				// has not been GC'd
				committed = commitMarkableResourceRecoveryModule.wasCommitted(
						commitMarkableJndiName, xid);
			}
			productName = os.unpackString();
			productVersion = os.unpackString();
			res = super.restore_state(os, t);
		} catch (Exception e) {
			jtaLogger.logger.warn(
					"Could not restore_state" + XAHelper.xidToString(xid), e);
		}

		return res;
	}

	/**
	 * This will add the required recovery data about this resource into the
	 * resources preparedConnection. If the preparedConnection is in read only
	 * mode, we do not need to persist this information.
	 */
	public int topLevelPrepare() {
		try {
			preparedConnection = (Connection) connectableResource
					.getConnection();

			PreparedStatement prepareStatement = preparedConnection
					.prepareStatement("insert into "
							+ tableName
							+ " (xid, transactionManagerID, actionuid) values (?,?,?)");
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dos = new DataOutputStream(baos);
				XID toSave = ((XidImple) xid).getXID();
				dos.writeInt(toSave.formatID);
				dos.writeInt(toSave.gtrid_length);
				dos.writeInt(toSave.bqual_length);
				dos.writeInt(toSave.data.length);
				dos.write(toSave.data);
				dos.flush();
				prepareStatement.setBytes(1, baos.toByteArray());
				prepareStatement.setString(2, TxControl.getXANodeName());
				prepareStatement.setBytes(3, basicAction.get_uid().getBytes());

				if (prepareStatement.executeUpdate() != 1) {
					tsLogger.logger.warn("Update was not successful");
					return TwoPhaseOutcome.PREPARE_NOTOK;
				}
			} finally {
				prepareStatement.close();
			}

			return TwoPhaseOutcome.PREPARE_OK;
		} catch (Throwable t) {
			tsLogger.logger.error(
					"Could not add recovery data to the 1PC resource", t);
			return TwoPhaseOutcome.PREPARE_NOTOK;
		}
	}

	public int topLevelAbort() {
		try {
			try {
				// This can never be null as it can only ever be called before
				// crash
				// when we have a reference
				// on a connectableResource still. Although topLevelAbort can be
				// called for RecoverAtomicAction, it
				// can only do that for resources after the head position in the
				// preparedList, we know this resource
				// must be first
				((XAResource) connectableResource).rollback(xid);
				hasCompleted = true;
				committed = false;
				return TwoPhaseOutcome.FINISH_OK;
			} catch (Throwable e) {
				tsLogger.logger.error("Could not rollback the 1PC resource", e);
				return TwoPhaseOutcome.FINISH_ERROR;
			}
		} finally {
			try {
				if (preparedConnection != null) {
					preparedConnection.close();
				}
			} catch (Throwable e) {
				tsLogger.logger.warn("Could not close the preparedConnection",
						e);
			}
		}
	}

	public int topLevelCommit() {
		return commit(false);
	}

	public int topLevelOnePhaseCommit() {
		return commit(true);
	}

	private int commit(boolean onePhase) {
		// As this can be called during recovery we check to see if we have the
		// pre-crash reference
		this.onePhase = onePhase;

		if (connectableResource != null) {

			try {
				((XAResource) connectableResource).commit(xid, false);
				hasCompleted = true;
				committed = true;
				return TwoPhaseOutcome.FINISH_OK;
			} catch (Throwable e) {
				tsLogger.logger.error(
						"Could not commit the preparedConnection", e);
				return TwoPhaseOutcome.FINISH_ERROR;
			} finally {
				if (!isPerformImmediateCleanupOfBranches) {
					try {
						if (preparedConnection != null) {
							if (preparedConnection != null) {
								preparedConnection.close();
							}
						}
					} catch (Throwable e) {
						tsLogger.logger.warn(
								"Could not close the preparedConnection", e);
					}
				}
			}
		} else {
			// This is a recovery scenario
			if (committed) {
				return TwoPhaseOutcome.FINISH_OK;
			} else {
				return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
			}
		}
	}

	public Uid order() {
		return Uid.minUid();
	}

	public boolean propagateOnCommit() {
		return false;
	}

	public int typeIs() {
		return RecordType.COMMITMARKABLERESOURCE;
	}

	public String type() {
		return "/StateManager/AbstractRecord/CommitMarkableResourceRecord";
	}

	public Object value() {
		return connectableResource;
	}

	public void setValue(Object o) {
	}

	public int nestedAbort() {
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedCommit() {
		return TwoPhaseOutcome.FINISH_ERROR;
	}

	public int nestedPrepare() {
		return TwoPhaseOutcome.PREPARE_NOTOK;
	}

	public void merge(AbstractRecord a) {
	}

	public void alter(AbstractRecord a) {
	}

	public boolean shouldAdd(AbstractRecord a) {
		return false;
	}

	public boolean shouldAlter(AbstractRecord a) {
		return false;
	}

	public boolean shouldMerge(AbstractRecord a) {
		return false;
	}

	public boolean shouldReplace(AbstractRecord a) {
		return false;
	}
}
