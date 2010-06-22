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
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;


import com.arjuna.ats.internal.arjuna.common.UidHelper;

import com.arjuna.ats.jta.logging.jtaLogger;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.*;
import com.arjuna.ats.jta.utils.XAHelper;

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
    public XARecoveryModule()
	{
		this(
				com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceManagerImple.class
						.getName(), "Local XARecoveryModule");

		com.arjuna.ats.internal.jta.Implementations.initialise();
	}


    public void addXAResourceRecoveryHelper(XAResourceRecoveryHelper xaResourceRecoveryHelper) {
        synchronized (_xaResourceRecoveryHelpers) {
            if(!_xaResourceRecoveryHelpers.contains(xaResourceRecoveryHelper)) {
                _xaResourceRecoveryHelpers.add(xaResourceRecoveryHelper);
            }
        }
    }

    public void removeXAResourceRecoveryHelper(XAResourceRecoveryHelper xaResourceRecoveryHelper) {
        synchronized (_xaResourceRecoveryHelpers) {
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


	public void periodicWorkFirstPass()
	{
        jtaLogger.i18NLogger.info_recovery_firstpass(_logName);

		_uids = new InputObjectState();

		/*
		 * Scan for resources in the object store.
		 */

		try
		{
			if (!_objStore.allObjUids(_recoveryManagerClass.type(), _uids))
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
	}

	public void periodicWorkSecondPass()
	{
		if (jtaLogger.logger.isInfoEnabled())
		{
            jtaLogger.i18NLogger.info_recovery_secondpass(_logName);
		}

		try
		{
			// do the recovery on anything from the scan in first pass

			transactionInitiatedRecovery();

			if (jtaLogger.logger.isDebugEnabled()) {
                jtaLogger.logger.debug(_logName
                        + ".transactionInitiatedRecovery completed");
            }

			/*
			 * See the comment about this routine!!
			 */

			resourceInitiatedRecovery();
            resourceInitiatedRecoveryForRecoveryHelpers();

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

	public XAResource getNewXAResource(Xid xid)
	{
		if (_xidScans == null) {
			resourceInitiatedRecovery();
            resourceInitiatedRecoveryForRecoveryHelpers();
        }

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

	protected XARecoveryModule(String recoveryClass, String logName)
    {
        _logName = logName;

        try
        {
            Class c = Thread.currentThread().getContextClassLoader().loadClass(
                    recoveryClass);

            _recoveryManagerClass = (XARecoveryResourceManager) c.newInstance();
        }
        catch (Exception ex)
        {
            jtaLogger.i18NLogger.warn_recovery_constfail(ex);
            _recoveryManagerClass = null;
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

					if (_objStore.currentState(theUid, _recoveryManagerClass
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
					XAResourceRecovery ri = (XAResourceRecovery) _xaRecoverers.get(i);

					while (ri.hasMoreResources())
					{
						try
						{
							resource = ri.getXAResource();

							xaRecovery(resource);
						}
						catch (Exception exp)
						{
                            jtaLogger.i18NLogger.warn_recovery_getxaresource(exp);
						}
					}
				}
				catch (Exception ex)
				{
                    jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
				}
			}
		}

		return true;
	}

    private boolean resourceInitiatedRecoveryForRecoveryHelpers()
    {
        synchronized (_xaResourceRecoveryHelpers)
        {
            for (XAResourceRecoveryHelper xaResourceRecoveryHelper : _xaResourceRecoveryHelpers)
            {
                try
                {
                    XAResource[] xaResources = xaResourceRecoveryHelper.getXAResources();
                    if (xaResources != null)
                    {
                        for (XAResource xaResource : xaResources)
                        {
                            try
                            {
                                // This calls out to remote systems and may block. Consider using alternate concurrency
                                // control rather than sync on __xaResourceRecoveryHelpers to avoid blocking problems?
                                xaRecovery(xaResource);
                            }
                            catch (Exception ex)
                            {
                                jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
                }
            }
        }

        return true;
    }


	private final boolean xaRecovery(XAResource xares)
	{
		if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("xarecovery of " + xares);
        }

		try
		{
			Xid[] trans = null;

			try
			{
				trans = xares.recover(XAResource.TMSTARTRSCAN);

				if (jtaLogger.logger.isDebugEnabled()) {
                    jtaLogger.logger.debug("Found "
                            + ((trans != null) ? trans.length : 0)
                            + " xids in doubt");
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

				return false;
			}

			RecoveryXids xidsToRecover = null;

			if (_xidScans == null)
				_xidScans = new Hashtable();
			else
			{
                refreshXidScansForEquivalentXAResourceImpl(xares, trans);
                
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

		return true;
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
        if(xids == null || xids.length == 0) {
            return;
        }

        Enumeration keys = _xidScans.keys();

        while (keys.hasMoreElements())
        {
            XAResource theKey = (XAResource) keys.nextElement();
            RecoveryXids recoveryXids = (RecoveryXids) _xidScans.get(theKey);

            if(recoveryXids.updateIfEquivalentRM(xares, xids)) {
                // recoveryXids is for this xares, but was originally obtained using
                // a different XAResource. rekey the hashtable to use the new one.
                _xidScans.remove(theKey);
                theKey = xares;
                _xidScans.put(theKey, recoveryXids);
                break;
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

	private ObjectStore _objStore = TxControl.getStore();

	private InputObjectState _uids = new InputObjectState();

	private final List<XAResourceRecovery> _xaRecoverers;

    private final List<XAResourceRecoveryHelper> _xaResourceRecoveryHelpers = new LinkedList<XAResourceRecoveryHelper>();

    private final List<XAResourceOrphanFilter> _xaResourceOrphanFilters;

    private Hashtable _failures = null;

	private Hashtable _xidScans = null;

	private XARecoveryResourceManager _recoveryManagerClass = null;

	private String _logName = null;

}
