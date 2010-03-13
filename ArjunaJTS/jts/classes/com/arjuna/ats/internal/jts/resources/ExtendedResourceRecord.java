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
 * Copyright (C) 1998, 1999, 2000, 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ExtendedResourceRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.resources;

/*
 * 
 * OTS Abstract Record Class Implementation.
 *
 * (Extended resource functionality.)
 *
 */

import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import java.io.PrintWriter;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CORBA.INVALID_TRANSACTION;

import org.omg.CosTransactions.NotPrepared;
import org.omg.CosTransactions.HeuristicCommit;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicRollback;
import java.io.IOException;

/**
 * This abstract record is used whenever resources are derived from the
 * ArjunaOTS module's AbstractRecord interface. This gives users the flexibility
 * of the original Arjuna system's AbstractRecord, and makes resources behave
 * correctly!
 * 
 * We know that instances of this record will only be called for instances of
 * AbstractRecord objects.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ExtendedResourceRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 * 
 * @message com.arjuna.ats.internal.jts.resources.errgenerr {0} caught exception: {1}
 * @message com.arjuna.ats.internal.jts.resources.noparent {0} has no parent transaction!
 */

public class ExtendedResourceRecord extends
		com.arjuna.ats.arjuna.coordinator.AbstractRecord
{

	/**
	 * @param propagate
	 *            tells us whether to propagate the resource at nested commit or
	 *            not.
	 * @param theResource
	 *            is the proxy that allows us to call out to the object.
	 * @param myParent
	 *            is the proxy for the parent coordinator needed in
	 *            commit_subtransaction.
	 */

	public ExtendedResourceRecord (boolean propagate, Uid objUid, ArjunaSubtranAwareResource theResource, Coordinator myParent, Uid recCoordUid, ArjunaTransactionImple current)
	{
		super(objUid, null, ObjectType.ANDPERSISTENT);

		_resourceHandle = theResource;
		_stringifiedResourceHandle = null;
		_parentCoordHandle = myParent;
		_recCoordUid = (recCoordUid != null) ? recCoordUid : new Uid(
				Uid.nullUid());
		_currentTransaction = current;
		_propagateRecord = propagate;
		_rolledback = false;
		_endpointFailed = false;
		_restored = false;
	}

	/**
	 * Specific OTS method for getting at the value.
	 */

	public final ArjunaSubtranAwareResource resourceHandle ()
	{
		/*
		 * After recovery we may have not been able to recreate the
		 * _resourceHandle due to the fact that the Resource itself may not be
		 * alive resulting in a failure to narrow the reference returned from
		 * string_to_object. In such cases we cache the stringified reference
		 * and retry the narrow when we need to use the _resourceHandle as at
		 * this point the Resource may have recovered.
		 */

		if ((_resourceHandle == null) && (_stringifiedResourceHandle != null))
		{
			try
			{
				org.omg.CORBA.ORB theOrb = ORBManager.getORB().orb();

				if (theOrb == null)
					throw new UNKNOWN();

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord: About to string_to_object on "
							+ _stringifiedResourceHandle);
				}

				org.omg.CORBA.Object optr = theOrb.string_to_object(_stringifiedResourceHandle);

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord: Successfully stringed to object, next try to narrow");
				}

				theOrb = null;

				_resourceHandle = com.arjuna.ArjunaOTS.ArjunaSubtranAwareResourceHelper.narrow(optr);

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord: Successfully narrowed");
				}

				if (_resourceHandle == null)
					throw new BAD_PARAM();
				else
				{
					optr = null;
				}
			}
			catch (SystemException e)
			{
				// Failed to narrow to a ArjunaSubtranAwareResource

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord: Failed to narrow to ArjunaSubtranAwareResource");
				}
			}
		}

		return _resourceHandle;
	}

	public boolean propagateOnCommit ()
	{
		if (_propagateRecord)
			return true;

		OTSAbstractRecord resHandle = otsRecord();

		try
		{
			if ((resHandle != null) && !_endpointFailed)
				return resHandle.propagateOnCommit();
		}
		catch (Exception e)
		{
			_endpointFailed = true;
		}

		return true;
	}

	public boolean propagateOnAbort ()
	{
		OTSAbstractRecord resHandle = otsRecord();

		try
		{
			if ((resHandle != null) && !_endpointFailed)
				return resHandle.propagateOnAbort();
		}
		catch (Exception e)
		{
			_endpointFailed = true;
		}

		return false;
	}

	public Uid order ()
	{
		Uid toReturn = super.order();

		if (_cachedUid == null)
		{
			OTSAbstractRecord resHandle = otsRecord();

			try
			{
				if ((resHandle != null) && !_endpointFailed)
				{
					toReturn = _cachedUid = new Uid(resHandle.uid());
				}
			}
			catch (Exception e)
			{
				_endpointFailed = true;
			}
		}
		else
			toReturn = _cachedUid;

		return toReturn;
	}

	/**
	 * @message com.arjuna.ats.internal.jts.resources.errtypefail {0} failed.
	 *          Returning default value: {1}
	 */

	public int typeIs ()
	{
		OTSAbstractRecord resHandle = otsRecord();
		int r = RecordType.OTS_ABSTRACTRECORD;

		if (_cachedType == -1)
		{
			try
			{
				if ((resHandle != null) && !_endpointFailed)
					_cachedType = r = resHandle.type_id();
			}
			catch (Exception e)
			{
				r = RecordType.OTS_ABSTRACTRECORD;

				_endpointFailed = true;

				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errtypefail", new Object[]
					{ "ExtendedResourceRecord.typeIs", new Integer(r) });
				}
			}
		}
		else
			r = _cachedType;

		resHandle = null;

		return r;
	}

	public Object value ()
	{
		return _resourceHandle;
	}

	/**
	 * @message com.arjuna.ats.internal.jts.resources.errsetvalue {0} called
	 *          illegally!
	 */

	public void setValue (Object o)
	{
		if (jtsLogger.loggerI18N.isWarnEnabled())
		{
			jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errsetvalue", new Object[]
			{ "ExtendedResourceRecord.set_value" });
		}
	}

	/**
	 * General nesting rules:
	 * 
	 * Only SubtransactionAware resources get registered with nested actions.
	 * The ExtendedResourceRecord creator is assumed to ensure that plain
	 * Resources are only registered with the appropriate top level action.
	 * 
	 * That said the _propagateRecord flag ensures that resources registered via
	 * register_subtran only take part in the action they where registered in
	 * after which they are dropped.
	 */

	public int nestedAbort ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord::nestedAbort() for "
					+ order());
		}

		/*
		 * Must be an staResource to get here.
		 */

		try
		{
			resourceHandle().rollback_subtransaction();
		}
		catch (OBJECT_NOT_EXIST one)
		{
			if (_rolledback)
			{
				_rolledback = false; // in case we get propagated to the parent

				return TwoPhaseOutcome.FINISH_OK;
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (SystemException ex)
		{
	                  if (jtsLogger.loggerI18N.isWarnEnabled())
	                        {
	                                jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
	                                { "ExtendedResourceRecord.nestedAbort", ex });
	                        }
	                  
			return TwoPhaseOutcome.FINISH_ERROR;
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	/**
	 * @message com.arjuna.ats.internal.jts.resources.errnoparent {0} - no
	 *          parent!
	 */

	public int nestedCommit ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord::nestedCommit() for "
					+ order());
		}

		int o = TwoPhaseOutcome.FINISH_ERROR;

		try
		{
			resourceHandle().commit_subtransaction(_parentCoordHandle);

			/*
			 * Now release the parent as it is about to be destroyed anyway.
			 */

			_parentCoordHandle = null;

			if (_currentTransaction != null)
			{
				/*
				 * Now change our notion of our parent for subsequent nested
				 * transaction commits. We were passed a reference to the
				 * current transaction, so we can just ask it for it's parent.
				 * If it doesn't have one, then generate an error.
				 */

				_currentTransaction = (ArjunaTransactionImple) _currentTransaction.parent();

				ControlImple control = ((_currentTransaction == null) ? null : _currentTransaction.getControlHandle());

				if (control != null)
				{
					_parentCoordHandle = control.get_coordinator();

					control = null;

					o = TwoPhaseOutcome.FINISH_OK;
				}
				else
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errnoparent", new Object[]
						{ "ExtendedResourceRecord.nestedCommit" });
					}

					o = TwoPhaseOutcome.FINISH_ERROR;
				}
			}
			else
			{
			    if (jtsLogger.loggerI18N.isWarnEnabled())
                            {
                                    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.noparent", new Object[]
                                    { get_uid() });
                            }
			    
				o = TwoPhaseOutcome.FINISH_ERROR;
			}
		}
		catch (Exception e)
		{
		    e.printStackTrace();
		    
	                  if (jtsLogger.loggerI18N.isWarnEnabled())
	                        {
	                                jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
	                                { "ExtendedResourceRecord.nestedCommit", e });
	                        }
		    
			o = TwoPhaseOutcome.FINISH_ERROR;
		}

		return o;
	}

	/**
	 * Because resource is an Arjuna AbstractRecord we can do proper nesting!
	 */

	public int nestedPrepare ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord::nestedPrepare() for "
					+ order());
		}

		int o = TwoPhaseOutcome.PREPARE_NOTOK;

		try
		{
			switch (resourceHandle().prepare_subtransaction().value())
			{
			case Vote._VoteCommit:
				o = TwoPhaseOutcome.PREPARE_OK;
				break;
			case Vote._VoteRollback:
				_rolledback = true;

				o = TwoPhaseOutcome.PREPARE_NOTOK;
				break;
			case Vote._VoteReadOnly:
				o = TwoPhaseOutcome.PREPARE_READONLY;
				break;
			}
		}
		catch (Exception e)
		{
	                  if (jtsLogger.loggerI18N.isWarnEnabled())
	                        {
	                                jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
	                                { "ExtendedResourceRecord.nestedPrepare", e });
	                        }
	                  
			o = TwoPhaseOutcome.PREPARE_NOTOK;
		}

		return o;
	}

	public int topLevelAbort ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord::topLevelAbort() for "
					+ order());
		}

		try
		{
			if (resourceHandle() != null)
			{
				_resourceHandle.rollback();
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (HeuristicCommit e1)
		{
			if (_rolledback)
				return TwoPhaseOutcome.HEURISTIC_HAZARD; // participant lied in
														 // prepare!
			else
				return TwoPhaseOutcome.HEURISTIC_COMMIT;
		}
		catch (HeuristicMixed e2)
		{
			return TwoPhaseOutcome.HEURISTIC_MIXED;
		}
		catch (HeuristicHazard e3)
		{
			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
		catch (OBJECT_NOT_EXIST one)
		{
			/*
			 * If the resource voted to roll back in prepare then it can legally
			 * be garbage collected at that point. Hence, it may be that we get
			 * a failure during rollback if we try to call it. If it didn't vote
			 * to roll back then some other error has happened.
			 */

			if (_rolledback)
				return TwoPhaseOutcome.FINISH_OK;
			else
				return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
		catch (SystemException e4)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
				{ "ExtendedResourceRecord.topLevelAbort", e4 });
			}

			return TwoPhaseOutcome.FINISH_ERROR;
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelCommit ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord::topLevelCommit() for "
					+ order());
		}

		try
		{
			if (resourceHandle() != null)
			{
				_resourceHandle.commit();
			}
			else
				return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (NotPrepared e1)
		{
			return TwoPhaseOutcome.NOT_PREPARED;
		}
		catch (HeuristicRollback e2)
		{
			return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
		}
		catch (HeuristicMixed e3)
		{
			return TwoPhaseOutcome.HEURISTIC_MIXED;
		}
		catch (HeuristicHazard e4)
		{
			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
		catch (SystemException e5)
		{
	                  if (jtsLogger.loggerI18N.isWarnEnabled())
	                        {
	                                jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
	                                { "ExtendedResourceRecord.topLevelCommit", e5 });
	                        }
	                  
			return TwoPhaseOutcome.FINISH_ERROR;
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	public int topLevelPrepare ()
	{
		if (jtsLogger.logger.isDebugEnabled())
		{
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord::topLevelPrepare() for "
					+ order());
		}

		try
		{
			if (resourceHandle() != null)
			{
				switch (_resourceHandle.prepare().value())
				{
				case Vote._VoteCommit:
					return TwoPhaseOutcome.PREPARE_OK;
				case Vote._VoteRollback:
					_rolledback = true;

					return TwoPhaseOutcome.PREPARE_NOTOK;
				case Vote._VoteReadOnly:
					return TwoPhaseOutcome.PREPARE_READONLY;
				}
			}
			else
				return TwoPhaseOutcome.PREPARE_NOTOK;
		}
		catch (HeuristicMixed e1)
		{
			return TwoPhaseOutcome.HEURISTIC_MIXED;
		}
		catch (HeuristicHazard e2)
		{
			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
		catch (Exception e)
		{
	                  if (jtsLogger.loggerI18N.isWarnEnabled())
	                        {
	                                jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
	                                { "ExtendedResourceRecord.topLevelPrepare", e });
	                        }
	                  
			return TwoPhaseOutcome.PREPARE_NOTOK;
		}

		return TwoPhaseOutcome.PREPARE_NOTOK;
	}

	public int nestedOnePhaseCommit ()
	{
		switch (nestedPrepare())
		{
		case TwoPhaseOutcome.PREPARE_OK:
			return nestedCommit();
		case TwoPhaseOutcome.PREPARE_READONLY:
			return TwoPhaseOutcome.FINISH_OK;
		default:
			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	public int topLevelOnePhaseCommit ()
	{
		try
		{			
			if (resourceHandle() != null)
				_resourceHandle.commit_one_phase();
		}
		catch (HeuristicHazard e1)
		{			
			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
		catch (TRANSACTION_ROLLEDBACK e4)
		{			
		    /*
		     * It rolled back. That's ok, but we need to be able
		     * to communicate that back to the caller.
		     */
		    
			return TwoPhaseOutcome.ONE_PHASE_ERROR;  // TODO TPO extension required.
		}
		catch (INVALID_TRANSACTION e5)
		{
			return TwoPhaseOutcome.ONE_PHASE_ERROR;
		}
		catch (final UNKNOWN ex)
		{
		    /*
		     * Means we can retry.
		     */
		    
		    return TwoPhaseOutcome.FINISH_ERROR;
		}
		catch (Exception e5)
		{
	                  if (jtsLogger.loggerI18N.isWarnEnabled())
	                        {
	                                jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
	                                { "ExtendedResourceRecord.topLevelOnePhaseCommit", e5 });
	                        }
	                  
			e5.printStackTrace();
			
			/*
			 * Unknown error - better assume heuristic!
			 */

			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}

		return TwoPhaseOutcome.FINISH_OK;
	}

	/**
	 * @message com.arjuna.ats.internal.jts.resources.errnores {0} called
	 *          without a resource reference!
	 */

	public boolean forgetHeuristic ()
	{
		try
		{
			if (resourceHandle() != null)
			{
				_resourceHandle.forget();
				return true;
			}
			else
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errnores", new Object[]
					{ "ExtendedResourceRecord.forgetHeuristic" });
				}
			}
		}
		catch (Exception e)
		{
			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
				{ "ExtendedResourceRecord.forgetHeuristic", e });
			}
		}

		return false;
	}
/*
	public static AbstractRecord create ()
	{
		return new ExtendedResourceRecord();
	}

	public void remove (AbstractRecord toDelete)
	{
		toDelete = null;
	}
*/
	public void print (PrintWriter strm)
	{
		super.print(strm);

		strm.println("ExtendedResourceRecord");
		strm.println(_resourceHandle + "\t" + _parentCoordHandle + "\t"
				+ _propagateRecord);
	}

	/**
	 * restore_state and save_state for ExtendedResourceRecords doesn't
	 * generally apply due to object pointers. However, we need to save
	 * something so we can recover failed transactions. So, rather than insist
	 * that all Resources derive from a class which we can guarantee will give
	 * us some unique id, we simply rely on string_to_object and
	 * object_to_string to be meaningful.
	 */

	public boolean restore_state (InputObjectState os, int t)
	{
		int isString = 0;
		boolean result = super.restore_state(os, t);

		if (!result)
			return false;

		try
		{
			_propagateRecord = os.unpackBoolean();

			/*
			 * Do we need to restore the parent coordinator handle?
			 */

			_parentCoordHandle = null;

			_cachedUid = UidHelper.unpackFrom(os);

			_cachedType = os.unpackInt();

			isString = os.unpackInt();

			if (isString == 1)
			{
				_stringifiedResourceHandle = os.unpackString();

				/*
				 * Could call resourceHandle() here to restore the
				 * _resourceHandle reference but no loss in doing it lazily.
				 */

				// Unpack recovery coordinator Uid
				_recCoordUid = UidHelper.unpackFrom(os);

				if (jtsLogger.logger.isDebugEnabled())
				{
					jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,

					(com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord.restore_state: unpacked record with uid="
							+ _recCoordUid);
				}
			}
			else
				_stringifiedResourceHandle = null;
		}
		catch (IOException e)
		{
			e.printStackTrace();

			result = false;
		}

		_restored = result;

		return result;
	}

	/**
	 * restore_state and save_state for ExtendedResourceRecords doesn't
	 * generally apply due to object pointers. However, we need to save
	 * something so we can recover failed transactions. So, rather than insist
	 * that all Resources derive from a class which we can guarantee will give
	 * us some unique id, we simply rely on string_to_object and
	 * object_to_string to be meaningful.
	 */

	public boolean save_state (OutputObjectState os, int t)
	{
		boolean result = super.save_state(os, t);

		if (!result)
			return false;

		try
		{
			/*
			 * Do we need to save the parent coordinator handle?
			 */

			/*
			 * If we have a _resourceHandle then we stringify it and pack the
			 * string. Failing that if we have a cached stringified version (in
			 * _stringifiedResourceHandle) then we pack that. If we have neither
			 * then we're doomed.
			 */

			os.packBoolean(_propagateRecord);

			if (_cachedUid == null)
				_cachedUid = order();

			UidHelper.packInto(_cachedUid, os);

			if (_cachedType == -1)
				_cachedType = typeIs();

			os.packInt(_cachedType);

			if ((_resourceHandle == null)
					&& (_stringifiedResourceHandle == null))
			{
				os.packInt(-1);
			}
			else
			{
				os.packInt(1);
				String stringRef = null;

				if (_resourceHandle != null)
				{
					org.omg.CORBA.ORB theOrb = ORBManager.getORB().orb();

					if (theOrb == null)
						throw new UNKNOWN();

					stringRef = theOrb.object_to_string(_resourceHandle);

					theOrb = null;
				}
				else
				{
					stringRef = _stringifiedResourceHandle;
				}

				if (stringRef != null)
				{
					os.packString(stringRef);

					if (jtsLogger.logger.isDebugEnabled())
					{
						jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ExtendedResourceRecord: packed obj ref "
								+ stringRef);
					}
				}
				else
				{
					result = false;
				}

				stringRef = null;

				if (result)
				{
					// Pack recovery coordinator Uid
				    UidHelper.packInto(_recCoordUid, os);

					if (jtsLogger.logger.isDebugEnabled())
					{
						jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "Packed rec co uid of "
								+ _recCoordUid);
					}
				}
			}
		}
		catch (IOException e)
		{
			result = false;
		}
		catch (SystemException e)
		{
			result = false;
		}

		return result;
	}

	public String type ()
	{
		return "/StateManager/AbstractRecord/ExtendedResourceRecord";
	}

	/**
	 * @message com.arjuna.ats.internal.jts.resources.errsavefail {0} failed.
	 *          Returning default value: {1}
	 */

	public boolean doSave ()
	{
		if (_restored)
			return true;

		switch (_doSave) // check cached value first
		{
		case -1:
			break; // not cached yet
		case 0:
			return false;
		case 1:
			return true;
		default:
			break;
		}
		
		OTSAbstractRecord resHandle = otsRecord();
		boolean save = true;

		try
		{
			if ((resHandle != null) && !_endpointFailed)
				save = resHandle.saveRecord();
		}
		catch (Exception e)
		{
			save = true; // just to be on the safe side!

			_endpointFailed = true;

			if (jtsLogger.loggerI18N.isWarnEnabled())
			{
				jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errsavefail", new Object[]
				{ "ExtendedResourceRecord.doSave", new Boolean(save) });
			}
		}

		resHandle = null;

		_doSave = (save ? 1 : 0);
		
		return save;
	}

	public final Uid getRCUid ()
	{
		return _recCoordUid;
	}

	public void merge (AbstractRecord absRec)
	{
		OTSAbstractRecord resHandle = otsRecord();

		if (resHandle != null)
		{
			OTSAbstractRecord rec = otsRecord(absRec);

			if (rec != null)
			{
				try
				{
					resHandle.merge(rec);
				}
				catch (OBJECT_NOT_EXIST ex)
				{
				}
				catch (Exception e)
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
						{ "ExtendedResourceRecord.merge", e });
					}
				}

				rec = null;
			}
		}

		resHandle = null;
	}

	public void alter (AbstractRecord absRec)
	{
		OTSAbstractRecord resHandle = otsRecord();

		if (resHandle != null)
		{
			OTSAbstractRecord rec = otsRecord(absRec);

			if (rec != null)
			{
				try
				{
					resHandle.alter(rec);
				}
				catch (OBJECT_NOT_EXIST ex)
				{
				}
				catch (Exception e)
				{
					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
						{ "ExtendedResourceRecord.alter", e });
					}
				}

				rec = null;
			}
		}

		resHandle = null;
	}

	public boolean shouldAdd (AbstractRecord absRec)
	{
		boolean result = false;
		OTSAbstractRecord resHandle = otsRecord();

		if ((resHandle != null) && !_endpointFailed)
		{
			OTSAbstractRecord rec = otsRecord(absRec);

			if (rec != null)
			{
				try
				{
					result = resHandle.shouldAdd(rec);
				}
				catch (OBJECT_NOT_EXIST ex)
				{
				}
				catch (Exception e)
				{
					_endpointFailed = true;

					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
						{ "ExtendedResourceRecord.shouldAdd", e });
					}
				}

				rec = null;
			}
		}

		resHandle = null;

		return result;
	}

	public boolean shouldAlter (AbstractRecord absRec)
	{
		boolean result = false;
		OTSAbstractRecord resHandle = otsRecord();

		if ((resHandle != null) && !_endpointFailed)
		{
			OTSAbstractRecord rec = otsRecord(absRec);

			if (rec != null)
			{
				try
				{
					result = resHandle.shouldAlter(rec);
				}
				catch (OBJECT_NOT_EXIST ex)
				{
				}
				catch (Exception e)
				{
					_endpointFailed = true;

					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
						{ "ExtendedResourceRecord.shouldAlter", e });
					}
				}

				rec = null;
			}
		}

		resHandle = null;

		return result;
	}

	public boolean shouldMerge (AbstractRecord absRec)
	{
		boolean result = false;
		OTSAbstractRecord resHandle = otsRecord();

		if ((resHandle != null) && !_endpointFailed)
		{
			OTSAbstractRecord rec = otsRecord(absRec);

			if (rec != null)
			{
				try
				{
					result = resHandle.shouldMerge(rec);
				}
				catch (OBJECT_NOT_EXIST ex)
				{
				}
				catch (Exception e)
				{
					_endpointFailed = true;

					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
						{ "ExtendedResourceRecord.shouldMerge", e });
					}
				}

				rec = null;
			}
		}

		resHandle = null;

		return result;
	}

	public boolean shouldReplace (AbstractRecord absRec)
	{
		boolean result = recoveryReplace(absRec);
		OTSAbstractRecord resHandle = otsRecord();

		if ((resHandle != null) && !result && !_endpointFailed)
		{
			OTSAbstractRecord rec = otsRecord(absRec);

			if (rec != null)
			{
				try
				{
					result = resHandle.shouldReplace(rec);
				}
				catch (OBJECT_NOT_EXIST ex)
				{
				}
				catch (Exception e)
				{
					_endpointFailed = true;

					if (jtsLogger.loggerI18N.isWarnEnabled())
					{
						jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
						{ "ExtendedResourceRecord.shouldReplace", e });
					}
				}

				rec = null;
			}
		}

		resHandle = null;

		return result;
	}

	/**
	 * Protected constructor used by crash recovery.
	 */

	public ExtendedResourceRecord ()
	{
		super();

		_resourceHandle = null;
		_stringifiedResourceHandle = null;
		_recCoordUid = new Uid(Uid.nullUid());
		_parentCoordHandle = null;
		_currentTransaction = null;
		_propagateRecord = false;
		_endpointFailed = false;
		_restored = false;
	}

	private boolean recoveryReplace (AbstractRecord rec)
	{
		boolean replace = false;

		if ((rec != null) && (rec instanceof ExtendedResourceRecord))
		{
			/*
			 * It is no good checking type equality because at recovery time the
			 * failed resource won't respond and the implementations of this
			 * interface can override typeIs.
			 */

			ExtendedResourceRecord newRec = (ExtendedResourceRecord) rec;

			/*
			 * Check whether the new record corresponds to the same
			 * RecoveryCoordinator as this one. If so replace. Don't replace if
			 * the uids are NIL_UID
			 */

			if ((_recCoordUid.notEquals(Uid.nullUid()))
					&& (_recCoordUid.equals(newRec.getRCUid())))
			{
				replace = true;
			}
		}

		return replace;
	}

	private final OTSAbstractRecord otsRecord (AbstractRecord absRec)
	{
		/*
		 * Is the abstract record an ExtendedResourceRecord ?
		 */

		OTSAbstractRecord resHandle = null;

		if ((absRec != null) && (absRec instanceof ExtendedResourceRecord))
		{
			try
			{
				ExtendedResourceRecord theRecord = (ExtendedResourceRecord) absRec;
				ArjunaSubtranAwareResource theResource = theRecord.resourceHandle();

				resHandle = com.arjuna.ArjunaOTS.OTSAbstractRecordHelper.narrow(theResource);

				theResource = null;
			}
			catch (Exception e)
			{
				if (jtsLogger.loggerI18N.isWarnEnabled())
				{
					jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.errgenerr", new Object[]
					{ "ExtendedResourceRecord.otsRecord", e });
				}
			}
		}

		return resHandle;
	}

	private final OTSAbstractRecord otsRecord ()
	{
		try
		{
		    if (_otsARHandle == null)
		        _otsARHandle = com.arjuna.ArjunaOTS.OTSAbstractRecordHelper.narrow(_resourceHandle);

		    if (_otsARHandle == null)
		        throw new BAD_PARAM();
		    else
		        return _otsARHandle;
		}
		catch (Exception e)
		{
			// we are not an OTSAbstractRecord

			return null;
		}
	}

	private Coordinator _parentCoordHandle;
	private ArjunaSubtranAwareResource _resourceHandle;
	private String _stringifiedResourceHandle;
	private Uid _recCoordUid;
	private ArjunaTransactionImple _currentTransaction;
	private boolean _propagateRecord;
	private OTSAbstractRecord _otsARHandle;

	// cached variables

	private Uid _cachedUid = null;
	private int _cachedType = -1;
	private int _doSave = -1;

	// not saved

	private boolean _rolledback;
	private boolean _endpointFailed;
	private boolean _restored;

}
