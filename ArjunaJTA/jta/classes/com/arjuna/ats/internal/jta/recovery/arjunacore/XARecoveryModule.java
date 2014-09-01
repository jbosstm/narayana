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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jta.resources.arjunacore.XAResourceRecord;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.arjuna.ats.jta.recovery.XARecoveryResourceManager;
import com.arjuna.ats.jta.recovery.XAResourceOrphanFilter;
import com.arjuna.ats.jta.recovery.XAResourceRecovery;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.ats.jta.utils.XAHelper;

/**
 * Designed to be able to recover any XAResource.
 */

public class XARecoveryModule implements RecoveryModule
{
	public XARecoveryModule()
	{
		this(new com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceManagerImple(),
                "Local XARecoveryModule");

		com.arjuna.ats.internal.jta.Implementations.initialise();
	}


    public void addXAResourceRecoveryHelper(XAResourceRecoveryHelper xaResourceRecoveryHelper) {
        _xaResourceRecoveryHelpers.add(xaResourceRecoveryHelper);
    }

    public void removeXAResourceRecoveryHelper(XAResourceRecoveryHelper xaResourceRecoveryHelper) {
        synchronized (scanState) {
            if (getScanState().equals(ScanStates.FIRST_PASS)) {
                // the first pass collects xa resources from recovery helpers - wait for it to finish
                waitForScanState(ScanStates.BETWEEN_PASSES);

                if (getScanState().equals(ScanStates.BETWEEN_PASSES)) {
                    /*
                     * check whether any resources found in the first pass were provided by
                     * the target xaResourceRecoveryHelper and if so then we need to wait for second pass
                     * of the scanner to finish
                     */
                    if (isHelperInUse(xaResourceRecoveryHelper))
                        waitForScanState(ScanStates.IDLE);
                }
            } else if (!getScanState().equals(ScanStates.IDLE)) {
                // scanner is in pass 2 or in between passes
                if (isHelperInUse(xaResourceRecoveryHelper))
                    waitForScanState(ScanStates.IDLE);
            }

            _xaResourceRecoveryHelpers.remove(xaResourceRecoveryHelper);
        }
    }

    public void addXAResourceOrphanFilter(XAResourceOrphanFilter xaResourceOrphanFilter) {
        synchronized (_xaResourceOrphanFilters) {
            if(!_xaResourceOrphanFilters.contains(xaResourceOrphanFilter)) {
                _xaResourceOrphanFilters.add(xaResourceOrphanFilter);
            }
        }
    }

    public void removeXAResourceOrphanFilter(XAResourceOrphanFilter xaResourceOrphanFilter) {
        synchronized (_xaResourceOrphanFilters) {
            _xaResourceOrphanFilters.remove(xaResourceOrphanFilter);
        }
    }
    
	public void addSerializableXAResourceDeserializer(SerializableXAResourceDeserializer serializableXAResourceDeserializer) {
		_seriablizableXAResourceDeserializers.add(serializableXAResourceDeserializer);		
	}
	
	public List<SerializableXAResourceDeserializer> getSeriablizableXAResourceDeserializers() {
		return _seriablizableXAResourceDeserializers;
	}

	public synchronized void periodicWorkFirstPass()
	{
		// JBTM-1354 allow a second thread to execute the first pass but make sure it is only done once per scan (TMSTART/ENDSCAN)
        synchronized (scanState) {
            if (!getScanState().equals(ScanStates.IDLE))
                return;

            setScanState(ScanStates.FIRST_PASS); // synchronized uses a reentrant lock
        }

        if(jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debugv("{0} - first pass", _logName);
        }

		_uids = new InputObjectState();

		/*
		 * Scan for resources in the object store.
		 */

		try
		{
			if (!_recoveryStore.allObjUids(_recoveryManagerClass.type(), _uids))
			{
                jtaLogger.i18NLogger.warn_recovery_alluids();
			}
		}
		catch (ObjectStoreException e)
		{
            jtaLogger.i18NLogger.warn_recovery_objstoreerror(e);
		}
		catch (Exception e)
		{
            jtaLogger.i18NLogger.warn_recovery_periodicfirstpass(_logName+".periodicWorkFirstPass", e);
		}
		// JBTM-1354 JCA needs to be able to recover XAResources associated with a subordinate transaction so we have to do at least
		// the start scan to make sure that we have loaded all the XAResources we possibly can to assist subordinate transactions recovering

		// scan using statically configured plugins;
		_resources = resourceInitiatedRecovery();
		// scan using dynamically configured plugins:
		_resources.addAll(resourceInitiatedRecoveryForRecoveryHelpers());

		List<XAResource> resources = new ArrayList<XAResource>(_resources);
		for (XAResource xaResource : resources) {
			try {
				xaRecoveryFirstPass(xaResource);
			} catch (Exception ex) {
				jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
			}
		}

        setScanState(ScanStates.BETWEEN_PASSES);
	}

	public void periodicWorkSecondPass()
	{
        setScanState(ScanStates.SECOND_PASS);

		if (jtaLogger.logger.isDebugEnabled())
		{
            jtaLogger.logger.debugv("{0} - second pass", _logName);
		}

		try
		{
			// do the recovery on anything from the scan in first pass

			transactionInitiatedRecovery();

			if (jtaLogger.logger.isDebugEnabled()) {
                jtaLogger.logger.debug(_logName
                        + ".transactionInitiatedRecovery completed");
            }

            bottomUpRecovery();

            if (jtaLogger.logger.isDebugEnabled()) {
                jtaLogger.logger.debug(_logName
                        + ".resourceInitiatedRecovery completed");
            }
		}
		catch (Exception e)
		{
            jtaLogger.i18NLogger.warn_recovery_periodicsecondpass(_logName+".periodicWorkSecondPass", e);
		}

		clearAllFailures();

        setScanState(ScanStates.IDLE);
 	}

	public String id()
	{
		return "XARecoveryModule:" + _recoveryManagerClass;
	}

	/**
	 * @param xid The transaction to commit/rollback.
	 *
	 * @return the XAResource than can be used to commit/rollback the specified
	 *         transaction.
	 */
	private XAResource getNewXAResource(Xid xid)
	{
		// JBTM-1354 JCA needs to be able to recover XAResources associated with a subordinate transaction so we have to do at least
		// the start scan to make sure that we have loaded all the XAResources we possibly can to assist subordinate transactions recovering
    	// the reason we can't do bottom up recovery is if this server has an XAResource which tries to recover a remote server (e.g. distributed JTA)
    	// then we get deadlock on the secondpass
    	periodicWorkFirstPass();

        if (_xidScans != null)
		{
			Enumeration<XAResource> keys = _xidScans.keys();

			while (keys.hasMoreElements())
			{
				XAResource theKey = keys.nextElement();
				RecoveryXids xids = _xidScans.get(theKey);

				// JBTM-1255 moved stale check back to bottomUpRecovery
				if (xids.contains(xid)) {
					// This Xid is going to be recovered by the AtomicAction
					xids.remove(xid);
					return theKey;
				}
			}
		}

		return null;
    }

       /**
        * @param xaResourceRecord The record to reassociate.
        *
        * @return the XAResource than can be used to commit/rollback the specified
        *         record.
        */
    public XAResource getNewXAResource(XAResourceRecord xaResourceRecord)
    {
        return getNewXAResource(xaResourceRecord.getXid());
    }

	protected XARecoveryModule(XARecoveryResourceManager recoveryClass, String logName)
    {
        _logName = logName;
        _recoveryManagerClass = recoveryClass;
        if(_recoveryManagerClass == null) {
            jtaLogger.i18NLogger.warn_recovery_constfail();
        }

        _xaRecoverers = jtaPropertyManager.getJTAEnvironmentBean().getXaResourceRecoveries();
        _xaResourceOrphanFilters = jtaPropertyManager.getJTAEnvironmentBean().getXaResourceOrphanFilters();
    }

	private final boolean transactionInitiatedRecovery()
	{
		Uid theUid = null;

		while (Uid.nullUid().notEquals(theUid))
		{
			try
			{
				theUid = UidHelper.unpackFrom(_uids);

				if (theUid.notEquals(Uid.nullUid()))
				{
					/*
					 * Ignore it if it isn't in the store any more. Transaction
					 * probably recovered it.
					 */

					if (_recoveryStore.currentState(theUid, _recoveryManagerClass
							.type()) != StateStatus.OS_UNKNOWN)
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
								if (jtaLogger.logger.isDebugEnabled()) {
                                    jtaLogger.logger.debug("XARecovery attempting recovery of "
                                            + theUid);
                                }

								int recoveryStatus = record.recover();

								if (recoveryStatus != XARecoveryResource.RECOVERED_OK)
								{
									if (recoveryStatus == XARecoveryResource.WAITING_FOR_RECOVERY)
									{
									    // resource initiated recovery not possible (no distribution).

										problem = false;

                                        jtaLogger.i18NLogger.info_recovery_recoverydelayed(theUid, Integer.toString(recoveryStatus));
									}
									else
									{
                                        jtaLogger.i18NLogger.warn_recovery_recoveryfailed(theUid, Integer.toString(recoveryStatus));
									}
								}
								else
									problem = false;
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
								if (jtaLogger.logger.isDebugEnabled()) {
                                    jtaLogger.logger.debug("XARecovery " + theUid
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

                            jtaLogger.i18NLogger.warn_recovery_recoveryerror(e);
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
                                jtaLogger.i18NLogger.warn_recovery_cannotadd();
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
                jtaLogger.i18NLogger.warn_recovery_unexpectedrecoveryerror(e);
			}
		}

		return true;
	}

	/**
	 * 
	 * JBTM-895 garbage collection is now done when we return XAResources {@see XARecoveryModule#getNewXAResource(XAResourceRecord)}
	 * @see XARecoveryModule#getNewXAResource(XAResourceRecord)
	 */
    private void bottomUpRecovery() {
			for (XAResource xaResource : _resources) {
				try {
					xaRecoverySecondPass(xaResource);
				} catch (Exception ex) {
					jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
				}
			}


        // JBTM-895 garbage collection is now done when we return XAResources {@see XARecoveryModule#getNewXAResource(XAResourceRecord)}
        // JBTM-924 requires this here garbage collection, see JBTM-1155:
        if (_xidScans != null) {
            Set<XAResource> keys = new HashSet<XAResource>(_xidScans.keySet());
            for(XAResource theKey : keys) {
                RecoveryXids recoveryXids = _xidScans.get(theKey);
                if(recoveryXids.isStale()) {
                    _xidScans.remove(theKey);
                }
            }
        }
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
	 */

	private final List<XAResource> resourceInitiatedRecovery()
	{
		/*
		 * Now any additional connections we may need to create. Relies upon
		 * information provided by the application.
		 */

		List<XAResource> xaresources = new ArrayList<XAResource>();
		if (_xaRecoverers.size() > 0)
		{
			for (int i = 0; i < _xaRecoverers.size(); i++)
			{
				try
				{
					XAResourceRecovery ri = (XAResourceRecovery) _xaRecoverers.get(i);

					while (ri.hasMoreResources())
					{
						xaresources.add(ri.getXAResource());
					}
				}
				catch (Exception ex)
				{
                    jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
				}
			}
		}

		return xaresources;
	}

    private List<XAResource> resourceInitiatedRecoveryForRecoveryHelpers()
    {
		List<XAResource> xaresources = new ArrayList<XAResource>();

        recoveryHelpersXAResource.clear();

        for (XAResourceRecoveryHelper xaResourceRecoveryHelper : _xaResourceRecoveryHelpers)
        {
            try
            {
                XAResource[] xaResources = xaResourceRecoveryHelper.getXAResources();
                if (xaResources != null)
                {
                    for (XAResource xaResource : xaResources)
                    {
                        xaresources.add(xaResource);
                    }
                    recoveryHelpersXAResource.put(xaResourceRecoveryHelper, xaResources);
                }
            }
            catch (Exception ex)
            {
                jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
            }
        }

        return xaresources;
    }


	private final void xaRecoveryFirstPass(XAResource xares)
	{
		if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("xarecovery of " + xares);
        }

		
			Xid[] trans = null;

			try
			{
				trans = xares.recover(XAResource.TMSTARTRSCAN);

				if (jtaLogger.logger.isDebugEnabled()) {
                    jtaLogger.logger.debug("Found "
                            + ((trans != null) ? trans.length : 0)
                            + " xids in doubt");
                }
				if (jtaLogger.logger.isTraceEnabled()) {
					for (Xid xid : trans) {
						byte[] globalTransactionId = xid.getGlobalTransactionId();
						byte[] branchQualifier = xid.getBranchQualifier();
	
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("< ");
						stringBuilder.append(xid.getFormatId());
						stringBuilder.append(", ");
						stringBuilder.append(globalTransactionId.length);
						stringBuilder.append(", ");
						stringBuilder.append(branchQualifier.length);
						stringBuilder.append(", ");
						for (int i = 0; i < globalTransactionId.length; i++) {
							stringBuilder.append(globalTransactionId[i]);
						}
						stringBuilder.append(", ");
						for (int i = 0; i < branchQualifier.length; i++) {
							stringBuilder.append(branchQualifier[i]);
						}
						stringBuilder.append(" >");
	
						jtaLogger.logger.debug("Recovered: "
								+ stringBuilder.toString());
					}
				}
			}
			catch (XAException e)
			{
                jtaLogger.i18NLogger.warn_recovery_xarecovery1(_logName+".xaRecovery", XAHelper.printXAErrorCode(e), e);

				try
				{
					xares.recover(XAResource.TMENDRSCAN);
				}
				catch (Exception e1)
				{
				}

				return;
			}

			RecoveryXids xidsToRecover = null;

			if (_xidScans == null)
				_xidScans = new Hashtable<XAResource,RecoveryXids>();
			else
			{
                refreshXidScansForEquivalentXAResourceImpl(xares, trans);

				xidsToRecover = _xidScans.get(xares);

				if (xidsToRecover == null)
				{
                    // this is probably redundant now due to updateIfEquivalentRM,
                    // but in some implementations hashcode/equals does not behave itself.

					java.util.Enumeration<RecoveryXids> elements = _xidScans.elements();
					boolean found = false;

					while (elements.hasMoreElements())
					{
						xidsToRecover = elements.nextElement();

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
	}
	
	private void xaRecoverySecondPass(XAResource xares) { 

		if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("xarecovery second pass of " + xares);
        }
		
		RecoveryXids xidsToRecover = _xidScans.get(xares);
		try {
			Xid[] xids = xidsToRecover.toRecover();

			if (xids != null)
			{
				if (jtaLogger.logger.isDebugEnabled()) {
                    jtaLogger.logger.debug("Have "
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

						recordUid = previousFailure(xids[j]);

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
                            doForget = handleOrphan(xares, xids[j]);
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
                                jtaLogger.i18NLogger.warn_recovery_failedtorecover(_logName+".xaRecovery", Integer.toString(recoveryStatus));
							}

							removeFailure(record.getXid(), record.get_uid());
						}

						if (doForget)
						{
							try
							{
								xares.forget(xids[j]);
							}
							catch (Exception e)
							{
                                jtaLogger.i18NLogger.warn_recovery_forgetfailed(_logName+".xaRecovery", e);
							}
						}

					} while (recordUid != null);
				}
			}
		}
		catch (Exception e)
		{
            jtaLogger.i18NLogger.warn_recovery_generalrecoveryerror(_logName + ".xaRecovery", e);
		}

		try
		{
			if (xares != null)
				xares.recover(XAResource.TMENDRSCAN);
		}
		catch (XAException e)
		{
            jtaLogger.i18NLogger.warn_recovery_xarecovery1(_logName+".xaRecovery", XAHelper.printXAErrorCode(e), e);
		}

		return;
	}

    /**
     * Apply use configurable filtering to determine how to handle the in-doubt resource.
     *
     * @param xares
     * @param xid
     * @return true if forget should be called, false otherwise.
     */
    private boolean handleOrphan(XAResource xares, Xid xid)
    {
        // be default we play it safe and leave resources alone unless a filter explicitly recognizes them.
        // getting presumed abort behaviour therefore requires appropriate filters to be registered.
        XAResourceOrphanFilter.Vote votingOutcome = XAResourceOrphanFilter.Vote.LEAVE_ALONE;

        for(XAResourceOrphanFilter filter : _xaResourceOrphanFilters) {
            XAResourceOrphanFilter.Vote vote = filter.checkXid(xid);

            if(jtaLogger.logger.isDebugEnabled()) {
                jtaLogger.logger.debug("XAResourceOrphanFilter " + filter.getClass().getName() + " voted " + vote);
            }

            if(vote == XAResourceOrphanFilter.Vote.LEAVE_ALONE)
            {
                return false;
            }
            else if(vote == XAResourceOrphanFilter.Vote.ROLLBACK)
            {
                votingOutcome = vote;
            }
        }

        try
        {
            if(votingOutcome == XAResourceOrphanFilter.Vote.ROLLBACK)
            {
                jtaLogger.i18NLogger.info_recovery_rollingback(XAHelper.xidToString(xid));

                xares.rollback(xid);
            }
        }
        catch (XAException e1)
        {
        	jtaLogger.i18NLogger.warn_recovery_xarecovery1(_logName+".xaRecovery", XAHelper.printXAErrorCode(e1), e1);

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
                    return true;
                }
                default:
                    break;
            }
        }
        catch (Exception e2)
        {
            jtaLogger.i18NLogger.warn_recovery_xarecovery2(_logName+".xaRecovery", e2);
        }
        return false;
    }

    /**
     * For some drivers, isSameRM is connection specific. If we have prev scan results
     * for the same RM but using a different connection, we need to be able to identify them.
     * Look at the data from previous scans, identify any for the same RM but different XAResource
     * by checking for matching Xids, then replace the old XAResource with the supplied one.
     *
     * @param xares
     * @param xids
     */
    private void refreshXidScansForEquivalentXAResourceImpl(XAResource xares, Xid[] xids)
    {
        Set<XAResource> keys = new HashSet<XAResource>(_xidScans.keySet());

        for(XAResource theKey : keys) {
            RecoveryXids recoveryXids = _xidScans.get(theKey);

            if(recoveryXids.updateIfEquivalentRM(xares, xids)) {
                // recoveryXids is for this xares, but was originally obtained using
                // a different XAResource. rekey the hashtable to use the new one.
                _xidScans.remove(theKey);
                _xidScans.put(xares, recoveryXids);
                _resources.remove(theKey);
                // There could be two datasources pointed at the same resource manager
                if (!_resources.contains(xares)) {
                	_resources.add(xares);
                }
            }
        }
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

    /**
     * Check whether an XAResourceRecoveryHelper is currently being used by the scanner.
     * Must be called holding a lock on scanState
     * @param xaResourceRecoveryHelper the helper
     * @return true if the helper is in use
     */
    private boolean isHelperInUse(XAResourceRecoveryHelper xaResourceRecoveryHelper) {
        XAResource[] xaResources = recoveryHelpersXAResource.get(xaResourceRecoveryHelper);

        if (xaResources != null) {
            for (int i = 0; i < xaResources.length; i++) {
                RecoveryXids recoveryXids = _xidScans.get(xaResources[i]);
                if (recoveryXids != null && recoveryXids.size() > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Wait until scanner reaches a specific target state.
     * Must be called holding a lock on scanState.
     * @param state the target scan state to wait for
     * @return false if the thread was interrupted
     */
    private boolean waitForScanState(ScanStates state) {
        try {
            do {
                scanState.wait();
            } while (!getScanState().equals(state));

            return true;
        } catch (InterruptedException e) {
            tsLogger.logger.warn("problem waiting for scanLock whilst in state " + state.name(), e);
            return false;
        }
    }

    /**
     * Update the status of the scanner
     * @param state the new state
     */
    private void setScanState(ScanStates state) {
        synchronized (scanState) {
            tsLogger.logger.debugf("XARecoveryModule state change %s->%s%n", getScanState(), state);
            scanState.set(state.ordinal());
            scanState.notifyAll();
        }
    }

    private ScanStates getScanState() {
        return ScanStates.values()[scanState.get()];
    }

    private RecoveryStore _recoveryStore = StoreManager.getRecoveryStore();

	private InputObjectState _uids = new InputObjectState();

	private List<XAResource> _resources;

    // WARNING com.hp.mwtests.ts.jta.recovery.XARecoveryModuleUnitTest uses reflection to peek at the scan state of this recovery module
    private enum ScanStates {
        IDLE,
        FIRST_PASS,
        BETWEEN_PASSES,
        SECOND_PASS
    }
    private AtomicInteger scanState = new AtomicInteger(ScanStates.IDLE.ordinal());

	private final List<XAResourceRecovery> _xaRecoverers;

    private final Set<XAResourceRecoveryHelper> _xaResourceRecoveryHelpers = new CopyOnWriteArraySet<XAResourceRecoveryHelper>();

    private final List<XAResourceOrphanFilter> _xaResourceOrphanFilters;

    private Hashtable _failures = null;

    private Hashtable<XAResourceRecoveryHelper,XAResource[]> recoveryHelpersXAResource = new Hashtable<XAResourceRecoveryHelper,XAResource[]>();

	private Hashtable<XAResource,RecoveryXids> _xidScans = null;

	private XARecoveryResourceManager _recoveryManagerClass = null;

	private String _logName = null;

	private List<SerializableXAResourceDeserializer> _seriablizableXAResourceDeserializers = new ArrayList<SerializableXAResourceDeserializer>();

}
