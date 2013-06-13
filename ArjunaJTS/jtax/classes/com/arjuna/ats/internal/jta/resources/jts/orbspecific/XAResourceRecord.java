/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XAResourceRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.resources.jts.orbspecific;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.UNKNOWN;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicRollback;
import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.RecoveryCoordinator;
import org.omg.CosTransactions.RecoveryCoordinatorHelper;
import org.omg.CosTransactions.Vote;

import com.arjuna.ArjunaOTS.OTSAbstractRecord;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.objectstore.ParticipantStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.ClassloadingUtility;
import com.arjuna.ats.internal.jta.recovery.jts.XARecoveryModule;
import com.arjuna.ats.internal.jta.resources.XAResourceErrorHandler;
import com.arjuna.ats.internal.jta.transaction.jts.TransactionImple;
import com.arjuna.ats.internal.jta.utils.jtaxLogger;
import com.arjuna.ats.internal.jta.xa.TxInfo;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.recovery.SerializableXAResourceDeserializer;
import com.arjuna.ats.jta.recovery.XARecoveryResource;
import com.arjuna.ats.jta.resources.EndXAResource;
import com.arjuna.ats.jta.resources.StartXAResource;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.xa.RecoverableXAConnection;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jts.utils.Utility;

public class XAResourceRecord extends com.arjuna.ArjunaOTS.OTSAbstractRecordPOA
{

	public static final int XACONNECTION = 0;

	private static final Uid START_XARESOURCE = Uid.minUid() ;

	private static final Uid END_XARESOURCE = Uid.maxUid() ;

	/**
	 * The params represent specific parameters we need to recreate the
	 * connection to the database in the event of a failure. If they're not set
	 * then recovery is out of our control.
	 *
	 * Could also use it to pass other information, such as the readonly flag.
	 */

	public XAResourceRecord(TransactionImple tx, XAResource res, Xid xid,
			Object[] params)
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.XAResourceRecord ( " + xid + " )");
        }

		_theXAResource = res;
		_recoveryObject = null;
		_tranID = xid;

		_valid = true;

		if (params != null)
		{
			if (params.length >= XACONNECTION)
			{
				if (params[XACONNECTION] instanceof RecoverableXAConnection)
					_recoveryObject = (RecoverableXAConnection) params[XACONNECTION];
			}
		}

		_prepared = false;
		_committed = false;
		_heuristic = TwoPhaseOutcome.FINISH_OK;
		_participantStore = null;
		_theUid = new Uid();
		_theReference = null;
		_recoveryCoordinator = null;

		_theTransaction = tx;

		if (_theXAResource instanceof StartXAResource)
			_cachedUidStringForm = START_XARESOURCE.stringForm();
		else
		{
			if (_theXAResource instanceof EndXAResource)
				_cachedUidStringForm = END_XARESOURCE.stringForm();
		}
	}

	// for recovery only!
	
	public XAResourceRecord ()
	{
	    _theXAResource = null;
            _recoveryObject = null;
            _tranID = null;

            _valid = true;

            _prepared = true;
            _committed = false;
            _heuristic = TwoPhaseOutcome.FINISH_OK;
            _participantStore = null;
            _theUid = new Uid();
            _theReference = null;
            _recoveryCoordinator = null;
	}
	
	public final Uid get_uid()
	{
		return _theUid;
	}

	public final synchronized org.omg.CosTransactions.Resource getResource()
	{
		if (_theReference == null)
		{
			ORBManager.getPOA().objectIsReady(this);

			_theReference = org.omg.CosTransactions.ResourceHelper
					.narrow(ORBManager.getPOA().corbaReference(this));
		}

		return _theReference;
	}

	public final Xid getXid()
	{
		return _tranID;
	}


	public org.omg.CosTransactions.Vote prepare() throws HeuristicMixed,
			HeuristicHazard, org.omg.CORBA.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.prepare for " + _tranID);
        }

		if (!_valid || (_theXAResource == null) || (_tranID == null))
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_nulltransaction("XAResourceRecord.prepare");

		    removeConnection();

		    return Vote.VoteRollback;
		}

		try
		{
			/*
			 * Window of vulnerability versus performance trade-off: if we
			 * create the resource log here then we slow things down in the case
			 * the resource rolls back or returns read only (since we have
			 * written data for no reason and now need to delete it). If we
			 * create the resource log after we know the prepare outcome then
			 * there's a chance we may crash between prepare and writing the
			 * state.
			 *
			 * We go for the latter currently since failures are rare, but
			 * performance is always required. The result is that the
			 * transaction will roll back (since it won't get an ack from
			 * prepare) and the resource won't be recovered. The sys. admin.
			 * will have to clean up manually.
			 *
			 * Actually what will happen in the case of ATS is that the XA
			 * recovery module will eventually roll back this resource when it
			 * notices that there is no log entry for it.
			 */

			if (endAssociation())
			{
				_theXAResource.end(_tranID, XAResource.TMSUCCESS);
			}

			if (_theXAResource.prepare(_tranID) == XAResource.XA_RDONLY)
			{
				removeConnection();

				return Vote.VoteReadOnly;
			}
			else
			{
				if (createState())
					return Vote.VoteCommit;
				else
					return Vote.VoteRollback;
			}
		}
		catch (XAException e1)
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_preparefailed(_theXAResource.toString(),
                    XAHelper.xidToString(_tranID), XAHelper.printXAErrorCode(e1), e1);

			/*
			 * XA_RB*, XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
			 * XAER_PROTO.
			 */

			if (_rollbackOptimization) // won't have rollback called on it
				removeConnection();

			switch (e1.errorCode)
			{
			case XAException.XAER_RMERR:
			case XAException.XAER_RMFAIL:
			case XAException.XA_RBROLLBACK:
			case XAException.XA_RBEND:
			case XAException.XA_RBCOMMFAIL:
			case XAException.XA_RBDEADLOCK:
			case XAException.XA_RBINTEGRITY:
			case XAException.XA_RBOTHER:
			case XAException.XA_RBPROTO:
			case XAException.XA_RBTIMEOUT:
			case XAException.XAER_INVAL:
			case XAException.XAER_PROTO:
			case XAException.XAER_NOTA: // resource may have arbitrarily rolled back (shouldn't, but ...)
				return Vote.VoteRollback;
			default:
				throw new HeuristicHazard(); // we're not really sure (shouldn't get here though).
			}
		}
		catch (Exception e2)
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_preparefailed(_theXAResource.toString(),
                    XAHelper.xidToString(_tranID), "-", e2);

			if (_rollbackOptimization) // won't have rollback called on it
				removeConnection();

			return Vote.VoteRollback;
		}
	}

	public void rollback() throws org.omg.CORBA.SystemException,
			HeuristicCommit, HeuristicMixed, HeuristicHazard
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.rollback for " + _tranID);
        }

		if (_theTransaction != null
				&& _theTransaction.getXAResourceState(_theXAResource) == TxInfo.OPTIMIZED_ROLLBACK)
		{
			/*
			 * Already rolledback during delist.
			 */

			return;
		}

		if (!_valid || (_tranID == null))
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_nulltransaction("XAResourceRecord.rollback");
		}
		else
		{
			if (_theXAResource != null)
			{
				switch (_heuristic)
				{
				case TwoPhaseOutcome.HEURISTIC_HAZARD:
					throw new org.omg.CosTransactions.HeuristicHazard();
				case TwoPhaseOutcome.HEURISTIC_MIXED:
					throw new org.omg.CosTransactions.HeuristicMixed();
				default:
					break;
				}

				try
				{
					if (!_prepared)
					{
						if (endAssociation())
						{
							_theXAResource.end(_tranID, XAResource.TMFAIL);
						}
					}
				}
				catch (XAException e1)
				{
				    if ((e1.errorCode >= XAException.XA_RBBASE)
						&& (e1.errorCode < XAException.XA_RBEND))
				    {
					/*
					 * Has been marked as rollback-only. We still
					 * need to call rollback.
					 */
				    } else if ((e1.errorCode == XAException.XAER_RMERR) || (e1.errorCode == XAException.XAER_RMFAIL)){
				    	    try {
				    	    	    _theXAResource.rollback(_tranID);
				    	    } catch (XAException e2)
				    	    {	
                                		jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_xaerror("XAResourceRecord.rollback",
                                XAHelper.printXAErrorCode(e2), _theXAResource.toString(), XAHelper.xidToString(_tranID), e2);

                                		    removeConnection();

                                		    throw new UNKNOWN();
				    	    }
				    }
				    else
				    {
					removeConnection();

					throw new UNKNOWN();
				    }
				}

				try
				{
					_theXAResource.rollback(_tranID);
				}
				catch (XAException e1)
				{
					if (notAProblem(e1, false))
					{
						// some other thread got there first (probably)
					}
					else
					{
                        jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_xaerror("XAResourceRecord.rollback",
                                XAHelper.printXAErrorCode(e1), _theXAResource.toString(), XAHelper.xidToString(_tranID), e1);

						switch (e1.errorCode)
						{
						case XAException.XAER_RMERR:
							if (!_prepared)
								break; // just do the finally block
						case XAException.XA_HEURHAZ:
							updateState(TwoPhaseOutcome.HEURISTIC_HAZARD);

							throw new org.omg.CosTransactions.HeuristicHazard();
						case XAException.XA_HEURCOM:
							updateState(TwoPhaseOutcome.HEURISTIC_COMMIT);

							throw new org.omg.CosTransactions.HeuristicCommit();
						case XAException.XA_HEURMIX:
							updateState(TwoPhaseOutcome.HEURISTIC_MIXED);

							throw new org.omg.CosTransactions.HeuristicMixed();
						case XAException.XA_HEURRB: // forget?
						case XAException.XA_RBROLLBACK:
						case XAException.XA_RBEND:
						case XAException.XA_RBCOMMFAIL:
						case XAException.XA_RBDEADLOCK:
						case XAException.XA_RBINTEGRITY:
						case XAException.XA_RBOTHER:
						case XAException.XA_RBPROTO:
						case XAException.XA_RBTIMEOUT:
							destroyState();
							break;
						default:
							destroyState();

							if (_prepared)
								throw new org.omg.CosTransactions.HeuristicHazard();
							else
								throw new UNKNOWN();
						}
					}
				}
				catch (Exception e2)
				{
                    jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_generror("XAResourceRecord.rollback",
                            _theXAResource.toString(), XAHelper.xidToString(_tranID), e2);

					throw new UNKNOWN();
				}
				finally
				{
					if (_prepared)
						destroyState();
					else
						removeConnection();
				}
			}
		}
	}

	public void commit() throws org.omg.CORBA.SystemException, NotPrepared,
			HeuristicRollback, HeuristicMixed, HeuristicHazard
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.commit for " + _tranID);
        }

		if (_tranID == null)
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_nulltransaction("XAResourceRecord.commit");
		}
		else
		{
			if ((_theXAResource != null) && (!_committed))
			{
				switch (_heuristic)
				{
				case TwoPhaseOutcome.HEURISTIC_HAZARD:
					throw new org.omg.CosTransactions.HeuristicHazard();
				case TwoPhaseOutcome.HEURISTIC_MIXED:
					throw new org.omg.CosTransactions.HeuristicMixed();
				case TwoPhaseOutcome.HEURISTIC_ROLLBACK:
					throw new org.omg.CosTransactions.HeuristicRollback();
				default:
					break;
				}

				if (!_prepared)
					throw new NotPrepared();

				try
				{
					if (!_committed)
					{
						_committed = true;

						_theXAResource.commit(_tranID, false);

						destroyState();
					}
				}
				catch (XAException e1)
				{
					e1.printStackTrace();

					if (notAProblem(e1, true))
					{
						// some other thread got there first (probably)
						destroyState();
					}
					else
					{
						_committed = false;

                        jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_xaerror("XAResourceRecord.commit",
                                XAHelper.printXAErrorCode(e1), _theXAResource.toString(), XAHelper.xidToString(_tranID), e1);

						/*
						 * XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX,
						 * XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
						 * XAER_PROTO.
						 */

						switch (e1.errorCode)
						{

						case XAException.XA_HEURHAZ:
							updateState(TwoPhaseOutcome.HEURISTIC_HAZARD);

							throw new org.omg.CosTransactions.HeuristicHazard();
						case XAException.XA_HEURCOM:  // what about forget? OTS doesn't support this code here.
							destroyState();
							break;
						case XAException.XA_HEURRB:
						case XAException.XA_RBROLLBACK:
						case XAException.XA_RBCOMMFAIL:
						case XAException.XA_RBDEADLOCK:
						case XAException.XA_RBINTEGRITY:
						case XAException.XA_RBOTHER:
						case XAException.XA_RBPROTO:
						case XAException.XA_RBTIMEOUT:
						case XAException.XA_RBTRANSIENT:
						case XAException.XAER_RMERR:
							updateState(TwoPhaseOutcome.HEURISTIC_ROLLBACK);

							throw new org.omg.CosTransactions.HeuristicRollback();
						case XAException.XA_HEURMIX:
							updateState(TwoPhaseOutcome.HEURISTIC_MIXED);

							throw new org.omg.CosTransactions.HeuristicMixed();

                        case XAException.XAER_NOTA:
                            // RM unexpectedly lost track of the tx, outcome is uncertain
                            updateState(TwoPhaseOutcome.HEURISTIC_HAZARD);
			    			throw new org.omg.CosTransactions.HeuristicHazard();
    					case XAException.XAER_PROTO:
                            // presumed abort (or we could be really paranoid and throw a heuristic)
                            throw new TRANSACTION_ROLLEDBACK();

						case XAException.XA_RETRY:
						case XAException.XAER_RMFAIL:
						    _committed = true;  // remember for recovery later.
							throw new UNKNOWN();  // will cause log to be rewritten.
						case XAException.XAER_INVAL: // resource manager failed, did it rollback?
						default:
							throw new org.omg.CosTransactions.HeuristicHazard();
						}
					}
				}
				catch (Exception e2)
				{
					_committed = false;

                    jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_generror("XAResourceRecord.commit",
                            _theXAResource.toString(), XAHelper.xidToString(_tranID), e2);

					throw new UNKNOWN();
				}
				finally
				{
					removeConnection();
				}
			}
		}
	}

	public org.omg.CosTransactions.Vote prepare_subtransaction()
			throws SystemException
	{
		return Vote.VoteRollback; // shouldn't be possible!
	}

	public void commit_subtransaction(Coordinator parent)
			throws SystemException
	{
		throw new UNKNOWN();
	}

	public void rollback_subtransaction() throws SystemException
	{
		throw new UNKNOWN();
	}

	public int type_id() throws SystemException
	{
		return RecordType.JTAX_RECORD;
	}

	public String uid() throws SystemException
	{
		if (_cachedUidStringForm == null)
			_cachedUidStringForm = _theUid.stringForm();

		return _cachedUidStringForm;
	}

	public boolean propagateOnAbort() throws SystemException
	{
		return false;
	}

	public boolean propagateOnCommit() throws SystemException
	{
		return false; // nesting not supported
	}

	public boolean saveRecord() throws SystemException
	{
		return true;
	}

	public void merge(OTSAbstractRecord record) throws SystemException
	{
	}

	public void alter(OTSAbstractRecord record) throws SystemException
	{
	}

	public boolean shouldAdd(OTSAbstractRecord record) throws SystemException
	{
		return false;
	}

	public boolean shouldAlter(OTSAbstractRecord record) throws SystemException
	{
		return false;
	}

	public boolean shouldMerge(OTSAbstractRecord record) throws SystemException
	{
		return false;
	}

	public boolean shouldReplace(OTSAbstractRecord record)
			throws SystemException
	{
		return false;
	}

	/**
	 * Is the XAException a non-error when received in reply to commit or
	 * rollback? It normally is, but may be overridden in recovery.
	 */

	protected boolean notAProblem(XAException ex, boolean commit)
	{
		return XAResourceErrorHandler.notAProblem(_theXAResource, ex, commit);
	}

	/**
	 * For commit_one_phase we can do whatever we want since the transaction
	 * outcome is whatever we want. Therefore, we do not need to save any
	 * additional recoverable state, such as a reference to the transaction
	 * coordinator, since it will not have an intentions list anyway.
	 */

	public void commit_one_phase() throws HeuristicHazard,
			org.omg.CORBA.SystemException
	{
	    if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.commit_one_phase for " + _tranID);
        }

	    if (_tranID == null)
	    {
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_nulltransaction("XAResourceRecord.commit_one_phase");

	        throw new TRANSACTION_ROLLEDBACK();
	    }
	    else
	    {
	        if (_theXAResource != null)
	        {
	            try
	            {
	                switch (_heuristic)
	                {
	                case TwoPhaseOutcome.HEURISTIC_HAZARD:
	                    throw new org.omg.CosTransactions.HeuristicHazard();
	                default:
	                    break;
	                }

	                /*
	                 * TODO in Oracle, the end is not required. Is this
	                 * common to other RM implementations?
	                 */

	                boolean commit = true;

	                try
	                {
	                    if (endAssociation())
	                    {
	                        _theXAResource.end(_tranID, XAResource.TMSUCCESS);
	                    }
	                }
	                catch (XAException e1)
	                {
	                    /*
	                     * Now it's not legal to return a heuristic from end, but
	                     * apparently Oracle does (http://jira.jboss.com/jira/browse/JBTM-343)
	                     * Since this is 1PC we can call forget: the outcome of the
	                     * transaction is the outcome of the participant.
	                     */

	                    switch (e1.errorCode)
	                    {
	                    case XAException.XA_HEURHAZ:
	                    case XAException.XA_HEURMIX:
	                    case XAException.XA_HEURCOM:
	                    case XAException.XA_HEURRB:
	                        throw e1;
	                    case XAException.XA_RBROLLBACK:
	                    case XAException.XA_RBCOMMFAIL:
	                    case XAException.XA_RBDEADLOCK:
	                    case XAException.XA_RBINTEGRITY:
	                    case XAException.XA_RBOTHER:
	                    case XAException.XA_RBPROTO:
	                    case XAException.XA_RBTIMEOUT:
	                    case XAException.XA_RBTRANSIENT:
	                        /*
	                         * Has been marked as rollback-only. We still
	                         * need to call rollback.
	                         */

	                        commit = false;
	                        break;
	                    case XAException.XAER_RMERR:
	                    case XAException.XAER_NOTA:
	                    case XAException.XAER_PROTO:
	                    case XAException.XAER_INVAL:
	                    case XAException.XAER_RMFAIL:
	                    default:
	                    {
	                        throw new UNKNOWN();
	                    }
	                    }
	                }

	                _theXAResource.commit(_tranID, true);
	            }
	            catch (XAException e1)
	            {
	                /*
	                 * XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX,
	                 * XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
	                 * XAER_PROTO. XA_RB*
	                 */

	                if ((e1.errorCode >= XAException.XA_RBBASE)
	                        && (e1.errorCode <= XAException.XA_RBEND))
	                {
	                    throw new TRANSACTION_ROLLEDBACK();
	                }

	                switch (e1.errorCode)
	                {
	                case XAException.XA_HEURHAZ:
	                case XAException.XA_HEURMIX:
	                    updateState(TwoPhaseOutcome.HEURISTIC_HAZARD);

	                    throw new org.omg.CosTransactions.HeuristicHazard();
	                case XAException.XA_HEURCOM:
	                    handleForget() ;
	                    break;
	                case XAException.XA_HEURRB:
	                       handleForget() ;
	                       throw new TRANSACTION_ROLLEDBACK();
	                case XAException.XA_RBROLLBACK:
	                case XAException.XA_RBCOMMFAIL:
	                case XAException.XA_RBDEADLOCK:
	                case XAException.XA_RBINTEGRITY:
	                case XAException.XA_RBOTHER:
	                case XAException.XA_RBPROTO:
	                case XAException.XA_RBTIMEOUT:
	                case XAException.XA_RBTRANSIENT:
	                case XAException.XAER_RMERR:
	                    throw new TRANSACTION_ROLLEDBACK();
	                case XAException.XAER_NOTA:
	                    // RM unexpectedly lost track of the tx, outcome is uncertain
	                    updateState(TwoPhaseOutcome.HEURISTIC_HAZARD);
	                    throw new org.omg.CosTransactions.HeuristicHazard();
	                case XAException.XAER_PROTO:
	                case XAException.XA_RETRY:  // not allowed to be thrown here by XA specification!
	                    // presumed abort (or we could be really paranoid and throw a heuristic)
	                    throw new TRANSACTION_ROLLEDBACK();

	                case XAException.XAER_INVAL: // resource manager failed, did it rollback?
	                    throw new org.omg.CosTransactions.HeuristicHazard();
	                case XAException.XAER_RMFAIL:
	                default:
	                    _committed = true;  // will cause log to be rewritten

	                throw new UNKNOWN();
	                }
	            }
	            catch (SystemException ex)
	            {
	                ex.printStackTrace();

	                throw ex;
	            }
	            catch (org.omg.CosTransactions.HeuristicHazard ex)
	            {
	                throw ex;
	            }
	            catch (Exception e2)
	            {
                    jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_coperror(e2);

	                throw new UNKNOWN();
	            }
	            finally
	            {
	                removeConnection();
	            }
	        }
	        else
	            throw new TRANSACTION_ROLLEDBACK();
	    }
	}

	public void forget() throws org.omg.CORBA.SystemException
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.forget for " + _tranID);
        }

		handleForget() ;

		destroyState();

		removeConnection();
	}

	private void handleForget()
	{
		if ((_theXAResource != null) && (_tranID != null))
		{
		    _heuristic = TwoPhaseOutcome.FINISH_OK;
		    
			try
			{
				_theXAResource.forget(_tranID);
			}
			catch (Exception e)
			{
			}
		}
	}

	public boolean saveState(OutputObjectState os)
	{
		boolean res = false;

		try
		{
			os.packInt(_heuristic);
			os.packBoolean(_committed);
			
			/*
			 * Since we don't know what type of Xid we are using, leave it up to
			 * XID to pack.
			 */

			XidImple.pack(os, _tranID);

			/*
			 * If no recovery object set then rely upon object serialisation!
			 */

			if (_recoveryObject == null)
			{
				os.packInt(RecoverableXAConnection.OBJECT_RECOVERY);

                                if (_theXAResource instanceof Serializable)
                                {
        				try
        				{
        					ByteArrayOutputStream s = new ByteArrayOutputStream();
        					ObjectOutputStream o = new ObjectOutputStream(s);

        					o.writeObject(_theXAResource);
        					o.close();

        					os.packBoolean(true);
        					
                            String name = _theXAResource.getClass().getName();
                            os.packString(name);

        					os.packBytes(s.toByteArray());
        				}
        				catch (NotSerializableException ex)
        				{
                            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_saveState();

        				    return false;
        				}
                                }
                                else
                                {
                                    // have to rely upon XAResource.recover!

                                    os.packBoolean(false);
                                }
			}
			else
			{
				os.packInt(RecoverableXAConnection.AUTO_RECOVERY);
				os.packString(_recoveryObject.getClass().getName());

				_recoveryObject.packInto(os);
			}

			if (_recoveryCoordinator == null)
				os.packBoolean(false);
			else
			{
				os.packBoolean(true);

				String ior = ORBManager.getORB().orb().object_to_string(
						_recoveryCoordinator);

				os.packString(ior);

				ior = null;
			}

			res = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			res = false;
		}

		return res;
	}

	public boolean restoreState(InputObjectState os)
	{
		boolean res = false;

		try
		{
			_heuristic = os.unpackInt();
			_committed = os.unpackBoolean();
			
			_tranID = XidImple.unpack(os);

			_theXAResource = null;
			_recoveryObject = null;

			if (os.unpackInt() == RecoverableXAConnection.OBJECT_RECOVERY)
			{
				boolean haveXAResource = os.unpackBoolean();

				if (haveXAResource)
				{
					try
					{
						// Read the classname of the serialized XAResource
						String className = os.unpackString();

						byte[] b = os.unpackBytes();

						ByteArrayInputStream s = new ByteArrayInputStream(b);
						ObjectInputStream o = new ObjectInputStream(s);

						// Give the list of deserializers a chance to deserialize the record
						boolean deserialized = false;
						Iterator<SerializableXAResourceDeserializer> iterator = getXAResourceDeserializers().iterator();
						while (iterator.hasNext()) {
							SerializableXAResourceDeserializer proxyXAResourceDeserializer = iterator.next();
							if (proxyXAResourceDeserializer.canDeserialze(className)) {
								_theXAResource = proxyXAResourceDeserializer.deserialze(o);
								deserialized = true;
                                break;
							}
						}

						// Give it a go ourselves
						if (!deserialized) {
							_theXAResource = (XAResource) o.readObject();
						}

						o.close();

						if (jtaxLogger.logger.isTraceEnabled()) {
                            jtaxLogger.logger.trace("XAResourceRecord.restore_state - XAResource de-serialized");
                        }
					}
					catch (Exception ex)
					{
						// not serializable in the first place!

                        jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_restoreerror1(ex);

						return false;
					}
				}
			}
			else
			{
				String creatorName = os.unpackString();

                _recoveryObject = ClassloadingUtility.loadAndInstantiateClass(RecoverableXAConnection.class, creatorName, null);
                if(_recoveryObject == null) {
                    throw new ClassNotFoundException();
                }

				_recoveryObject.unpackFrom(os);
				_theXAResource = _recoveryObject.getResource();

				if (_theXAResource == null)
				{
                    jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_norecoveryxa(XAHelper.xidToString(_tranID));
				}

				if (jtaxLogger.logger.isTraceEnabled()) {
                    jtaxLogger.logger.trace("XAResourceRecord.restore_state - XAResource got from "
                            + creatorName);
                }
			}

			boolean haveRecCoord = os.unpackBoolean();

			if (haveRecCoord)
			{
				String ior = os.unpackString();

				if (ior == null)
					return false;
				else
				{
					org.omg.CORBA.Object objRef = ORBManager.getORB().orb()
							.string_to_object(ior);

					_recoveryCoordinator = RecoveryCoordinatorHelper
							.narrow(objRef);
				}
			}
			else
				_recoveryCoordinator = null;

			res = true;
		}
		catch (Exception e)
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_restoreerror2(e);

			res = false;
		}

		return res;
	}

	public String type()
	{
		return XAResourceRecord.typeName();
	}

	public static String typeName()
	{
		return "/CosTransactions/XAResourceRecord";
	}

	public final void setRecoveryCoordinator(RecoveryCoordinator recCoord)
	{
		_recoveryCoordinator = recCoord;
	}

	/**
	 * @deprecated Only used in tests
	 */
	public final RecoveryCoordinator getRecoveryCoordinator()
	{
		return _recoveryCoordinator;
	}
	
	public String toString ()
	{
	    return "XAResourceRecord < resource:"+_theXAResource+", txid:"+_tranID+", heuristic"+TwoPhaseOutcome.stringForm(_heuristic)+" "+super.toString()+" >";
	}

	protected XAResourceRecord(Uid u)
	{
		_theXAResource = null;
		_recoveryObject = null;
		_tranID = null;
		_prepared = true;
		_committed = false;
		_heuristic = TwoPhaseOutcome.FINISH_OK;
		_theUid = new Uid(u);
		_participantStore = null;
		_valid = false;
		_theReference = null;
		_recoveryCoordinator = null;
		_theTransaction = null;

		_valid = loadState();
	}

	/**
	 * For those objects where the original XAResource could not be saved.
	 */

	protected synchronized void setXAResource(XAResource res)
	{
		_theXAResource = res;
	}

	protected int recover()
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.recover");
        }

		if (_valid)
		{
			org.omg.CosTransactions.Status s = org.omg.CosTransactions.Status.StatusUnknown;

			try
			{
				// force tx to rollback if not prepared

				s = _recoveryCoordinator.replay_completion(getResource());
			}
			catch (OBJECT_NOT_EXIST ex)
			{
				// no coordinator, so presumed abort unless we have better information.

			    if (_committed)
			        s = org.omg.CosTransactions.Status.StatusCommitted;
			    else
				s = org.omg.CosTransactions.Status.StatusRolledBack;
			}
			catch (NotPrepared ex1)
			{
                jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_notprepared("XAResourceRecord");

				return XARecoveryResource.TRANSACTION_NOT_PREPARED;
			}
			catch (java.lang.NullPointerException ne)
			{
				/*
				 * No recovery coordinator!
				 */
			}
			catch (Exception e)
			{
				/*
				 * Unknown error, so better to do nothing at this stage.
				 */

                jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_unexpected("XAResourceRecord", e);

				return XARecoveryResource.FAILED_TO_RECOVER;
			}

			if (jtaxLogger.logger.isTraceEnabled()) {
                jtaxLogger.logger.trace("XAResourceRecord.recover got status: "
                        + Utility.stringStatus(s));
            }

			boolean doCommit = false;

			switch (s.value())
			{
			case org.omg.CosTransactions.Status._StatusUnknown:
				// some problem occurred

				return XARecoveryResource.FAILED_TO_RECOVER;
			case org.omg.CosTransactions.Status._StatusMarkedRollback:
			case org.omg.CosTransactions.Status._StatusRollingBack:
				// we should be told eventually, so wait

				return XARecoveryResource.WAITING_FOR_RECOVERY;
			case org.omg.CosTransactions.Status._StatusCommitted:

				doCommit = true;
				break;
			case org.omg.CosTransactions.Status._StatusRolledBack:
			case org.omg.CosTransactions.Status._StatusNoTransaction:
				// presumed abort

				doCommit = false;
				break;
			case org.omg.CosTransactions.Status._StatusCommitting:
				// leave it for now as we'll be driven top-down soon

				return XARecoveryResource.WAITING_FOR_RECOVERY;
			default:
				// wait

				return XARecoveryResource.FAILED_TO_RECOVER;
			}

			return doRecovery(doCommit);
		}

		return XARecoveryResource.FAILED_TO_RECOVER;
	}

	private final void setObjectStore()
	{
		if (_participantStore == null)
		    _participantStore = StoreManager.getParticipantStore();
	}

	private final boolean createState()
	{
		setObjectStore();

		if ((_theXAResource != null) && (_tranID != null)
				&& (_participantStore != null))
		{
			OutputObjectState os = new OutputObjectState();

			if (saveState(os))
			{
				try
				{
					_valid = _participantStore.write_committed(_theUid, type(), os);
					_prepared = true;
				}
				catch (Exception e)
				{
                    jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_createstate();

					_valid = false;
				}
			}
			else
				_valid = false;
		}
		else
			_valid = false;

		return _valid;
	}

	private final boolean updateState(int h)
	{
		setObjectStore();

		if (_prepared) // only need do if we have prepared
		{
			OutputObjectState os = new OutputObjectState();

			_heuristic = h;

			if (saveState(os))
			{
				try
				{
					_valid = _participantStore.write_committed(_theUid, type(), os);
				}
				catch (Exception e)
				{
					_valid = false;
				}
			}
			else
			{
                jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_updatestate();

				_valid = false;
			}
		}

		return _valid;
	}

	private final boolean loadState()
	{
		setObjectStore();

		InputObjectState os = null;

		try
		{
			os = _participantStore.read_committed(_theUid, type());
		}
		catch (Exception e)
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_loadstateread(e);

			os = null;
		}

		if (os != null)
		{
			_valid = restoreState(os);

			os = null;
		}
		else
			_valid = false;

		return _valid;
	}

	private final boolean destroyState()
	{
		setObjectStore();

		if (_prepared && _valid)
		{
			try
			{
				_valid = _participantStore.remove_committed(_theUid, type());
			}
			catch (Exception e)
			{
				e.printStackTrace();

				_valid = false;
			}
		}

		if (_recoveryObject != null)
			removeConnection();

		return _valid;
	}

	private final void removeConnection()
	{
		/*
		 * Should only be called once. Remove the connection so that user can
		 * reuse the driver as though it were fresh (e.g., can do read only
		 * optimisation).
		 */

		if (_recoveryObject != null)
		{
			_recoveryObject.close();
			_recoveryObject = null;
		}

		if (_theTransaction != null)
			_theTransaction = null;

		try
		{
			if (_theReference != null)
			{
				ORBManager.getPOA().shutdownObject(this);
				_theReference = null;
			}
		}
		catch (Exception e)
		{
            jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_remconn(e);
		}
	}

	private final int doRecovery(boolean commit)
	{
		if (jtaxLogger.logger.isTraceEnabled()) {
            jtaxLogger.logger.trace("XAResourceRecord.doRecovery ( " + commit + " )");
        }

		int result = XARecoveryResource.FAILED_TO_RECOVER;

		if ((_theXAResource != null) && (_tranID != null))
		{
			try
			{
				if (commit) {
                    jtaxLogger.i18NLogger.info_jtax_recovery_jts_orbspecific_commit(XAHelper.xidToString(_tranID));
					commit();
                } else {
                    jtaxLogger.i18NLogger.info_jtax_recovery_jts_orbspecific_rollback(XAHelper.xidToString(_tranID));
					rollback();
                }
				// if those succeed, they will have removed any persistent state

				result = XARecoveryResource.RECOVERED_OK;
			}
			catch (Exception e2)
			{
                jtaxLogger.i18NLogger.warn_jtax_resources_jts_orbspecific_recfailed(((commit) ? "commit" : "rollback"), e2);
			}
		}

		return result;
	}

	/*
	 * Ask the transaction whether or not this XAResource is still associated
	 * with the thread, i.e., has end already been called on it?
	 */

	private final boolean endAssociation()
	{
		boolean doEnd = true;

		if (_theTransaction != null)
		{
			if (_theTransaction.getXAResourceState(_theXAResource) == TxInfo.NOT_ASSOCIATED)
			{
				// end has been called so we don't need to do it again!

				doEnd = false;
			}
		}
		else
			doEnd = false; // recovery mode

		return doEnd;
	}

    private List<SerializableXAResourceDeserializer> getXAResourceDeserializers() {
        if (serializableXAResourceDeserializers != null) {
            return serializableXAResourceDeserializers;
        }
        synchronized (this) {
            if (serializableXAResourceDeserializers != null) {
                return serializableXAResourceDeserializers;
            }
            serializableXAResourceDeserializers = new ArrayList<SerializableXAResourceDeserializer>();
            for (RecoveryModule recoveryModule : RecoveryManager.manager().getModules()) {
                if (recoveryModule instanceof XARecoveryModule) {
                    XARecoveryModule xaRecoveryModule = (XARecoveryModule) recoveryModule;
                    serializableXAResourceDeserializers.addAll(xaRecoveryModule.getSeriablizableXAResourceDeserializers());
                    return serializableXAResourceDeserializers;
                }
            }

        }
        return serializableXAResourceDeserializers;
    }
	
	protected XAResource _theXAResource;

	private RecoverableXAConnection _recoveryObject;
	private Xid _tranID;
	private boolean _prepared;
	private boolean _committed;
	private boolean _valid;
	private int _heuristic;
	private ParticipantStore _participantStore;
	private Uid _theUid;
	private org.omg.CosTransactions.Resource _theReference;
	private org.omg.CosTransactions.RecoveryCoordinator _recoveryCoordinator;
	private TransactionImple _theTransaction;

	// cached variables

	private String _cachedUidStringForm;

	private static boolean _rollbackOptimization = jtaPropertyManager.getJTAEnvironmentBean().isXaRollbackOptimization();
	
    private List<SerializableXAResourceDeserializer> serializableXAResourceDeserializers;
}
