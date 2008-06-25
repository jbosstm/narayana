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
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: ResourceRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.resources;

/*
 * 
 * OTS Resource Record Class Implementation
 *
 */

import com.arjuna.ats.jts.CosTransactionsNames;
import com.arjuna.ats.jts.logging.*;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.arjuna.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;

import com.arjuna.orbportability.*;

import com.arjuna.common.util.logging.*;

import org.omg.CosTransactions.*;

import com.arjuna.ArjunaOTS.*;

import java.io.PrintWriter;

import org.omg.CORBA.CompletionStatus;
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
 * Arjuna abstract record to handle CORBA (SubtransactionAware)Resource
 * interface.
 *
 * The OTS handling of Resources is bizarre (by Arjuna standards) and confusing
 * Our current understanding is:
 *
 * Resources registered using 'register_resource' ONLY take
 * part in top-level events
 *
 * SubtransactionAwareResources registered using 'register_subtran_aware' ONLY
 * take part in commit/abort of the action in which they are registered - i.e.
 * they DO NOT propagate automatically - the registering object must do the
 * propagation itself using the parent arg in the 'commit_subtransaction'
 * operation.
 *
 * If a SubtransactionAwareResource is registered with 'register_resource' then
 * it will be propagated to the parent when the action commit. Otherwise it is
 * only registered with the current transaction.
 *
 * Subtransactions do not have a 'prepare' phase which can thus lead to
 * inconsistency. If they underwent the full 2-phase protocol (as they do in
 * Arjuna), then all nested participants will have to repond successfully
 * to prepare before they can be told to commit. The way the OTS mandates the
 * protocol, we could tell some to commit before being told by another resource
 * that it cannot commit! We then have to go through the resources and tell
 * them to abort! May cause heuristic decisions!
 *
 * The only problem is if a resource which does not propagate causes a nested
 * action to fail.
 *
 * SubtransactionAwareResources registered using 'register_resource' do appear
 * to propagate.
 *
 * UGH! Braindead!
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ResourceRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 *
 * @message com.arjuna.ats.internal.jts.resources.rrcaught {0} caught exception: {1}
 */

public class ResourceRecord extends com.arjuna.ats.arjuna.coordinator.AbstractRecord
{
    
    /**
     * Constructor
     *
     * @param propagate tells us whether to propagate the resource at nested
     * commit or not.
     * @param theResource is the proxy that allows us to call out to the
     * object.
     * @param myParent is the proxy for the parent coordinator needed in
     * commit_subtransaction.
     */

    public ResourceRecord (boolean propagate, Resource theResource,
			   Coordinator myParent, Uid recCoordUid,
			   ArjunaTransactionImple current)
    {
	super(new Uid(), null, ObjectType.ANDPERSISTENT);

	_parentCoordHandle = myParent;
	_resourceHandle = theResource;
	_stringifiedResourceHandle = null;
	_recCoordUid = (recCoordUid != null) ? (new Uid(recCoordUid)) : Uid.nullUid();
	_propagateRecord = propagate;
	_committed = false;
	_rolledback = false;
    }

    public void finalize () throws Throwable
    {
	_resourceHandle = null;
	_stringifiedResourceHandle = null;
	_recCoordUid = null;
	_parentCoordHandle = null;

	super.finalize();
    }

    public final Resource resourceHandle ()
    {
	/*
	 * After recovery we may have not been able to recreate the
	 * _resourceHandle due to the fact that the Resource itself
	 * may not be alive resulting in a failure to narrow the
	 * reference returned from string_to_object. In such cases we
	 * cache the stringied reference and retry the narrow when we
	 * need to use the _resourceHandle as at this point the
	 * Resource may have recovered.
	 */ 
	
	if ( (_resourceHandle == null) && (_stringifiedResourceHandle != null) )
	{
	    try
	    {
		org.omg.CORBA.ORB theOrb = ORBManager.getORB().orb();
		
		if (theOrb == null)
		    throw new UNKNOWN();

		if (jtsLogger.logger.isDebugEnabled())
		{
		    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord: About to string_to_object on "+_stringifiedResourceHandle);
		}

		org.omg.CORBA.Object optr = theOrb.string_to_object(_stringifiedResourceHandle);

		if (jtsLogger.logger.isDebugEnabled())
		{
		    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord: Successfully stringed to object, next try to narrow");
		}
		
		theOrb = null;
		
		_resourceHandle = org.omg.CosTransactions.ResourceHelper.narrow(optr);

		if (jtsLogger.logger.isDebugEnabled())
		{
		    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord: Successfully narrowed");
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
		// Failed to narrow to a Resource

		if (jtsLogger.logger.isDebugEnabled())
		{
		    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord: Failed to narrow to Resource");
		}
	    }
	}

	return _resourceHandle;
    }
    
    public boolean propagateOnCommit ()
    {
	return _propagateRecord;
    }

    public int typeIs ()
    {
	return RecordType.OTS_RECORD;
    }
    
    public ClassName className ()
    {
	return CosTransactionsNames.AbstractRecord_ResourceRecord();
    }
    
    public Object value ()
    {
	return _resourceHandle;
    }

    /**
     * @message com.arjuna.ats.internal.jts.resources.rrillegalvalue {0} called illegally.
     */

    public void setValue (Object o)
    {
	if (jtsLogger.loggerI18N.isWarnEnabled())
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.rrillegalvalue",
				      new Object[] {"ResourceRecord.set_value"} );
	}
    }

    /**
     * General nesting rules:
     *
     * Only SubtransactionAware resources get registered with nested actions. 
     * The ResourceRecord creator is assumed to ensure that plain Resources
     * are only registered with the appropriate top level action.
     *
     * That said the _propagateRecord flag ensures that resources registered
     * via register_subtran only take part in the action they where registered
     * in after which they are dropped
     */

    public int nestedAbort ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord::nestedAbort() for "+order());
	}

	/*
	 * We shouldn't need to check committed since aborted nested actions
	 * will drop these resources.
	 */

	SubtransactionAwareResource staResource = null;
	int o = TwoPhaseOutcome.FINISH_ERROR;

	try
	{
	    /*
	     * Must be an staResource to get here.
	     */
	    
	    staResource = org.omg.CosTransactions.SubtransactionAwareResourceHelper.narrow(resourceHandle());

	    if (staResource == null)
		throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
	}
	catch (Exception exEnv)
	{
	    // not a sub tran resource, so ignore;

	    o = TwoPhaseOutcome.FINISH_OK;
	}

	if (staResource != null)
	{
	    try
	    {
		staResource.rollback_subtransaction();
		o = TwoPhaseOutcome.FINISH_OK;
	    }
	    catch (Exception e)
	    {
		o = TwoPhaseOutcome.FINISH_ERROR;
	    }
	
	    staResource = null;
	}

	/*
	 * Now release the parent as it is about to be destroyed
	 * anyway.
	 *
	 * The parent may have already been released if abort is
	 * being called because commit failed.
	 */

	_parentCoordHandle = null;

	return o;
    }

    /**
     * If this resource handles nesting this was done in prepare
     * or it should be ignored. In either case returning FINISH_OK
     * suffices.
     */

    public int nestedCommit ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord::nestedCommit() for "+order());
	}

	return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * OTS does not use nested prepare at all!
     * However, to make nested commit/abort clean we do commit_subtransaction
     * here.
     * Note that we only get a prepare from Arjuna if the action is to be
     * committed so this is safe in Arjuna terms.
     */

    public int nestedPrepare ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord::nestedPrepare() for "+order());
	}

	int o = TwoPhaseOutcome.ONE_PHASE_ERROR;
	SubtransactionAwareResource staResource = null;
	
	try
	{
	    if (_committed)
		return TwoPhaseOutcome.PREPARE_OK;
	    else
		_committed = true;
	    
	    staResource = org.omg.CosTransactions.SubtransactionAwareResourceHelper.narrow(resourceHandle());

	    if (staResource == null)
		throw new BAD_PARAM(0, CompletionStatus.COMPLETED_NO);
	}
	catch (Exception e)
	{
	    /*
	     * Not subtran aware resource, so return PREPARE_OK.
	     * Resource will get invocations at top-level only.
	     */
	    
	    o = TwoPhaseOutcome.PREPARE_OK;
	}

	if (staResource != null)
	{
	    try
	    {
		staResource.commit_subtransaction(_parentCoordHandle);

		o = TwoPhaseOutcome.PREPARE_OK;
		
		staResource = null;
	    }
	    catch (Exception e)
	    {
		o = TwoPhaseOutcome.ONE_PHASE_ERROR;
	    }
	}
	    
	/*
	 * Now release the parent as it is about to be destroyed
	 * anyway.
	 */

	_parentCoordHandle = null;
	
	return o;
    }

    public int topLevelAbort ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord::topLevelAbort() for "+order());
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
	catch (HeuristicCommit exEnv)
	{
	    if (_rolledback)
		return TwoPhaseOutcome.HEURISTIC_HAZARD;  // participant lied in prepare!
	    else
		return TwoPhaseOutcome.HEURISTIC_COMMIT;
	}
	catch (HeuristicMixed ex1)
	{
	    return TwoPhaseOutcome.HEURISTIC_MIXED;
	}
	catch (HeuristicHazard ex2)
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
	catch (SystemException ex3)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.rrcaught",
					  new Object[] {"ResourceRecord.topLevelAbort", ex3} );
	    }
	    
	    return TwoPhaseOutcome.FINISH_ERROR;
	}
    
	return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelCommit ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord::topLevelCommit() for "+order());
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
	catch (NotPrepared ex1)
	{
	    return TwoPhaseOutcome.NOT_PREPARED;
	}
	catch (HeuristicRollback ex2)
	{
	    return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
	}
	catch (HeuristicMixed ex3)
	{
	    return TwoPhaseOutcome.HEURISTIC_MIXED;
	}
	catch (HeuristicHazard ex4)
	{
	    return TwoPhaseOutcome.HEURISTIC_HAZARD;
	}
	catch (SystemException ex5)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.rrcaught",
					  new Object[] {"ResourceRecord commit -", ex5} );
	    }

	    return TwoPhaseOutcome.FINISH_ERROR;
	}

	return TwoPhaseOutcome.FINISH_OK;
    }

    public int topLevelPrepare ()
    {
	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), "ResourceRecord::topLevelPrepare() for "+order());
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
	catch (HeuristicMixed ex)
	{
	    return TwoPhaseOutcome.HEURISTIC_MIXED;
	}
	catch (HeuristicHazard ex)
	{
	    return TwoPhaseOutcome.HEURISTIC_HAZARD;
	}
	catch (Exception e)
	{
	    return TwoPhaseOutcome.PREPARE_NOTOK;
	}

	return TwoPhaseOutcome.PREPARE_NOTOK;
    }
    
    public int nestedOnePhaseCommit ()
    {
	int res = nestedPrepare();

	switch (res)
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
	    return TwoPhaseOutcome.FINISH_ERROR;
	}
	catch (INVALID_TRANSACTION e5)
	{
	    return TwoPhaseOutcome.FINISH_ERROR;
	}
	catch (Exception e5)
	{
	    /*
	     * Unknown error - better assume heuristic!
	     */
	    
	    return TwoPhaseOutcome.HEURISTIC_HAZARD;
	}

	return TwoPhaseOutcome.FINISH_OK;
    }

    /**
     * @message com.arjuna.ats.internal.jts.resources.rrinvalid {0} called without a resource!
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
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.rrinvalid",
					      new Object[] {"ResourceRecord.forgetHeuristic"} );
		}
	    }
	}
	catch (SystemException e)
	{
	    if (jtsLogger.loggerI18N.isWarnEnabled())
	    {
		jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.resources.rrcaught",
					  new Object[] {"ResourceRecord.forgetHeuristic", e} );
	    }
	}

	return false;
    }

    public static AbstractRecord create ()
    {
	return new ResourceRecord();
    }

    public static void remove (AbstractRecord toDelete)
    {
	toDelete = null;
    }
    
    public void print (PrintWriter strm)
    {
	super.print(strm);

	strm.print("Resource Record");
	strm.print(_resourceHandle+"\t"+_parentCoordHandle+"\t"+_propagateRecord);
    }

    /**
     * restore_state and save_state for ResourceRecords doesn't generally
     * apply due to object pointers. However, we need to save something so we
     * can recover failed transactions. So, rather than insist that all
     * Resources derive from a class which we can guarantee will give us some
     * unique id, we simply rely on string_to_object and object_to_string to
     * be meaningful.
     */

    public boolean restore_state (InputObjectState os, int t)
    {
	int isString = 0;
	boolean result = super.restore_state(os, t);

	if (!result)
	    return false;
	
	try
	{
	    isString = os.unpackInt();

	    /*
	     * Do we need to restore the parent coordinator handle?
	     */

	    _parentCoordHandle = null;
	    
	    if (isString == 1)
	    {
		_stringifiedResourceHandle = os.unpackString();

		/*
		 * Could call resourceHandle() here to restore the
		 * _resourceHandle reference but no loss in doing it
		 * lazily.  
		 */

		// Unpack recovery coordinator Uid

		_recCoordUid.unpack(os);

		if (jtsLogger.logger.isDebugEnabled())
		{
		    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							 (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), 
							 "ResourceRecord.restore_state: unpacked rec co with uid="+_recCoordUid);
		}
	    }
	}
	catch (IOException e)
	{
	    result = false;
	}

	return result;
    }

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
	     * If we have a _resourceHandle then we stringify it and
	     * pack the string.  Failing that if we have a cached
	     * stringified version (in _stringifiedResourceHandle)
	     * then we pack that. If we have neither then we're
	     * doomed.  
	     */

	    if ( (resourceHandle() == null) && (_stringifiedResourceHandle == null) )
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
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							     (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), 
							     "ResourceRecord: packed obj ref "+stringRef);
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
		    _recCoordUid.pack(os);

		    if (jtsLogger.logger.isDebugEnabled())
		    {
			jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
							     (com.arjuna.ats.jts.logging.FacilityCode.FAC_OTS | com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC), 
							     "Packed rec co uid of "+_recCoordUid);
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
	return "/StateManager/AbstractRecord/ResourceRecord";
    }

    public boolean doSave ()
    {
	return true;
    }

    public final Uid getRCUid ()
    {
	return _recCoordUid;
    }

    public void merge (AbstractRecord a)
    {
    }

    public void alter (AbstractRecord a)
    {
    }

    public boolean shouldAdd (AbstractRecord a)
    {
	return false;
    }

    public boolean shouldAlter (AbstractRecord a)
    {
	return false;
    }

    public boolean shouldMerge (AbstractRecord a)
    {
	return false;
    }

    public boolean shouldReplace (AbstractRecord rec)
    {
	boolean replace = false;
	
	if (rec != null)
	{
	    if (rec.typeIs() == typeIs())
	    {
		ResourceRecord newRec = (ResourceRecord) rec;

		/*
		 * Check whether the new record corresponds to the same
		 * RecoveryCoordinator as this one. If so replace.
		 * Don't replace if the uids are NIL_UID
		 */

		if ( ( _recCoordUid.notEquals(Uid.nullUid()) ) && (_recCoordUid.equals(newRec.getRCUid())) )
		{
		    replace = true;
		}
	    }
	}

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
						 (com.arjuna.ats.arjuna.logging.FacilityCode.FAC_ABSTRACT_REC),
						 "ResourceRecord: shouldReplace() = "+replace);
	}

	return replace;
    }

    /*
     * Protected constructor used by crash recovery.
     */

    protected ResourceRecord (boolean propagate, Resource theResource,
			      Uid recCoordUid)
    {
	super(new Uid(), null, ObjectType.ANDPERSISTENT);

	_parentCoordHandle = null;
	_resourceHandle = theResource;
	_stringifiedResourceHandle = null;
	_recCoordUid = (recCoordUid != null) ? (new Uid(recCoordUid)) : Uid.nullUid();
	_propagateRecord = propagate;
	_committed = false;
	_rolledback = false;
    }
	
    protected ResourceRecord ()
    {
	super();

	_parentCoordHandle = null;
	_resourceHandle = null;
	_stringifiedResourceHandle = null;
	_recCoordUid = new Uid(Uid.nullUid());
	_propagateRecord = false;
	_committed = false;
	_rolledback = false;
    }
    
    private Coordinator _parentCoordHandle;
    private Resource    _resourceHandle;
    private String      _stringifiedResourceHandle;
    private Uid	        _recCoordUid;
    private boolean     _propagateRecord;
    private boolean     _committed;
    private boolean     _rolledback;
    
}
