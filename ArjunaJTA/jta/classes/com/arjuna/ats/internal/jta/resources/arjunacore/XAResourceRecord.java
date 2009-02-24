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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XAResourceRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.resources.arjunacore;

import com.arjuna.ats.jta.recovery.*;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.common.Environment;

import com.arjuna.ats.jta.xa.RecoverableXAConnection;
import com.arjuna.ats.jta.xa.XidImple;
import com.arjuna.ats.jta.utils.XAHelper;
import com.arjuna.ats.jta.logging.*;
import com.arjuna.ats.jta.resources.StartXAResource;
import com.arjuna.ats.jta.resources.EndXAResource;

import com.arjuna.common.util.logging.*;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple;
import com.arjuna.ats.internal.jta.xa.TxInfo;
import com.arjuna.ats.internal.jta.resources.XAResourceErrorHandler;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.coordinator.AbstractRecord;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.arjuna.coordinator.RecordType;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import java.util.Vector;
import java.util.Enumeration;

import java.io.*;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;

import javax.transaction.xa.XAException;

/**
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: XAResourceRecord.java 2342 2006-03-30 13:06:17Z $
 * @since JTS 1.2.4.
 *
 * @message com.arjuna.ats.internal.jta.resources.arjunacore.noresource
 *          [com.arjuna.ats.internal.jta.resources.arjunacore.noresource]
 *          No XAResource to recover {0}
 * @message com.arjuna.ats.internal.jta.resources.arjunacore.assumecomplete
 *          [com.arjuna.ats.internal.jta.resources.arjunacore.assumecomplete]
 *          Being told to assume complete on Xid {0}
 */

public class XAResourceRecord extends AbstractRecord
{

	public static final int XACONNECTION = 0;

	private static final Uid START_XARESOURCE = Uid.minUid();

	private static final Uid END_XARESOURCE = Uid.maxUid();

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
		super(new Uid(), null, ObjectType.ANDPERSISTENT);

		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.CONSTRUCTORS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"XAResourceRecord.XAResourceRecord ( " + xid + " )");
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
		_heuristic = TwoPhaseOutcome.FINISH_OK;

		_theTransaction = tx;
	}

	public final Xid getXid()
	{
		return _tranID;
	}

	public Uid order()
	{
		if (_theXAResource instanceof StartXAResource)
			return START_XARESOURCE;
		else
		{
			if (_theXAResource instanceof EndXAResource)
				return END_XARESOURCE;
			else
				return super.order();
		}
	}

	public boolean propagateOnCommit()
	{
		return false; // cannot ever be nested!
	}

	public int typeIs()
	{
		return RecordType.JTA_RECORD;
	}

	public ClassName className()
	{
		return new ClassName("RecordType.JTA_RECORD");
	}

	public Object value()
	{
		return _theXAResource;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.setvalue
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.setvalue] {0}
	 *          called illegally.
	 */

	public void setValue(Object o)
	{
		if (jtaLogger.loggerI18N.isWarnEnabled())
		{
			jtaLogger.loggerI18N
					.warn(
							"com.arjuna.ats.internal.jta.resources.arjunacore.setvalue",
							new Object[]
							{ "XAResourceRecord::set_value()" });
		}
	}

	public int nestedAbort()
	{
		return TwoPhaseOutcome.FINISH_OK;
	}

	public int nestedCommit()
	{
		return TwoPhaseOutcome.FINISH_OK;
	}

	/*
	 * XA is not subtransaction aware.
	 */

	public int nestedPrepare()
	{
		return TwoPhaseOutcome.PREPARE_OK; // do nothing
	}

	/**
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.preparenulltx
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.preparenulltx]
	 *          {0} - null transaction!
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.preparefailed
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.preparefailed]
	 *          {0} - prepare failed with exception {1}
	 */

	public int topLevelPrepare()
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"XAResourceRecord.topLevelPrepare for " + _tranID);
		}

		if (!_valid || (_theXAResource == null))
		{
			removeConnection();

			return TwoPhaseOutcome.PREPARE_READONLY;
		}

		if (_tranID == null)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.resources.arjunacore.preparenulltx",
								new Object[]
								{ "XAResourceRecord.prepare" });
			}

			removeConnection();

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}

		try
		{
			if (endAssociation())
			{
				_theXAResource.end(_tranID, XAResource.TMSUCCESS);
			}

			_prepared = true;

			if (_theXAResource.prepare(_tranID) == XAResource.XA_RDONLY)
			{
                if (TxControl.isReadonlyOptimisation())
                {
                    // we won't be called again, so we need to tidy up now
                    removeConnection();
                }
                
				return TwoPhaseOutcome.PREPARE_READONLY;
			}
			else
				return TwoPhaseOutcome.PREPARE_OK;
		}
		catch (XAException e1)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.resources.arjunacore.preparefailed",
								new Object[]
								{ "XAResourceRecord.prepare",
										XAHelper.printXAErrorCode(e1) });
			}

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
				return TwoPhaseOutcome.PREPARE_NOTOK;  // will not call rollback
			default:
				return TwoPhaseOutcome.HEURISTIC_HAZARD; // we're not really sure (shouldn't get here though).
			}
		}
		catch (Exception e2)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.resources.arjunacore.preparefailed",
								new Object[]
								{ "XAResourceRecord.prepare", e2 });
			}

			if (_rollbackOptimization) // won't have rollback called on it
				removeConnection();

			return TwoPhaseOutcome.PREPARE_NOTOK;
		}
	}

	/**
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.rollbacknulltx
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.rollbacknulltx]
	 *          {0} - null transaction!
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.rollbackxaerror
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.rollbackxaerror]
	 *          {0} - xa error {1}
	 */

	public int topLevelAbort()
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"XAResourceRecord.topLevelAbort for " + _tranID);
		}

		if (!_valid)
			return TwoPhaseOutcome.FINISH_ERROR;

		if (_theTransaction != null
				&& _theTransaction.getXAResourceState(_theXAResource) == TxInfo.OPTIMIZED_ROLLBACK)
		{
			/*
			 * Already rolledback during delist.
			 */

			return TwoPhaseOutcome.FINISH_OK;
		}
		
		if (_tranID == null)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.resources.arjunacore.rollbacknulltx",
								new Object[]
								{ "XAResourceRecord.rollback" });
			}

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		else
		{
			if (_theXAResource == null)
				_theXAResource = getNewXAResource();

			if (_theXAResource != null)
			{
				if (_heuristic != TwoPhaseOutcome.FINISH_OK)
					return _heuristic;

				try
				{
					if (!_prepared)
					{
						if (endAssociation())
						{
							_theXAResource.end(_tranID, XAResource.TMSUCCESS);
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
				    }
				    else
				    {
					removeConnection();
					
					return TwoPhaseOutcome.FINISH_ERROR;
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
						if (jtaLogger.loggerI18N.isWarnEnabled())
						{
							jtaLogger.loggerI18N
									.warn(
											"com.arjuna.ats.internal.jta.resources.arjunacore.rollbackxaerror",
											new Object[]
											{
													"XAResourceRecord.rollback",
													XAHelper
															.printXAErrorCode(e1) });
						}

						switch (e1.errorCode)
						{
						case XAException.XAER_RMERR:
							if (!_prepared)
								break; // just do the finally block
						case XAException.XA_HEURHAZ:
							return TwoPhaseOutcome.HEURISTIC_HAZARD;
						case XAException.XA_HEURCOM:
							return TwoPhaseOutcome.HEURISTIC_COMMIT;
						case XAException.XA_HEURMIX:
							return TwoPhaseOutcome.HEURISTIC_MIXED;
						case XAException.XAER_NOTA:
						    if (_recovered)
							break; // rolled back previously and recovery completed
						case XAException.XA_HEURRB: // forget?
						case XAException.XA_RBROLLBACK:
						case XAException.XA_RBEND:
						case XAException.XA_RBCOMMFAIL:
						case XAException.XA_RBDEADLOCK:
						case XAException.XA_RBINTEGRITY:
						case XAException.XA_RBOTHER:
						case XAException.XA_RBPROTO:
						case XAException.XA_RBTIMEOUT:
							break;
						default:
							return TwoPhaseOutcome.FINISH_ERROR;
						}
					}
				}
				catch (Exception e2)
				{
					e2.printStackTrace();

					return TwoPhaseOutcome.FINISH_ERROR;
				}
				finally
				{
					if (!_prepared)
						removeConnection();
				}
			}
			else
			{
			    if (jtaLogger.loggerI18N.isWarnEnabled())
			    {
				jtaLogger.loggerI18N
				    .warn(
					  "com.arjuna.ats.internal.jta.resources.arjunacore.noresource",
					  new Object[] {_tranID});
			    }

			    if (XAResourceRecord._assumedComplete)
			    {
				if (jtaLogger.loggerI18N.isInfoEnabled())
				{
				    jtaLogger.loggerI18N
					.info(
					      "com.arjuna.ats.internal.jta.resources.arjunacore.assumecomplete",
					      new Object[] {_tranID});
				}

				return TwoPhaseOutcome.FINISH_OK;
			    }
			    else
				return TwoPhaseOutcome.FINISH_ERROR;
			}
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.commitnulltx
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.commitnulltx]
	 *          {0} - null transaction!
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.commitxaerror
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.commitxaerror]
	 *          {0} - xa error {1}
	 */

	public int topLevelCommit()
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"XAResourceRecord.topLevelCommit for " + _tranID);
		}

		if (!_prepared)
			return TwoPhaseOutcome.NOT_PREPARED;

		if (_tranID == null)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.resources.arjunacore.commitnulltx",
								new Object[]
								{ "XAResourceRecord.commit" });
			}

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		else
		{
			if (_theXAResource == null)
				_theXAResource = getNewXAResource();

			if (_theXAResource != null)
			{
				if (_heuristic != TwoPhaseOutcome.FINISH_OK)
					return _heuristic;

				/*
				 * No need for end call here since we can only get to this
				 * point by going through prepare.
				 */
				
				try
				{
					_theXAResource.commit(_tranID, false);
				}
				catch (XAException e1)
				{
					if (notAProblem(e1, true))
					{
						// some other thread got there first (probably)
					}
					else
					{
						if (jtaLogger.loggerI18N.isWarnEnabled())
						{
							jtaLogger.loggerI18N
									.warn(
											"com.arjuna.ats.internal.jta.resources.arjunacore.commitxaerror",
											new Object[]
											{
													"XAResourceRecord.commit",
													XAHelper
															.printXAErrorCode(e1) });
						}

						/*
						 * XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX,
						 * XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
						 * XAER_PROTO.
						 */

						switch (e1.errorCode)
						{
						case XAException.XA_HEURHAZ:
							return TwoPhaseOutcome.HEURISTIC_HAZARD;
						case XAException.XA_HEURCOM: // what about forget?
														// OTS doesn't support
														// this code here.
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
							return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
						case XAException.XA_HEURMIX:
							return TwoPhaseOutcome.HEURISTIC_MIXED;
						case XAException.XAER_NOTA:
						    if (_recovered)
							break; // committed previously and recovery completed
						    else
						        return TwoPhaseOutcome.HEURISTIC_HAZARD;  // something terminated the transaction!
						case XAException.XAER_PROTO:
						case XAException.XA_RETRY:
							return TwoPhaseOutcome.FINISH_ERROR;
						case XAException.XAER_INVAL:
						case XAException.XAER_RMFAIL: // resource manager
							// failed, did it
							// rollback?
							return TwoPhaseOutcome.HEURISTIC_HAZARD;
						default:
							return TwoPhaseOutcome.HEURISTIC_HAZARD;
						}
					}
				}
				catch (Exception e2)
				{
					return TwoPhaseOutcome.FINISH_ERROR;
				}
				finally
				{
					removeConnection();
				}
			}
			else
			{
			    if (jtaLogger.loggerI18N.isWarnEnabled())
			    {
				jtaLogger.loggerI18N
				    .warn(
					  "com.arjuna.ats.internal.jta.resources.arjunacore.noresource",
					  new Object[] {_tranID});
			    }

			    if (XAResourceRecord._assumedComplete)
			    {
				if (jtaLogger.loggerI18N.isInfoEnabled())
				{
				    jtaLogger.loggerI18N
					.info(
					      "com.arjuna.ats.internal.jta.resources.arjunacore.assumecomplete",
					      new Object[] {_tranID});
				}

				return TwoPhaseOutcome.FINISH_OK;
			    }
			    else
				return TwoPhaseOutcome.FINISH_ERROR;
			}
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	/**
	 * Is the XAException a non-error when received in reply to commit or
	 * rollback ? It normally is, but may be overridden in recovery.
	 */

	protected boolean notAProblem(XAException ex, boolean commit)
	{
		return XAResourceErrorHandler.notAProblem(_theXAResource, ex, commit);
	}

	public int nestedOnePhaseCommit()
	{
		return TwoPhaseOutcome.FINISH_ERROR;
	}

	/**
	 * For commit_one_phase we can do whatever we want since the transaction
	 * outcome is whatever we want. Therefore, we do not need to save any
	 * additional recoverable state, such as a reference to the transaction
	 * coordinator, since it will not have an intentions list anyway.
	 * 
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.opcnulltx
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.opcnulltx] {0} -
	 *          null transaction!
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.opcerror
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.opcerror] {0}
	 *          caught: {1}
	 */

	public int topLevelOnePhaseCommit()
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"XAResourceRecord.topLevelOnePhaseCommit for " + _tranID);
		}

		if (_tranID == null)
		{
			if (jtaLogger.loggerI18N.isWarnEnabled())
			{
				jtaLogger.loggerI18N
						.warn(
								"com.arjuna.ats.internal.jta.resources.arjunacore.opcnulltx",
								new Object[]
								{ "XAResourceRecord.1pc" });
			}

			return TwoPhaseOutcome.FINISH_ERROR;
		}
		else
		{
			if (_theXAResource != null)
			{
				if (_heuristic != TwoPhaseOutcome.FINISH_OK)
					return _heuristic;

				boolean commit = true;
				XAException endHeuristic = null;
				
				try
				{
					/*
					 * TODO in Oracle the end is not needed. Is this common
					 * across other RMs?
					 */

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
					endHeuristic = e1;
					break;
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
					removeConnection();
					return TwoPhaseOutcome.FINISH_ERROR;
				    }
				    }
				}
				
				try
				{
				    /*
				     * Not strictly necessary since calling commit will
				     * do the rollback if end failed as above.
				     */
				    
				    if (endHeuristic != null) // catch those RMs that terminate in end rather than follow the spec
					throw endHeuristic;
				    
				    if (commit)
					_theXAResource.commit(_tranID, true);
				    else
					_theXAResource.rollback(_tranID);
				}
				catch (XAException e1)
				{
					/*
					 * XA_HEURHAZ, XA_HEURCOM, XA_HEURRB, XA_HEURMIX,
					 * XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or
					 * XAER_PROTO. XA_RB*
					 */

					switch (e1.errorCode)
					{
					case XAException.XA_HEURHAZ:
					case XAException.XA_HEURMIX:
						return TwoPhaseOutcome.HEURISTIC_HAZARD;
					case XAException.XA_HEURCOM:
						forget();
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
						forget();
						return TwoPhaseOutcome.FINISH_ERROR;
					case XAException.XAER_NOTA:
						return TwoPhaseOutcome.HEURISTIC_HAZARD; // something committed or rolled back without asking us!
					case XAException.XAER_PROTO:
					case XAException.XAER_INVAL:
					case XAException.XAER_RMFAIL: // resource manager failed,
						// did it rollback?
						return TwoPhaseOutcome.FINISH_ERROR;
					default:
						return TwoPhaseOutcome.FINISH_ERROR;
					}
				}
				catch (Exception e2)
				{
					if (jtaLogger.loggerI18N.isWarnEnabled())
					{
						jtaLogger.loggerI18N
								.warn(
										"com.arjuna.ats.internal.jta.resources.arjunacore.opcerror",
										new Object[]
										{ "XAResourceRecord.commit_one_phase",
												e2 });
					}

					return TwoPhaseOutcome.FINISH_ERROR;
				}
				finally
				{
					removeConnection();
				}
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	public boolean forgetHeuristic()
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PUBLIC,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"XAResourceRecord.forget for " + _tranID);
		}

		forget();

		removeConnection();

		return true;
	}

	private void forget()
	{
		if ((_theXAResource != null) && (_tranID != null))
		{
			try
			{
				_theXAResource.forget(_tranID);
			}
			catch (Exception e)
			{
			}
		}
	}

	/*
	 * Independant recovery cannot occur. Must be driven by the recovery of the
	 * local transaction, i.e., top-down recovery.
	 */

	protected int recover()
	{
		if (jtaLogger.logger.isDebugEnabled())
		{
			jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
					VisibilityLevel.VIS_PROTECTED,
					com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
					"XAResourceRecord.recover");
		}

		return XARecoveryResource.FAILED_TO_RECOVER;
	}

	public static AbstractRecord create()
	{
		return new XAResourceRecord();
	}

	public static void remove(AbstractRecord toDelete)
	{
		toDelete = null;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.savestate
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.savestate]
	 *          Could not serialize a Serializable XAResource!
	 */

	public boolean save_state(OutputObjectState os, int t)
	{
		boolean res = false;

		try
		{
			os.packInt(_heuristic);

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
        
        					os.packBytes(s.toByteArray());
        				}
        				catch (NotSerializableException ex)
        				{
        				    if (jtaLogger.loggerI18N.isWarnEnabled())
        				    {
        				        jtaLogger.loggerI18N
        				            .warn("com.arjuna.ats.internal.jta.resources.arjunacore.savestate");
        				    }
                                            
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

			res = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			res = false;
		}

		if (res)
			res = super.save_state(os, t);

		return res;
	}

	/**
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.restorestate
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.restorestate]
	 *          Exception on attempting to restore XAResource
	 * @message com.arjuna.ats.internal.jta.resources.arjunacore.norecoveryxa
	 *          [com.arjuna.ats.internal.jta.resources.arjunacore.norecoveryxa]
	 *          Could not find new XAResource to use for recovering
	 *          non-serializable XAResource {0}
	 */

	public boolean restore_state(InputObjectState os, int t)
	{
		boolean res = false;

		try
		{
			_heuristic = os.unpackInt();
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
						byte[] b = os.unpackBytes();

						ByteArrayInputStream s = new ByteArrayInputStream(b);
						ObjectInputStream o = new ObjectInputStream(s);

						_theXAResource = (XAResource) o.readObject();
						o.close();

						if (jtaLogger.logger.isDebugEnabled())
						{
							jtaLogger.logger
									.debug(
											DebugLevel.FUNCTIONS,
											VisibilityLevel.VIS_PUBLIC,
											com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
											"XAResourceRecord.restore_state - XAResource de-serialized");
						}
					}
					catch (Exception ex)
					{
						// not serializable in the first place!

						if (jtaLogger.loggerI18N.isWarnEnabled())
						{
							jtaLogger.loggerI18N
									.warn(
											"com.arjuna.ats.internal.jta.resources.arjunacore.restorestate",
											ex);
						}

						return false;
					}
				}
				else
				{
					/*
					 * Lookup new XAResource via XARecoveryModule if possible.
					 */

					_theXAResource = getNewXAResource();

					if (_theXAResource == null)
					{
						jtaLogger.loggerI18N
								.warn(
										"com.arjuna.ats.internal.jta.resources.arjunacore.norecoveryxa",
										new Object[]
										{ _tranID });

						/*
						 * Don't prevent tx from activating because there may be
						 * other participants that can still recover. Plus, we will
						 * try to get a new XAResource later for this instance.
						 */
						
						res = true;
					}
				}
			}
			else
			{
				String creatorName = os.unpackString();
				Class c = Thread.currentThread().getContextClassLoader()
						.loadClass(creatorName);

				_recoveryObject = (RecoverableXAConnection) c.newInstance();

				_recoveryObject.unpackFrom(os);
				_theXAResource = _recoveryObject.getResource();

				if (jtaLogger.logger.isDebugEnabled())
				{
					jtaLogger.logger.debug(DebugLevel.FUNCTIONS,
							VisibilityLevel.VIS_PUBLIC,
							com.arjuna.ats.jta.logging.FacilityCode.FAC_JTA,
							"XAResourceRecord.restore_state - XAResource got from "
									+ creatorName);
				}
			}

			res = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();

			res = false;
		}

		if (res)
			res = super.restore_state(os, t);

		return res;
	}

	public String type()
	{
		return XAResourceRecord.typeName();
	}

	public static String typeName()
	{
		return "/StateManager/AbstractRecord/XAResourceRecord";
	}

	public boolean doSave()
	{
		return true;
	}

	public void merge(AbstractRecord a)
	{
	}

	public void alter(AbstractRecord a)
	{
	}

	public boolean shouldAdd(AbstractRecord a)
	{
		return false;
	}

	public boolean shouldAlter(AbstractRecord a)
	{
		return false;
	}

	public boolean shouldMerge(AbstractRecord a)
	{
		return false;
	}

	public boolean shouldReplace(AbstractRecord a)
	{
		return false;
	}

	protected XAResourceRecord()
	{
		super();

		_theXAResource = null;
		_recoveryObject = null;
		_tranID = null;
		_prepared = true;
		_heuristic = TwoPhaseOutcome.FINISH_OK;
		_valid = true;
		_theTransaction = null;
		_recovered = true;
	}

	protected XAResourceRecord(Uid u)
	{
		super(u, null, ObjectType.ANDPERSISTENT);

		_theXAResource = null;
		_recoveryObject = null;
		_tranID = null;
		_prepared = true;
		_heuristic = TwoPhaseOutcome.FINISH_OK;
		_valid = true;
		_theTransaction = null;
		_recovered = true;
	}

	/**
	 * For those objects where the original XAResource could not be saved.
	 */

	protected synchronized void setXAResource(XAResource res)
	{
		_theXAResource = res;
	}

	/**
	 * This routine finds the new XAResource for the transaction that used the
	 * old resource we couldn't serialize. It does this by looking up the
	 * XARecoveryModule in the recovery manager and asking it for the
	 * XAResource. The recovery manager will then look through its list of
	 * registered XARecoveryResource implementations for the appropriate
	 * instance. If the XARecoveryModule hasn't been initialised yet then this
	 * routine will fail, but on the next scan it should work.
	 */

	private final XAResource getNewXAResource()
	{
		RecoveryManager recMan = RecoveryManager.manager();
		Vector recoveryModules = recMan.getModules();

		if (recoveryModules != null)
		{
			Enumeration modules = recoveryModules.elements();

			while (modules.hasMoreElements())
			{
				RecoveryModule m = (RecoveryModule) modules.nextElement();

				if (m instanceof XARecoveryModule)
				{
				    /*
				     * Blaargh! There are better ways to do this!
				     */

					return ((XARecoveryModule) m).getNewXAResource(_tranID);
				}
			}
		}

		return null;
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
			doEnd = false; // Recovery mode

		return doEnd;
	}

	protected XAResource _theXAResource;

	private RecoverableXAConnection _recoveryObject;
	private Xid _tranID;

	private boolean _prepared;

	private boolean _valid;

	private int _heuristic;

	private TransactionImple _theTransaction;
    private boolean _recovered = false;

	private static boolean _rollbackOptimization = false;
    private static boolean _assumedComplete = false;

	static
	{
		String optimization = jtaPropertyManager.propertyManager.getProperty(
				Environment.JTA_TM_IMPLEMENTATION, "OFF");

		if (optimization.equals("ON"))
			_rollbackOptimization = true;

		/*
		 * WARNING: USE WITH EXTEREME CARE!!
		 *
		 * This assumes that if there is no XAResource that can deal with an Xid
		 * after recovery, then we failed after successfully committing the transaction
		 * but before updating the log. In which case we just need to ignore this
		 * resource and remove the entry from the log.
		 *
		 * BUT if not all XAResourceRecovery instances are correctly implemented
		 * (or present) we may end up removing participants that have not been dealt
		 * with. Hence USE WITH EXTREME CARE!!
		 */

		String assumedComplete = jtaPropertyManager.propertyManager.getProperty(
				Environment.XA_ASSUME_RECOVERY_COMPLETE, "FALSE");

		if (assumedComplete.equalsIgnoreCase("true"))
			_assumedComplete = true;
	}

}
