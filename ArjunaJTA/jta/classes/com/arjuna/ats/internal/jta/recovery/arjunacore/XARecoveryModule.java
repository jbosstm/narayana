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
import java.util.Collections;
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
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StateStatus;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.ExtendedRecoveryModule;
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
import com.arjuna.ats.jta.utils.XARecoveryResourceHelper;

import com.arjuna.ats.jta.xa.XATxConverter;
import org.jboss.tm.XAResourceWrapper;

/**
 * Designed to be able to recover any XAResource.
 */

public class XARecoveryModule implements ExtendedRecoveryModule
{

	public XARecoveryModule()
	{
		this(new com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryResourceManagerImple(),
                "Local XARecoveryModule");

		com.arjuna.ats.internal.jta.Implementations.initialise();
	}

    public Set<String> getContactedJndiNames() {
        return Collections.unmodifiableSet(contactedJndiNames);
    }

    @Override
    public boolean isPeriodicWorkSuccessful() {
        return !jtaLogger.isRecoveryProblems();
    }

    public void addXAResourceRecoveryHelper(XAResourceRecoveryHelper xaResourceRecoveryHelper) {
        _xaResourceRecoveryHelpers.add(xaResourceRecoveryHelper);
    }

    public synchronized void removeXAResourceRecoveryHelper(XAResourceRecoveryHelper xaResourceRecoveryHelper) {
        switch (getScanState()) {
            case FIRST_PASS:
                waitForNotScanState(ScanStates.FIRST_PASS);
                this.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);
                return;
            case SECOND_PASS:
                if (isHelperInUse(xaResourceRecoveryHelper)) {
                    waitForScanState(ScanStates.IDLE);
                    // we are idle now and we can reiterate the call
                    this.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);
                    return;
                }
                // although in the middle of the second pass we know the XAResources
                //   are not originated from the helper, we are safe to remove them
                break;
            case BETWEEN_PASSES:
                // if we are in the between passes we still be sure the XAResources
                //  are not originated from the helper we are going to remove
                if (isHelperInUse(xaResourceRecoveryHelper)) {
                    waitForScanState(ScanStates.IDLE);
                    // we are idle now and we can reiterate the call
                    this.removeXAResourceRecoveryHelper(xaResourceRecoveryHelper);
                    return;
                }

                // XAResources managed by "the recovery helper to be removed" are not active
                //   we can finish them by simulating the second pass recovery, then removing the helper
                XAResource[] xaResources = recoveryHelpersXAResource.get(xaResourceRecoveryHelper);
                if (xaResources != null) {
                    for (XAResource xaResource : xaResources) {
						String jndiName = null;
						if(xaResource instanceof XAResourceWrapper) {
							jndiName = ((XAResourceWrapper)xaResource).getJndiName();
						}
						NameScopedXAResource nameScopedXAResource = new NameScopedXAResource(xaResource, jndiName);
                        xaRecoverySecondPass(nameScopedXAResource);
                        _resources.remove(nameScopedXAResource);
                    }
                } else {
                    jtaLogger.logger.debugf("nothing to remove - xa resources of recovery helper '%s' were null",
                            xaResourceRecoveryHelper);
                }
                break;
            case IDLE:
                // the periodic recovery is sleeping, we can remove helper safely
                break;
        }

        _xaResourceRecoveryHelpers.remove(xaResourceRecoveryHelper);
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

	public synchronized void periodicWorkFirstPass() {
		periodicWorkFirstPass(ScanStates.BETWEEN_PASSES);
	}

	private synchronized void periodicWorkFirstPass(ScanStates endState)
	{
		// JBTM-1354 JCA needs to be able to recover XAResources associated with a subordinate transaction so we have to do at least
		// the start scan to make sure that we have loaded all the XAResources we possibly can to assist subordinate transactions recovering
		// the reason we can't do bottom up recovery is if this server has an XAResource which tries to recover a remote server (e.g. distributed JTA)
		// then we get deadlock on the secondpass
		if (getScanState() == ScanStates.BETWEEN_PASSES) {
			periodicWorkSecondPass();
			endState = ScanStates.BETWEEN_PASSES; // Ensure if originally we are between periodic recovery scans we continue in that state and leave XAResource in STARTRSCAN
		}

		setScanState(ScanStates.FIRST_PASS);

        if(jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debugv("{0} - first pass", _logName);
        }

        contactedJndiNames.clear();
        jtaLogger.setRecoveryProblems(false);

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

		List<NameScopedXAResource> resources = new ArrayList<>(_resources);
		for (NameScopedXAResource xaResource : resources) {
			try {
				xaRecoveryFirstPass(xaResource);
			} catch (Exception ex) {
				jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
			}
		}

		if (endState != ScanStates.BETWEEN_PASSES) {
			for (NameScopedXAResource xaResource : resources) {
				try {
					xaResource.getXaResource().recover(XAResource.TMENDRSCAN);
				} catch (Exception ex) {
					jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
				}
			}
        }

		setScanState(endState);
	}

	public synchronized void periodicWorkSecondPass()
	{
	    if (getScanState() == ScanStates.IDLE) {
	        // a call to getNewXAResource must have already ran the second pass so it is safe to return
			return;
		}

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

 	public static XARecoveryModule getRegisteredXARecoveryModule () {
 		if (registeredXARecoveryModule == null) {
			RecoveryManager recMan = RecoveryManager.manager();
			Vector<RecoveryModule> recoveryModules = recMan.getModules();

			if (recoveryModules != null) {
				Enumeration<RecoveryModule> modules = recoveryModules.elements();

				while (modules.hasMoreElements()) {
					RecoveryModule m = modules.nextElement();

					if (m instanceof XARecoveryModule) {
						registeredXARecoveryModule = (XARecoveryModule) m;
						break;
					}
				}
			}
		}
		return registeredXARecoveryModule;
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
	private XAResource getNewXAResource(Xid xid, String jndiName)
	{
		NameScopedXid key = new NameScopedXid(xid, jndiName);

		XAResource toReturn = getTheKey(key);

		if (toReturn == null) {
			synchronized (this) {
				/*
				 * run an xid scan with the lock held to avoid _xidScans being changed
				 * after the call to periodicWorkFirstPass but before the call to getTheKey
				 */
				periodicWorkFirstPass(ScanStates.IDLE);
				toReturn = getTheKey(key);
			}
		}

		return toReturn;
    }

    private XAResource getTheKey(NameScopedXid scopedXid) {
		if (_xidScans != null)
		{
			Enumeration<NameScopedXAResource> keys = _xidScans.keys();

			while (keys.hasMoreElements())
			{
				NameScopedXAResource theKey = keys.nextElement();

				// In cases where the xid originally came from named source,
				// only  a replacement from the same source is allowed.
				// This deals with inflow cases where the xid by itself is non-unique,
				// but the <xid,jndiName> tuple is unique.
				// BUT...
				// It also breaks legacy cross-origin recovery configurations, where N datasources point to the same RM
				// and one could be used to recover on behalf of the others. So, where it's a JTA tx we know will
				// have uniq branches (see TransactionImple::createXid) we ignore the names to preserve compatibility.
				if(USE_JNDI_NAME && scopedXid.getXid().getFormatId() != XATxConverter.FORMAT_ID && !scopedXid.isAnonymous() && !scopedXid.isSameName(theKey)) {
					continue;
				}

				RecoveryXids xids = _xidScans.get(theKey);

				// JBTM-1255 moved stale check back to bottomUpRecovery
				if (xids.contains(scopedXid.getXid())) {
					// This Xid is going to be recovered by the AtomicAction
					// it is possible that the Xid is recovered by both txbridge and XATerminator - the second
					// would get noxaresource error message
					xids.remove(scopedXid.getXid());
					return theKey.getXaResource();
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
		if (jtaLogger.logger.isTraceEnabled()) {
			jtaLogger.logger.trace("trying getNewXAResource for "+xaResourceRecord);
		}
        return getNewXAResource(xaResourceRecord.getXid(), xaResourceRecord.getJndiName());
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

                                        jtaLogger.i18NLogger.info_recovery_recoverydelayed(theUid, XARecoveryResourceHelper.stringForm(recoveryStatus));
									}
									else
									{
                                        jtaLogger.i18NLogger.warn_recovery_recoveryfailed(theUid, XARecoveryResourceHelper.stringForm(recoveryStatus));
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
	 * JBTM-895 garbage collection is now done when we return XAResources @see XARecoveryModule#getNewXAResource(XAResourceRecord)
	 * @see XARecoveryModule#getNewXAResource(XAResourceRecord)
	 */
    private void bottomUpRecovery() {
			for (NameScopedXAResource xaResource : _resources) {
				try {
					xaRecoverySecondPass(xaResource);
				} catch (Exception ex) {
					jtaLogger.i18NLogger.warn_recovery_getxaresource(ex);
				}
			}


        // JBTM-895 garbage collection is now done when we return XAResources {@see XARecoveryModule#getNewXAResource(XAResourceRecord)}
        // JBTM-924 requires this here garbage collection, see JBTM-1155:
        if (_xidScans != null) {
            Set<NameScopedXAResource> keys = new HashSet<>(_xidScans.keySet());
            for(NameScopedXAResource theKey : keys) {
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

	private final List<NameScopedXAResource> resourceInitiatedRecovery()
	{
		/*
		 * Now any additional connections we may need to create. Relies upon
		 * information provided by the application.
		 */

		List<NameScopedXAResource> xaresources = new ArrayList<>();
		if (_xaRecoverers.size() > 0)
		{
			for (int i = 0; i < _xaRecoverers.size(); i++)
			{
				try
				{
					XAResourceRecovery ri = _xaRecoverers.get(i);

					while (ri.hasMoreResources())
					{
						XAResource xaResource = ri.getXAResource();

						String jndiName = null;
						if(xaResource instanceof XAResourceWrapper) {
							jndiName = ((XAResourceWrapper)xaResource).getJndiName();
						}

						xaresources.add(new NameScopedXAResource(xaResource, jndiName));
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

    private List<NameScopedXAResource> resourceInitiatedRecoveryForRecoveryHelpers()
    {
		List<NameScopedXAResource> xaresources = new ArrayList<>();

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
						String jndiName = null;
						if(xaResource instanceof XAResourceWrapper) {
							jndiName = ((XAResourceWrapper)xaResource).getJndiName();
						}

                        xaresources.add(new NameScopedXAResource(xaResource, jndiName));
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


	private final void xaRecoveryFirstPass(NameScopedXAResource xares)
	{
		if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("xarecovery of " + xares);
        }

		
			Xid[] trans = null;

			try
			{
				trans = xares.getXaResource().recover(XAResource.TMSTARTRSCAN);

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
					xares.getXaResource().recover(XAResource.TMENDRSCAN);
				}
				catch (Exception e1)
				{
				}

				if (_xidScans != null)
					_xidScans.remove(xares);

				return;
			}

			RecoveryXids xidsToRecover = null;

			if (_xidScans == null)
				_xidScans = new Hashtable<>();
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
            saveContactedJndiName(xares.getJndiName());
	}
	
	private void xaRecoverySecondPass(NameScopedXAResource xares) {

		if (jtaLogger.logger.isDebugEnabled()) {
            jtaLogger.logger.debug("xarecovery second pass of " + xares);
        }
		
		RecoveryXids xidsToRecover = _xidScans.get(xares);
		if (xidsToRecover != null) {
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
										.getResource(recordUid, xares.getXaResource());
								int recoveryStatus = record.recover();
	
								if (recoveryStatus != XARecoveryResource.RECOVERED_OK)
								{
	                                jtaLogger.i18NLogger.warn_recovery_failedtorecover(_logName+".xaRecovery", XARecoveryResourceHelper.stringForm(recoveryStatus));
								}
	
								removeFailure(record.getXid(), record.get_uid());
							}
	
							if (doForget)
							{
								try
								{
									xares.getXaResource().forget(xids[j]);
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
					xares.getXaResource().recover(XAResource.TMENDRSCAN);
			}
			catch (XAException e)
			{
	            jtaLogger.i18NLogger.warn_recovery_xarecovery1(_logName+".xaRecovery", XAHelper.printXAErrorCode(e), e);
			}
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
    private boolean handleOrphan(NameScopedXAResource xares, Xid xid)
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

                xares.getXaResource().rollback(xid);
            }
        }
        catch (XAException e1)
        {
            if(e1.errorCode == XAException.XAER_NOTA)
            {
                if(jtaLogger.logger.isDebugEnabled()) {
                    jtaLogger.logger.debug(_logName+".xaRecovery: XAER_NOTA received while rolling back " + XAHelper.xidToString(xid));
                }
            }
            else
            {
                jtaLogger.i18NLogger.warn_recovery_xarecovery1(_logName+".xaRecovery", XAHelper.printXAErrorCode(e1), e1);
            }

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
    private void refreshXidScansForEquivalentXAResourceImpl(NameScopedXAResource xares, Xid[] xids)
    {
        Set<NameScopedXAResource> keys = new HashSet<>(_xidScans.keySet());

        for(NameScopedXAResource theKey : keys) {
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
            	XAResource xaResource = xaResources[i];
				String jndiName = null;
				if(xaResource instanceof XAResourceWrapper) {
					jndiName = ((XAResourceWrapper)xaResource).getJndiName();
				}
                RecoveryXids recoveryXids = _xidScans.get(new NameScopedXAResource(xaResource, jndiName));
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
                this.wait();
            } while (!getScanState().equals(state));

            return true;
        } catch (InterruptedException e) {
            jtaLogger.i18NLogger.warn_intteruptedExceptionOnWaitingXARecoveryModuleLock(this, state.name(), e);
            return false;
        }
    }
    private boolean waitForNotScanState(ScanStates state) {
        try {
			while (getScanState().equals(state)) {
				this.wait();
			} 

            return true;
        } catch (InterruptedException e) {
            jtaLogger.i18NLogger.warn_intteruptedExceptionOnWaitingXARecoveryModuleLock(this, state.name(), e);
            return false;
        }
    }

    /**
     * Update the status of the scanner
     * @param state the new state
     */
    private synchronized void setScanState(ScanStates state) {
        jtaLogger.logger.debugf("XARecoveryModule state change %s->%s%n", getScanState(), state);
        scanState.set(state.ordinal());
        this.notifyAll();
    }

    private ScanStates getScanState() {
        return ScanStates.values()[scanState.get()];
    }

    private void saveContactedJndiName(final String jndiName) {
        if (jndiName != null && jndiName.length() > 0) {
            contactedJndiNames.add(jndiName);
        }
    }

    private RecoveryStore _recoveryStore = StoreManager.getRecoveryStore();

	private InputObjectState _uids = new InputObjectState();

	private List<NameScopedXAResource> _resources;

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

	private Hashtable<NameScopedXAResource,RecoveryXids> _xidScans = null;

	private XARecoveryResourceManager _recoveryManagerClass = null;

	private String _logName = null;

	private List<SerializableXAResourceDeserializer> _seriablizableXAResourceDeserializers = new ArrayList<SerializableXAResourceDeserializer>();

    private Set<String> contactedJndiNames = new HashSet<String>();

	private static XARecoveryModule registeredXARecoveryModule;

	public static final boolean USE_JNDI_NAME = Boolean.getBoolean("tx-recover-non-unique-xids");
}
