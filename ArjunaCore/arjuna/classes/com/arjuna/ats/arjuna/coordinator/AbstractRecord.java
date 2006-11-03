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
 * Hewlett Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: AbstractRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.coordinator;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.state.*;
import com.arjuna.ats.arjuna.gandiva.ClassName;
import com.arjuna.ats.arjuna.gandiva.inventory.Inventory;

import com.arjuna.common.util.logging.*;

import java.io.PrintWriter;
import java.io.IOException;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

/**
 * Abstract Record Class
 * 
 * This class provides an abstract template that defines the interface that the
 * atomic action system uses to notify objects that various state transitions
 * have occurred as the 2PC protocol executes. Record types derived from this
 * class manage certain properties of objects such as recovery information,
 * concurrency control information etc, and all must redifine the operations
 * defined here as abstract to take appropriate action.
 * 
 * Many functions are declared pure virtual to force a definition to occur in
 * any derived class. These are currently all functions dealing with atomic
 * action coordination as well as the following list management functions:
 * typeIs: returns the record type of the instance. This is one of the values of
 * the enumerated type Record_type value: Some arbitrary value associated with
 * the record instance merge: Used when two records need to merge togethor.
 * Currently this is only used by CadaverRecords to merge information from
 * PersistenceRecords shouldAdd: returns TRUE is the record should be added to
 * the list FALSE if it should be discarded shouldMerge: returns TRUE is the two
 * records should be merged into a single record, FALSE if it should be
 * discarded shouldReplace: returns TRUE if the record should replace an
 * existing one, FALSE otherwise.
 * 
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: AbstractRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since 1.0.
 */

public abstract class AbstractRecord extends StateManager
{

	/**
	 * @return <code>RecordType</code> value.
	 */

	public abstract int typeIs ();

	/**
	 * If this abstract record caused a heuristic then it should return an
	 * object which implements <code>HeuristicInformation</code>
	 * 
	 * @return <code>Object</code> to be used to order.
	 */

	public abstract Object value ();

	public abstract void setValue (Object o);

	/**
	 * Only used for crash recovery, so most records don't need it.
	 * 
	 * @return <code>ClassName</code> to identify this abstract record.
	 */

	public ClassName className ()
	{
		return new ClassName("" + typeIs());
	}

	/**
	 * Atomic action interface - one operation per two-phase commit state.
	 */

	/**
	 * A rollback of a nested transaction has occurred.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

	public abstract int nestedAbort ();

	/**
	 * A commit of a nested transaction has occurred.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

	public abstract int nestedCommit ();

	/**
	 * A prepare for a nested transaction has occurred.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

	public abstract int nestedPrepare ();

	/**
	 * A rollback of a top-level transaction has occurred.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

	public abstract int topLevelAbort ();

	/**
	 * A commit of a top-level transaction has occurred.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

	public abstract int topLevelCommit ();

	/**
	 * A prepare for a top-level transaction has occurred.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

	public abstract int topLevelPrepare ();

	/**
	 * Return the Uid of this abstract record so that it can be ordered in the
	 * intentions list. This is also the Uid that the record was saved with in
	 * the object store.
	 * 
	 * @return <code>Uid</Uid> for this instance.
	 * @see com.arjuna.ats.arjuna.common.Uid
	 */

	/*
	 * Now that StateManager actually maintains state, we could save this in
	 * StateManager. However, it would affect all StateManager instances (more
	 * state to save).
	 */

	public Uid order ()
	{
		return uidOfObject;
	}

	/**
	 * Return the type of the abstract record. Used in ordering the instances in
	 * the intentions list. This is also the type that the record was saved with
	 * in the object store.
	 * 
	 * @return <code>String</code> representing type.
	 */

	public String getTypeOfObject ()
	{
		return typeOfObject;
	}

	/**
	 * Determine if records are discarded on action abort or must be propagated
	 * to parents.
	 * 
	 * @return <code>true</code> if the record should be propagated to the
	 *         parent transaction if the current transaction rolls back,
	 *         <code>false</code> otherwise. The default is <code>false</code>.
	 */

	public boolean propagateOnAbort ()
	{
		return false;
	}

	/**
	 * Determine if records are discarded on action commit or must be propagated
	 * to parents.
	 * 
	 * @return <code>true</code> if the record should be propagated to the
	 *         parent transaction if the current transaction commits,
	 *         <code>false</code> otherwise. The default is <code>true</code>.
	 */

	public boolean propagateOnCommit ()
	{
		return true;
	}

	/**
	 * Operators for comparing and sequencing instances of classes derived from
	 * AbstractRecords. Records are ordered primarily based upon the value of
	 * 'order', followed by 'typeIs'.
	 */

	/**
	 * Determine if two records are equal in that both are the same type and
	 * have the same order value (determined via 'order()').
	 * 
	 * @return <code>true</code> if equal, <code>false</code> otherwise.
	 */

	public final boolean equals (AbstractRecord ar)
	{
		return ((com.arjuna.ats.arjuna.common.Configuration.useAlternativeOrdering()) ? typeEquals(ar)
				: orderEquals(ar));
	}

	/**
	 * Determine if two records are less than in that both are the same type and
	 * their Uids are less than.
	 * 
	 * @return <code>true</code> if equal, <code>false</code> otherwise.
	 */

	public final boolean lessThan (AbstractRecord ar)
	{
		return ((com.arjuna.ats.arjuna.common.Configuration.useAlternativeOrdering()) ? typeLessThan(ar)
				: orderLessThan(ar));
	}

	/**
	 * Determine if two records are greater than in that both are the same type
	 * and their Uids are greater than.
	 * 
	 * @return <code>true</code> if equal, <code>false</code> otherwise.
	 */

	public final boolean greaterThan (AbstractRecord ar)
	{
		return ((com.arjuna.ats.arjuna.common.Configuration.useAlternativeOrdering()) ? typeGreaterThan(ar)
				: orderGreaterThan(ar));
	}

	/**
	 * Cleanup is called if a top-level action is detected to be an orphan.
	 * 
	 * NOTE nested actions are never orphans since their parents would be
	 * aborted we may as well abort them as well.
	 * 
	 * @return <code>TwoPhaseOutcome</code> as default is the same as
	 *         topLevelAbort.
	 */

	public int topLevelCleanup ()
	{
		return topLevelAbort();
	}

	/**
	 * Cleanup is called if a nested is detected to be an orphan.
	 * 
	 * NOTE nested actions are never orphans since their parents would be
	 * aborted we may as well abort them as well.
	 * 
	 * @return <code>TwoPhaseOutcome</code> as default is the same as
	 *         nestedAbort.
	 */

	public int nestedCleanup ()
	{
		return nestedAbort();
	}

	/**
	 * Should this record be saved in the intentions list? If the record is
	 * saved, then it may be recovered later in the event of a failure. Note,
	 * however, that the size of the intentions list on disk is critical to the
	 * performance of the system (disk I/O is a bottleneck).
	 * 
	 * @return <code>true</code> if it should be saved, <code>false</code>
	 *         otherwise. <code>false</code> is the default.
	 */

	public boolean doSave ()
	{
		return false;
	}

	/**
	 * Re-implementation of abstract methods inherited from base class.
	 */

	public String type ()
	{
		return "/StateManager/AbstractRecord";
	}

	/**
	 * Write information about this specific instance to the specified stream.
	 * 
	 * @param PrintWriter
	 *            strm the stream on which to output.
	 */

	public void print (PrintWriter strm)
	{
		strm.println("Uid of Managed Object: " + uidOfObject);
		strm.println("Type of Managed Object: " + typeOfObject);
		super.print(strm);
	}

	/**
	 * When the transaction is required to make the intentions list persistent,
	 * it scans the list and asks each record whether or not it requires state
	 * to be saved (by calling doSave). If the answer is yes, then save_state is
	 * called and the record instance must save enough information to enable it
	 * to be restored from that state later. The basic AbstractRecord save_state
	 * will save common data that is required by the base class during recovery.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean save_state (OutputObjectState os, int i)
	{
		try
		{
			uidOfObject.pack(os);
			os.packString(typeOfObject);

			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	/**
	 * During recovery, the transaction log is given to the recovery system and
	 * it will recreate a transaction instance to perform necessary recovery
	 * actions. This transaction will recreate the intentions list and give each
	 * recreated AbstractRecord the state that that was saved during transaction
	 * persistence. The base class will restore information that it needs from
	 * the log.
	 * 
	 * @return <code>true</code> if successful, <code>false</code>
	 *         otherwise.
	 */

	public boolean restore_state (InputObjectState os, int i)
	{
		typeOfObject = null;

		try
		{
			uidOfObject.unpack(os);
			typeOfObject = os.unpackString();

			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	/**
	 * Forget any heuristic outcome which this implementation may have produced.
	 * 
	 * @return <code>true</code> by default. If <code>false</code> is
	 *         returned then the instance must be remembered by the transaction
	 *         (in the log) in order for recovery to retry later or for a system
	 *         administrator to be able to determine which resources have not
	 *         been successfully completed.
	 */

	public boolean forgetHeuristic ()
	{
		return true;
	}

	/**
	 * Perform a nested one phase commit.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

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

	/**
	 * Perform a top-level one phase commit.
	 * 
	 * @return <code>TwoPhaseOutcome</code> to indicate success/failure.
	 * @see com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome
	 */

	public int topLevelOnePhaseCommit ()
	{
		int res = topLevelPrepare();

		switch (res)
		{
		case TwoPhaseOutcome.PREPARE_OK:
			return topLevelCommit();
		case TwoPhaseOutcome.PREPARE_READONLY:
			return TwoPhaseOutcome.FINISH_OK;
		default:
			return TwoPhaseOutcome.FINISH_ERROR;
		}
	}

	/*
	 * This could be done through interface/implementation separation for
	 * AbstractRecords. However, since we only need to dynamically create
	 * records for crash recovery purposes we may not need such flexibility.
	 */

	/**
	 * @return a newly created instance of the derived class specified by the
	 *         ClassName, or null if there is nothing defined in the inventory.
	 * @see com.arjuna.ats.arjuna.gandiva.ClassName
	 */

	public static AbstractRecord create (ClassName cName)
	{
		Object ptr = Inventory.inventory().createVoid(cName);
		AbstractRecord record = null;

		if (ptr instanceof AbstractRecord)
			record = (AbstractRecord) ptr;
		else
			record = null;

		return record;
	}

	/*
	 * We would like these to be protected, but the requirement for a
	 * RecoveryAbstractRecord interface which is in a different package prevents
	 * this.
	 */

	/**
	 * Merge the current record with the one presented.
	 * 
	 * @param AbstractRecord
	 *            a the record with which to merge.
	 */

	public abstract void merge (AbstractRecord a);

	/**
	 * Alter the current record with the one presented.
	 * 
	 * @param AbstractRecord
	 *            a the record with which to alter.
	 */

	public abstract void alter (AbstractRecord a);

	/**
	 * Should we add the record presented to the intentions list?
	 * 
	 * @param AbstractRecord
	 *            a The record to try to add.
	 * @return <code>true</code> if the record should be added,
	 *         <code>false</code> otherwise.
	 */

	public abstract boolean shouldAdd (AbstractRecord a);

	/**
	 * Should we alter the current record with the one presented?
	 * 
	 * @param AbstractRecord
	 *            a The record to try to alter.
	 * @return <code>true</code> if the record should be altered,
	 *         <code>false</code> otherwise.
	 */

	public abstract boolean shouldAlter (AbstractRecord a);

	/**
	 * Should we merge the current record with the one presented?
	 * 
	 * @param AbstractRecord
	 *            a The record to try to merge.
	 * @return <code>true</code> if the record should be merged,
	 *         <code>false</code> otherwise.
	 */

	public abstract boolean shouldMerge (AbstractRecord a);

	/**
	 * Should we replace the record presented with the current record?
	 * 
	 * @param AbstractRecord
	 *            a The record to try to replace.
	 * @return <code>true</code> if the record should be replaced,
	 *         <code>false</code> otherwise.
	 */

	public abstract boolean shouldReplace (AbstractRecord a);

	/**
	 * The current record is about to replace the one presented. This method is
	 * invoked to give the current record a chance to copy information, for
	 * example, from the record being replaced.
	 * 
	 * @param AbstractRecord
	 *            a the record that will replace this instance.
	 */

	public void replace (AbstractRecord a)
	{
	}

	/**
	 * These few functions are link manipulation primitives used by the
	 * RecordList processing software to chain instances together.
	 * 
	 * @return the previous element in the intentions list, or null.
	 */

	protected final AbstractRecord getPrevious ()
	{
		return previous;
	}

	/**
	 * @return the next element in the intentions list, or null.
	 */

	protected final AbstractRecord getNext ()
	{
		return next;
	}

	/**
	 * Set the previous element in the list to the specified instance.
	 * 
	 * @param AbstractRecord
	 *            ar the instance to become previous.
	 */

	protected final void setPrevious (AbstractRecord ar)
	{
		previous = ar;
	}

	/**
	 * Set the next element in the list to the specified instance.
	 * 
	 * @param AbstractRecord
	 *            ar the instance to become next.
	 */

	protected final void setNext (AbstractRecord ar)
	{
		next = ar;
	}

	/**
	 * Create a new instance with the specified paramaters.
	 * 
	 * @param Uid
	 *            storeUid the unique id for this instance.
	 * @param String
	 *            objType the type of the instance.
	 * @param int
	 *            otype the ObjectType of the object.
	 * @see com.arjuna.ats.arjuna.ObjectType
	 */

	protected AbstractRecord (Uid storeUid, String objType, int otype)
	{
		super(otype);

		next = null;
		previous = null;
		uidOfObject = storeUid;
		typeOfObject = objType;

		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ABSTRACT_REC, "AbstractRecord::AbstractRecord ("
					+ storeUid + ", " + otype + ")");
		}
	}

	/**
	 * Create a new instance with the specified paramaters.
	 * 
	 * @param Uid
	 *            storeUid the unique id for this instance.
	 */

	protected AbstractRecord (Uid storeUid)
	{
		super(storeUid);

		next = null;
		previous = null;
		uidOfObject = storeUid;
		typeOfObject = null;

		if (tsLogger.arjLogger.debugAllowed())
		{
			tsLogger.arjLogger.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ABSTRACT_REC, "AbstractRecord::AbstractRecord ("
					+ storeUid + ")");
		}
	}

	/**
	 * Creates a 'blank' abstract record. This is used during crash recovery
	 * when recreating the prepared list of a server atomic action.
	 * 
	 * @message com.arjuna.ats.arjuna.coordinator.AbstractRecord_1
	 *          [com.arjuna.ats.arjuna.coordinator.AbstractRecord_1] -
	 *          AbstractRecord::AbstractRecord () - crash recovery constructor
	 */

	protected AbstractRecord ()
	{
		super(Uid.nullUid());

		next = null;
		previous = null;
		uidOfObject = new Uid(Uid.nullUid());
		typeOfObject = null;

		if (tsLogger.arjLoggerI18N.debugAllowed())
		{
			tsLogger.arjLoggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PROTECTED, FacilityCode.FAC_ABSTRACT_REC, "com.arjuna.ats.arjuna.coordinator.AbstractRecord_1");
		}
	}

	/**
	 * ensure records of the same type are grouped together in the list, rather
	 * than grouping them by object (i.e. uid)
	 */

	private final boolean typeEquals (AbstractRecord ar)
	{
		return ((typeIs() == ar.typeIs()) && (order().equals(ar.order())));
	}

	private final boolean typeLessThan (AbstractRecord ar)
	{
		return ((typeIs() < ar.typeIs()) || ((typeIs() == ar.typeIs()) && (order().lessThan(ar.order()))));
	}

	private final boolean typeGreaterThan (AbstractRecord ar)
	{
		return ((typeIs() > ar.typeIs()) || ((typeIs() == ar.typeIs()) && (order().greaterThan(ar.order()))));
	}

	private final boolean orderEquals (AbstractRecord ar)
	{
		return ((order().equals(ar.order())) && (typeIs() == ar.typeIs()));
	}

	private final boolean orderLessThan (AbstractRecord ar)
	{
		return ((order().lessThan(ar.order())) || ((order().equals(ar.order())) && (typeIs() < ar.typeIs())));
	}

	private final boolean orderGreaterThan (AbstractRecord ar)
	{
		return ((order().greaterThan(ar.order())) || ((order().equals(ar.order())) && (typeIs() > ar.typeIs())));
	}

	private AbstractRecord next;
	private AbstractRecord previous;
	private Uid uidOfObject;
	private String typeOfObject;

}
