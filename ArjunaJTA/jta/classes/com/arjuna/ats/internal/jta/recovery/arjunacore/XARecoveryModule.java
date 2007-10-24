/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XARecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.objectstore.ObjectStore;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.internal.jta.transaction.arjunacore.AtomicAction;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.common.Environment;
import com.arjuna.ats.jta.recovery.*;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.XidImple;

import com.arjuna.common.util.logging.*;

import java.util.*;
import javax.transaction.xa.*;

import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;

import java.io.IOException;
import javax.transaction.xa.XAException;

/**
 * Designed to be able to recover any XAResource.
 */

public class XARecoveryModule implements RecoveryModule
{
	// why not in Environment?

	public static final String XARecoveryPropertyNamePrefix = "com.arjuna.ats.jta.recovery.XAResourceRecovery";

	private static final String RECOVER_ALL_NODES = "*";

	public XARecoveryModule()
	{
		this(
				com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceManagerImple.class
						.getName(), "Local XARecoveryModule");

		com.arjuna.ats.internal.jta.Implementations.initialise();
	}

	/**
	 * @message com.arjuna.ats.internal.jta.recovery.xafirstpass {0} - first
	 *          pass
	 * @message com.arjuna.ats.internal.jta.recovery.alluids could not get all
	 *          object Uids.
	 * @message com.arjuna.ats.internal.jta.recovery.objstoreerror {0}
	 * @message com.arjuna.ats.internal.jta.recovery.periodicfirstpass {0}
	 *          exception
	 * @message com.arjuna.ats.internal.jta.recovery.info.firstpass {0} - first
	 *          pass
	 */

	public void periodicWorkFirstPass()
	{
		if (jtaLogger.loggerI18N.isInfoEnabled())
		{
			jtaLogger.loggerI18N.info(
					"com.arjuna.ats.internal.jta.recovery.info.firstpass",
					new Object[]
					{ _logName });
		}

		_uids = new InputObjectState();

		/*
		 * Scan for resources in the object store.
		 */

		try
		{
			if (!_objStore.allObjUids(_recoveryManagerClass.type(), _uids))
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N
							.warn("com.arjuna.ats.internal.jta.recovery.alluids");
				}
			}
		}
		catch (ObjectStoreException e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.recovery.objstoreerror",
								e);
			}
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.recovery.periodicfirstpass",
								new Object[]
								{ _logName
										+ ".periodicWorkFirstPass exception " },
								e);
			}
		}
	}

	/**
	 * @message com.arjuna.ats.internal.jta.recovery.periodicsecondpass {0}
	 *          exception
	 * @message com.arjuna.ats.internal.jta.recovery.info.secondpass {0} -
	 *          second pass
	 */

	public void periodicWorkSecondPass()
	{
		if (jtaLogger.logger.isInfoEnabled())
		{
			if (jtaLogger.loggerI18N.isInfoEnabled())
			{
				jtaLogger.loggerI18N.info(
						"com.arjuna.ats.internal.jta.recovery.info.secondpass",
						new Object[]
						{ _logName });
			}
		}

		try
		{
			// do the recovery on anything from the scan in first pass

			transactionInitiatedRecovery();

			if (jtaLogger.logger.isDebugEnabled())
			{
				jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
						VisibilityLevel.VIS_PUBLIC,
						FacilityCode.FAC_CRASH_RECOVERY, _logName
								+ ".transactionInitiatedRecovery completed");
			}

			/*
			 * See the comment about this routine!!
			 */

			resourceInitiatedRecovery();

			if (jtaLogger.logger.isDebugEnabled())
			{
				jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
						VisibilityLevel.VIS_PUBLIC,
						FacilityCode.FAC_CRASH_RECOVERY, _logName
								+ ".resourceInitiatedRecovery completed");
			}
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.recovery.periodicsecondpass",
								new Object[]
								{ _logName
										+ ".periodicWorkSecondPass exception " },
								e);
			}
		}

		clearAllFailures();
	}

	public String id()
	{
		return "XARecoveryModule:" + _recoveryManagerClass;
	}

	/**
	 * @param Xid
	 *            xid The transaction to commit/rollback.
	 * 
	 * @return the XAResource than can be used to commit/rollback the specified
	 *         transaction.
	 */

	public XAResource getNewXAResource(Xid xid)
	{
		if (_xidScans == null)
			resourceInitiatedRecovery();

		if (_xidScans != null)
		{
			Enumeration keys = _xidScans.keys();

			while (keys.hasMoreElements())
			{
				XAResource theKey = (XAResource) keys.nextElement();
				RecoveryXids xids = (RecoveryXids) _xidScans.get(theKey);

				if (xids.contains(xid))
					return theKey;
			}
		}

		return null;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.recovery.constfail {0}
	 * @message com.arjuna.ats.internal.jta.recovery.classloadfail {0} - could
	 *          not get class name for {1}
	 * @message com.arjuna.ats.internal.jta.recovery.general Caught exception:
	 *          {0} for {1}
	 * @message com.arjuna.ats.internal.jta.recovery.info.loading {0} loading
	 *          {1}
	 */

	protected XARecoveryModule(String recoveryClass, String logName)
	{
		_xaRecoverers = new Vector();
		_logName = logName;

		try
		{
			Class c = Thread.currentThread().getContextClassLoader().loadClass(
					recoveryClass);

			_recoveryManagerClass = (XARecoveryResourceManager) c.newInstance();
		}
		catch (Exception ex)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn(
						"com.arjuna.ats.internal.jta.recovery.constfail", ex);
			}

			_recoveryManagerClass = null;
		}

		Properties props = jtaPropertyManager.propertyManager.getProperties();

		if (props != null)
		{
			Enumeration names = props.propertyNames();

			while (names.hasMoreElements())
			{
				String propName = (String) names.nextElement();

				if (propName
						.startsWith(XARecoveryModule.XARecoveryPropertyNamePrefix))
				{
					/*
					 * Given the recovery string, create the class it refers to
					 * and store it.
					 */

					String theClassAndParameter = jtaPropertyManager.propertyManager
							.getProperty(propName);

					// see if there is a string parameter

					int breakPosition = theClassAndParameter
							.indexOf(BREAKCHARACTER);

					String theClass = null;
					String theParameter = null;

					if (breakPosition != -1)
					{
						theClass = theClassAndParameter.substring(0,
								breakPosition);
						theParameter = theClassAndParameter
								.substring(breakPosition + 1);
					}
					else
					{
						theClass = theClassAndParameter;
					}

					if (jtaLogger.loggerI18N.isInfoEnabled())
					{
						if (jtaLogger.loggerI18N.isInfoEnabled())
						{
							jtaLogger.loggerI18N
									.info(
											"com.arjuna.ats.internal.jta.recovery.info.loading",
											new Object[]
											{
													_logName,
													(theClass + ((theParameter != null) ? theParameter
															: "")) });
						}
					}

					if (theClass == null)
					{
						if (jtaLogger.loggerI18N.isWarnEnabled())
						{
							jtaLogger.loggerI18N
									.warn(
											"com.arjuna.ats.internal.jta.recovery.classloadfail",
											new Object[]
											{ _logName, propName });
						}
					}
					else
					{
						try
						{
							Class c = Thread.currentThread()
									.getContextClassLoader()
									.loadClass(theClass);

							XAResourceRecovery ri = (XAResourceRecovery) c
									.newInstance();

							if (theParameter != null)
								ri.initialise(theParameter);

							_xaRecoverers.addElement(ri);
						}
						catch (Exception e)
						{
							if (jtaLogger.loggerI18N.isWarnEnabled())
							{
								jtaLogger.loggerI18N
										.warn(
												"com.arjuna.ats.internal.jta.recovery.general",
												new Object[]
												{ e, theClass });
							}
						}
					}
				}
				else
				{
					if (propName.startsWith(Environment.XA_RECOVERY_NODE))
					{
						/*
						 * Find the node(s) we can recover on behalf of.
						 */

						String name = jtaPropertyManager.propertyManager
								.getProperty(propName);

						if (_xaRecoveryNodes == null)
							_xaRecoveryNodes = new Vector();

						_xaRecoveryNodes.addElement(name);
					}
				}
			}
		}

		if ((_xaRecoveryNodes == null) || (_xaRecoveryNodes.size() == 0))
		{
			if (jtaLogger.loggerI18N.isInfoEnabled())
			{
				jtaLogger.loggerI18N
						.info("com.arjuna.ats.internal.jta.recovery.noxanodes");
			}
		}
	}

	/**
	 * @message com.arjuna.ats.internal.jta.recovery.recoveryfailed JTA failed
	 *          to recovery {0}; got status {1}
	 * @message com.arjuna.ats.internal.jta.recovery.recoverydelayed JTA
	 *          recovery delayed for {0}; got status {1} so waiting for
	 *          coordinator driven recovery
	 * @message com.arjuna.ats.internal.jta.recovery.recoveryerror Recovery
	 *          threw:
	 * @message com.arjuna.ats.internal.jta.recovery.cannotadd Cannot add
	 *          resource to table: no XID value available.
	 * @message com.arjuna.ats.internal.jta.recovery.unexpectedrecoveryerror
	 *          Unexpceted recovery error:
	 * @message com.arjuna.ats.internal.jta.recovery.noxanodes No XA recovery
	 *          nodes specified. Will only recover saved states.
	 */

	private final boolean transactionInitiatedRecovery()
	{
		Uid theUid = new Uid();

		while (theUid.notEquals(Uid.nullUid()))
		{
			try
			{
				theUid.unpack(_uids);

				if (theUid.notEquals(Uid.nullUid()))
				{
					/*
					 * Ignore it if it isn't in the store any more. Transaction
					 * probably recovered it.
					 */

					if (_objStore.currentState(theUid, _recoveryManagerClass
							.type()) != ObjectStore.OS_UNKNOWN)
					{
						boolean problem = false;
						XARecoveryResource record = null;

						try
						{
							record = _recoveryManagerClass.getResource(theUid);

							problem = true;

							switch (record.recoverable())
							{
							case XARecoveryResource.RECOVERY_REQUIRED:
							{
								if (jtaLogger.logger.isDebugEnabled())
								{
									jtaLogger.logger.debug(
											DebugLevel.FUNCTIONS,
											VisibilityLevel.VIS_PUBLIC,
											FacilityCode.FAC_CRASH_RECOVERY,
											"XARecovery attempting recovery of "
													+ theUid);
								}

								int recoveryStatus = record.recover();

								if (recoveryStatus != XARecoveryResource.RECOVERED_OK)
								{
									if (recoveryStatus == XARecoveryResource.WAITING_FOR_RECOVERY)
									{
										problem = false;

										if (jtaLogger.loggerI18N
												.isInfoEnabled())
										{
											jtaLogger.loggerI18N
													.info(
															"com.arjuna.ats.internal.jta.recovery.recoverydelayed",
															new Object[]
															{
																	theUid,
																	new Integer(
																			recoveryStatus) });
										}
									}
									else
									{
										if (jtaLogger.loggerI18N
												.isWarnEnabled())
										{
											jtaLogger.loggerI18N
													.warn(
															"com.arjuna.ats.internal.jta.recovery.recoveryfailed",
															new Object[]
															{
																	theUid,
																	new Integer(
																			recoveryStatus) });
										}
									}
								}
								else
									problem = false; // resource initiated
								// recovery not possible
								// (no distribution).
							}
								break;
							case XARecoveryResource.INFLIGHT_TRANSACTION:
							{
								/*
								 * Transaction was inflight and between us
								 * noticing it and trying to access the state,
								 * it finished and removed the state.
								 */

								problem = false;
							}
								break;
							case XARecoveryResource.INCOMPLETE_STATE:
							default:
							{
								if (jtaLogger.logger.isDebugEnabled())
								{
									jtaLogger.logger.debug(
											DebugLevel.FUNCTIONS,
											VisibilityLevel.VIS_PUBLIC,
											FacilityCode.FAC_CRASH_RECOVERY,
											"XARecovery " + theUid
													+ " is non-recoverable");
								}
							}
								break;
							}
						}
						catch (NullPointerException ex)
						{
							problem = true;
						}
						catch (Throwable e)
						{
							problem = true;

							if (jtaLogger.loggerI18N.isWarnEnabled())
							{
								jtaLogger.loggerI18N
										.warn(
												"com.arjuna.ats.internal.jta.recovery.recoveryerror",
												e);
							}
						}

						if (problem && (record != null))
						{
							/*
							 * Some error occurred which prevented the state of
							 * the resource from being read from the log. Hence
							 * we don't have a valid key to use to insert it
							 * into the list of records to be recovered. Print a
							 * warning and move on. Force recovery via the
							 * administration tool. Should be a rare occurrence.
							 */

							if (record.getXid() == null)
							{
								if (jtaLogger.loggerI18N.isWarnEnabled())
								{
									jtaLogger.loggerI18N
											.warn("com.arjuna.ats.internal.jta.recovery.cannotadd");
								}
							}
							else
							{
								addFailure(record.getXid(), record.get_uid());
							}
						}
					}
				}
			}
			catch (IOException e)
			{
				theUid = Uid.nullUid();
			}
			catch (Throwable e)
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N
							.warn(
									"com.arjuna.ats.internal.jta.recovery.unexpectedrecoveryerror",
									e);
				}
			}
		}

		return true;
	}

	/**
	 * Now check for any outstanding transactions. If we didn't fail to recover
	 * them, then roll them back - if they'd got through prepare we would have
	 * an entry within the object store.
	 * 
	 * Rely upon _xaRecoverers being set up properly (via properties).
	 * 
	 * We cannot just remember the XAResourceRecords we used (if any) to cache
	 * the JDBC connection information and use that since we may never have had
	 * any such records!
	 * 
	 * IMPORTANT: resourceInitiatedRecovery may rollback transactions which are
	 * inflight: just because we have no entry for a transaction in the object
	 * store does not mean it does not exist - it may be *about* to write its
	 * intentions list. To try to reduce this probability we remember potential
	 * rollback-ees at this scan, and wait for the next scan before actually
	 * rolling them back.
	 * 
	 * Note we cannot use the method that works with Transactions and
	 * TransactionalObjects, of checking with original process that created the
	 * transaction, because we don't know which process it was.
	 * 
	 * @message com.arjuna.ats.internal.jta.recovery.getxaresource Caught:
	 */

	private final boolean resourceInitiatedRecovery()
	{
		/*
		 * Now any additional connections we may need to create. Relies upon
		 * information provided by the application.
		 */

		if (_xaRecoverers.size() > 0)
		{
			for (int i = 0; i < _xaRecoverers.size(); i++)
			{
				XAResource resource = null;

				try
				{
					XAResourceRecovery ri = (XAResourceRecovery) _xaRecoverers
							.elementAt(i);

					while (ri.hasMoreResources())
					{
						try
						{
							resource = ri.getXAResource();

							xaRecovery(resource);
						}
						catch (Exception exp)
						{
							if (jtaLogger.loggerI18N.isWarnEnabled())
							{
								jtaLogger.loggerI18N
										.warn(
												"com.arjuna.ats.internal.jta.recovery.getxaresource",
												exp);
							}
						}
					}
				}
				catch (Exception ex)
				{
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N
								.warn(
										"com.arjuna.ats.internal.jta.recovery.getxaresource",
										ex);
					}
				}
			}
		}

		return true;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.recovery.xarecovery1 {0} got XA
	 *          exception {1}, {2}
	 * @message com.arjuna.ats.internal.jta.recovery.xarecovery2 {0} got
	 *          exception {1}
	 * @message com.arjuna.ats.internal.jta.recovery.failedtorecover {0} -
	 *          failed to recover XAResource.
	 * @message com.arjuna.ats.internal.jta.recovery.forgetfailed {0} - forget
	 *          threw: {1}
	 * @message com.arjuna.ats.internal.jta.recovery.generalrecoveryerror {0} -
	 *          caught {1}
	 * @message com.arjuna.ats.internal.jta.recovery.info.rollingback Rolling
	 *          back {0}
	 * @message com.arjuna.ats.internal.jta.recovery.info.notrollback Told not
	 *          to rollback {0}
	 */

	private final boolean xaRecovery(XAResource xares)
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					FacilityCode.FAC_CRASH_RECOVERY, "xarecovery of " + xares);
		}

		try
		{
			Xid[] trans = null;

			try
			{
				trans = xares.recover(XAResource.TMSTARTRSCAN);

				if (jtaLogger.loggerI18N.isDebugEnabled())
				{
					jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
							VisibilityLevel.VIS_PUBLIC,
							FacilityCode.FAC_CRASH_RECOVERY, "Found "
									+ ((trans != null) ? trans.length : 0)
									+ " xids in doubt");
				}
			}
			catch (XAException e)
			{
				if (jtaLogger.loggerI18N.isWarnEnabled())
				{
					jtaLogger.loggerI18N.warn(
							"com.arjuna.ats.internal.jta.recovery.xarecovery1",
							new Object[]
							{ _logName + ".xaRecovery ", e,
									XAHelper.printXAErrorCode(e) });
				}

				try
				{
					xares.recover(XAResource.TMENDRSCAN);
				}
				catch (Exception e1)
				{
				}

				return false;
			}

			RecoveryXids xidsToRecover = null;

			if (_xidScans == null)
				_xidScans = new Hashtable();
			else
			{
				xidsToRecover = (RecoveryXids) _xidScans.get(xares);

				if (xidsToRecover == null)
				{
					java.util.Enumeration elements = _xidScans.elements();
					boolean found = false;

					while (elements.hasMoreElements())
					{
						xidsToRecover = (RecoveryXids) elements.nextElement();

						if (xidsToRecover.isSameRM(xares))
						{
							found = true;

							break;
						}
					}

					if (!found)
						xidsToRecover = null;
				}
			}

			if (xidsToRecover == null)
			{
				xidsToRecover = new RecoveryXids(xares);

				_xidScans.put(xares, xidsToRecover);
			}

			xidsToRecover.nextScan(trans);

			Object[] xids = xidsToRecover.toRecover();

			if (xids != null)
			{
				if (jtaLogger.loggerI18N.isDebugEnabled())
				{
					jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
							VisibilityLevel.VIS_PUBLIC,
							FacilityCode.FAC_CRASH_RECOVERY, "Have "
									+ xids.length
									+ " Xids to recover on this pass.");
				}

				for (int j = 0; j < xids.length; j++)
				{
					boolean doForget = false;

					/*
					 * Check if in failure list.
					 */

					Uid recordUid = null;
					boolean foundTransaction = false;

					do
					{
						// is the xid known to be one that couldn't be recovered

						recordUid = previousFailure((Xid) xids[j]);

						if ((recordUid == null) && (foundTransaction))
							break; // end
						// of
						// recovery
						// for
						// this
						// transaction

						if (recordUid == null)
						{
							/*
							 * It wasn't an xid that we couldn't recover, so the
							 * RM knows about it, but we don't. Therefore it may
							 * have to be rolled back.
							 */

							if (jtaLogger.loggerI18N.isDebugEnabled())
							{
								jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
										VisibilityLevel.VIS_PUBLIC,
										FacilityCode.FAC_CRASH_RECOVERY,
										"Checking node name of "
												+ ((Xid) xids[j]));
							}

							String nodeName = XAUtils
									.getXANodeName((Xid) xids[j]);
							boolean doRecovery = false;

							if (jtaLogger.loggerI18N.isDebugEnabled())
							{
								jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
										VisibilityLevel.VIS_PUBLIC,
										FacilityCode.FAC_CRASH_RECOVERY,
										"Node name is " + nodeName);
							}

							/*
							 * If there is no node name but we have been told to
							 * recover everything, then we can roll it back.
							 */

							if ((nodeName == null)
									&& (_xaRecoveryNodes != null)
									&& (_xaRecoveryNodes
											.contains(RECOVER_ALL_NODES)))
							{
								if (jtaLogger.loggerI18N.isDebugEnabled())
								{
									jtaLogger.logger.debug(
											DebugLevel.FUNCTIONS,
											VisibilityLevel.VIS_PUBLIC,
											FacilityCode.FAC_CRASH_RECOVERY,
											"Will recover this Xid (a)");
								}

								doRecovery = true;
							}
							else
							{
								if (nodeName != null)
								{
									/*
									 * Check that the node name is in our
									 * recovery set or that we have been told to
									 * recover everything.
									 */

									if (_xaRecoveryNodes != null)
									{
										if (_xaRecoveryNodes
												.contains(RECOVER_ALL_NODES)
												|| _xaRecoveryNodes
														.contains(nodeName))
										{
											if (jtaLogger.loggerI18N
													.isDebugEnabled())
											{
												jtaLogger.logger
														.debug(
																DebugLevel.FUNCTIONS,
																VisibilityLevel.VIS_PUBLIC,
																FacilityCode.FAC_CRASH_RECOVERY,
																"Will recover this Xid (b)");
											}

											doRecovery = true;
										}
										else
										{
											if (jtaLogger.loggerI18N
													.isDebugEnabled())
											{
												jtaLogger.logger
														.debug(
																DebugLevel.FUNCTIONS,
																VisibilityLevel.VIS_PUBLIC,
																FacilityCode.FAC_CRASH_RECOVERY,
																"Will not recover this Xid (a)");
											}
										}
									}
									else
									{
										if (jtaLogger.loggerI18N
												.isDebugEnabled())
										{
											jtaLogger.logger
													.debug(
															DebugLevel.FUNCTIONS,
															VisibilityLevel.VIS_PUBLIC,
															FacilityCode.FAC_CRASH_RECOVERY,
															"Will not recover this Xid (b)");
										}
									}
								}
								else
								{
									if (jtaLogger.loggerI18N.isDebugEnabled())
									{
										jtaLogger.logger
												.debug(
														DebugLevel.FUNCTIONS,
														VisibilityLevel.VIS_PUBLIC,
														FacilityCode.FAC_CRASH_RECOVERY,
														"Will not recover this Xid");
									}
								}
							}

							try
							{
								if (doRecovery)
								{
									if (jtaLogger.loggerI18N.isInfoEnabled())
									{
										jtaLogger.loggerI18N
												.info(
														"com.arjuna.ats.internal.jta.recovery.info.rollingback",
														new Object[]
														{ XAHelper
																.xidToString((Xid) xids[j]) });
									}

									if (!transactionLog((Xid) xids[j]))
										xares.rollback((Xid) xids[j]);
									else
									{
										/*
										 * Ignore it as the transaction system
										 * will recovery it eventually.
										 */
									}
								}
								else
								{
									if (jtaLogger.loggerI18N.isInfoEnabled())
									{
										jtaLogger.loggerI18N
												.info(
														"com.arjuna.ats.internal.jta.recovery.info.notrollback",
														new Object[]
														{ XAHelper
																.xidToString((Xid) xids[j]) });
									}
								}
							}
							catch (XAException e1)
							{
								e1.printStackTrace();

								switch (e1.errorCode)
								{
								case XAException.XAER_RMERR:
									break;
								case XAException.XA_HEURHAZ:
								case XAException.XA_HEURCOM:
								case XAException.XA_HEURMIX:
								case XAException.XA_HEURRB:
								case XAException.XA_RBROLLBACK:
								{
									if (!doForget) // already done?
										doForget = true;
								}
									break;
								default:
									break;
								}
							}
							catch (Exception e2)
							{
								if (jtaLogger.loggerI18N.isWarnEnabled())
								{
									jtaLogger.loggerI18N
											.warn(
													"com.arjuna.ats.internal.jta.recovery.xarecovery2",
													new Object[]
													{
															_logName
																	+ ".xaRecovery ",
															e2 });
								}
							}
						}
						else
						{
							foundTransaction = true;

							/*
							 * In the failures list so it may be that we just
							 * need another XAResource to be able to recover
							 * this.
							 */

							XARecoveryResource record = _recoveryManagerClass
									.getResource(recordUid, xares);
							int recoveryStatus = record.recover();

							if (recoveryStatus != XARecoveryResource.RECOVERED_OK)
							{
								if (jtaLogger.loggerI18N.isWarnEnabled())
								{
									jtaLogger.loggerI18N
											.warn(
													"com.arjuna.ats.internal.jta.recovery.failedtorecover",
													new Object[]
													{
															_logName
																	+ ".xaRecovery ",
															new Integer(
																	recoveryStatus) });
								}
							}

							removeFailure(record.getXid(), record.get_uid());
						}

						if (doForget)
						{
							try
							{
								xares.forget((Xid) xids[j]);
							}
							catch (Exception e)
							{
								if (jtaLogger.loggerI18N.isWarnEnabled())
								{
									jtaLogger.loggerI18N
											.warn(
													"com.arjuna.ats.internal.jta.recovery.forgetfailed",
													new Object[]
													{ _logName + ".xaRecovery",
															e });
								}
							}
						}

					} while (recordUid != null);
				}
			}
		}
		catch (Exception e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.recovery.generalrecoveryerror",
								new Object[]
								{ _logName + ".xaRecovery", e });
			}

			e.printStackTrace();
		}

		try
		{
			if (xares != null)
				xares.recover(XAResource.TMENDRSCAN);
		}
		catch (XAException e)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N.warn(
						"com.arjuna.ats.internal.jta.recovery.xarecovery1",
						new Object[]
						{ _logName + ".xaRecovery", e,
								XAHelper.printXAErrorCode(e) });
			}
		}

		return true;
	}

	/**
	 * Is there a log file for this transaction?
	 * 
	 * @param Xid
	 *            xid the transaction to check.
	 * 
	 * @return <code>boolean</code>true if there is a log file,
	 *         <code>false</code> if there isn't.
	 * 
	 * @message com.arjuna.ats.internal.jta.recovery.notaxid {0} not an Arjuna
	 *          XID
	 */

	private final boolean transactionLog(Xid xid)
	{
		if (_transactionStore == null)
		{
			_transactionStore = TxControl.getStore();
		}

		XidImple theXid = new XidImple(xid);
		Uid u = com.arjuna.ats.internal.arjuna.utils.XATxConverter
				.getUid(theXid.getXID());

		if (!u.equals(Uid.nullUid()))
		{
			try
			{
				if (_transactionStore.currentState(u, _transactionType) != ObjectStore.OS_UNKNOWN)
				{
					return true;
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		else
		{
			if (jtaLogger.logger.isInfoEnabled())
			{
				jtaLogger.loggerI18N.info(
						"com.arjuna.ats.internal.jta.recovery.notaxid",
						new Object[]
						{ xid });
			}
		}

		return false;
	}

	/**
	 * Is the Xid is in the failure list, i.e., the list of those transactions
	 * we couldn't recover, possibly because of transient failures. If so,
	 * return the uid of (one of) the records and remove it from the list.
	 */

	private final Uid previousFailure(Xid xid)
	{
		if (_failures == null)
		{
			return null;
		}

		Enumeration e = _failures.keys();

		while (e.hasMoreElements())
		{
			Xid theXid = (Xid) e.nextElement();

			if (XAHelper.sameXID(xid, theXid))
			{
				// remove uid from failure list
				Vector failureItem = (Vector) _failures.get(theXid);
				Uid u = (Uid) failureItem.remove(0);

				if (failureItem.size() == 0)
					_failures.remove(theXid);

				return u;
			}
		}

		// not present in the failures list.

		return null;
	}

	/* methods to manipulate the failure list */

	/**
	 * Add record to failure list
	 */

	private void addFailure(Xid xid, Uid uid)
	{
		if (_failures == null)
			_failures = new Hashtable();

		Vector failureItem = (Vector) _failures.get(xid);

		if (failureItem == null)
		{
			failureItem = new Vector();

			_failures.put(xid, failureItem);
		}

		failureItem.addElement(uid);
	}

	/**
	 * @message com.arjuna.ats.internal.jta.recovery.removefailed {0} - could
	 *          not remove record for {1}
	 */

	/* remove record uid from failure list */
	private void removeFailure(Xid xid, Uid uid)
	{
		// find the failure item for this xid
		Vector failureItem = (Vector) _failures.get(xid);

		if (failureItem == null)
		{
			/*
			 * if (jtaLogger.loggerI18N.isWarnEnabled()) {
			 * jtaLogger.loggerI18N.warn("com.arjuna.ats.internal.jta.recovery.removefailed",
			 * new Object[] { _logName, xid}); }
			 */

			/*
			 * Already removed via previousFailure.
			 */
		}
		else
		{
			// remove this record from the item
			failureItem.remove(uid);

			// if that was the last one, remove the item altogether
			if (failureItem.size() == 0)
				_failures.remove(xid);
		}
	}

	private void clearAllFailures()
	{
		if (_failures != null)
			_failures.clear();
	}

	private ObjectStore _objStore = new ObjectStore();

	private InputObjectState _uids = new InputObjectState();

	private Vector _xaRecoverers = null;

	private Hashtable _failures = null;

	private Vector _xaRecoveryNodes = null;

	private Hashtable _xidScans = null;

	private XARecoveryResourceManager _recoveryManagerClass = null;

	private String _logName = null;

	// 'type' within the Object Store for AtomicActions.
	private String _transactionType = new AtomicAction().type();

	// Reference to the Object Store.
	private static ObjectStore _transactionStore = null;

	private static int _backoffPeriod = 0;

	private static final int XA_BACKOFF_PERIOD = 20000; // backoff in

	// milliseconds

	private static final char BREAKCHARACTER = ';'; // delimiter for xaconnrecov
	// property

	static
	{
		String env = jtaPropertyManager.propertyManager
				.getProperty(com.arjuna.ats.jta.common.Environment.XA_BACKOFF_PERIOD);

		XARecoveryModule._backoffPeriod = XA_BACKOFF_PERIOD;

		if (env != null)
		{
			try
			{
				Integer i = new Integer(env);

				XARecoveryModule._backoffPeriod = i.intValue();
			}
			catch (Exception e)
			{
				throw new ExceptionInInitializerError(e);
			}
		}
	}

}
