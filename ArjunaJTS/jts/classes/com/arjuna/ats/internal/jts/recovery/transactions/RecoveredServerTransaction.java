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
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveredServerTransaction.java 2342 2006-03-30 13:06:17Z  $
 */


package com.arjuna.ats.internal.jts.recovery.transactions;

import com.arjuna.ats.internal.jts.Implementations;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.*;
import org.omg.CosTransactions.*;
import java.io.IOException;
import java.util.Date;

import com.arjuna.ats.jts.utils.Utility;

import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.orbspecific.interposition.*;
import com.arjuna.ats.internal.jts.recovery.contact.StatusChecker;
import com.arjuna.ats.arjuna.exceptions.*;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.coordinator.*;
import com.arjuna.ats.arjuna.objectstore.*;
import com.arjuna.ats.arjuna.state.*;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.OBJECT_NOT_EXIST;

import org.omg.CORBA.TRANSIENT;


/**
 * Transaction type only instantiated at recovery time.  This is used
 * to re-activate the state of a server transaction that did not
 * terminate correctly due to failure.
 * <P>
 * @author Dave Ingham (dave@arjuna.com)
 * @version $Id: RecoveredServerTransaction.java 2342 2006-03-30 13:06:17Z  $
 *
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_1 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_1] - RecoveredServerTransaction {0} created
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_2 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_2] - RecoveredServerTransaction - activate of {0} failed!
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_4 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_4] - RecoveredServerTransaction.replayPhase2({0}) - status = {1}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_5 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_5] - RecoveredServerTransaction.replayPhase2({0}) - status after contacting parent = {1}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_6 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_6] - ServerTransaction {0} unable determine status - retry later
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_7 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_7] - RecoveredServerTransaction.replayPhase2: unexpected Status: {0}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_8 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_8] - RecoveredServerTransaction.replayPhase2: ({0}) finished
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_9 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_9] - RecoveredServerTransaction.getStatusFromParent - replay_completion status = {0}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_10 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_10] - Got TRANSIENT from ORB for tx {0} and assuming OBJECT_NOT_EXIST
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_11 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_11] - RecoveredServerTransaction.getStatusFromParent - replay_completion got object_not_exist = {0}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_12 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_12] - RecoveredServerTransaction: caught NotPrepared
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_13 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_13] - RecoveredServerTransaction: caught unexpected exception: 
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_14 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_14] - RecoveredServerTransaction: {0} is invalid
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_15 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_15] - RecoveredServerTransaction:getStatusFromParent - no recovcoord or status not prepared
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_16 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_16] -  "RecoveredServerTransaction.unpackHeader - txid = {0} and processUid = {1}
 * @message com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_17 [com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_17] - RecoveredServerTransaction - activate of {0} failed with {1}
 */

public class RecoveredServerTransaction extends ServerTransaction
			implements RecoveringTransaction
{
    /**
     * actionUid is the local transaction identification for the remote
     * transaction - the name of the store entry which contains the state
     * of the server transaction. The actual main transaction id is only
     * obtained when we activate the transaction.
     */

    public RecoveredServerTransaction ( Uid actionUid )
    {
        this(actionUid, "");
    }

    /**
     * actionUid is the local transaction identification for the remote
     * transaction - the name of the store entry which contains the state
     * of the server transaction. The actual main transaction id is only
     * obtained when we activate the transaction.
     */

    public RecoveredServerTransaction ( Uid actionUid, String changedTypeName )
    {
	super(actionUid);

	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_1", new Object[]{getSavingUid()});
	    }

	// Don't bother trying to activate a transaction that isn't in
	// the store. This saves an error message.
	_recoveryStatus = RecoveryStatus.ACTIVATE_FAILED;

	String effectiveTypeName = typeName();
	
	if ( changedTypeName.length() < 1) {
	    _typeName = null;
	} else {
	    _typeName = changedTypeName;
	    effectiveTypeName = changedTypeName;
	}

	_originalProcessUid = new Uid(Uid.nullUid());
	
	try
	{
	    if ((store().currentState(getSavingUid(), effectiveTypeName) != ObjectStore.OS_UNKNOWN))
	    {
		/*
		 * By activating the state we get the actual transaction
		 * id and process id, which are needed for recovery
		 * purposes.
		 */

		if (activate())
		    _recoveryStatus = RecoveryStatus.ACTIVATED;
		else { 
		    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_2", new Object[]{getSavingUid()});
		};
	    }
	}
	catch (Exception e)
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_2", new Object[]{getSavingUid(), e});
	}

	_txStatus = Status.StatusUnknown;
    }

    /**
     * Get the status of the transaction. If we successfully activated
     * the transaction then we return whatever the transaction reports
     * otherwise we return RolledBack as we're using presumed abort.
     */
    public synchronized Status get_status () throws SystemException
    {
	if (_txStatus != Status.StatusUnknown)
	    return _txStatus;

	Status theStatus = Status.StatusUnknown;

	if (_recoveryStatus == RecoveryStatus.ACTIVATE_FAILED)
	    theStatus = Status.StatusRolledBack;
	else
	    theStatus = super.get_status();

	return theStatus;
    }

    /**
     * Allows a new Resource to be added to the transaction. Typically
     * this is used to replace a Resource that has failed and cannot
     * be recovered on it's original IOR.
     */
    public void addResourceRecord (Uid rcUid, Resource r)
    {
	Coordinator coord = null;
	AbstractRecord corbaRec = createOTSRecord(true, r, coord, rcUid);

	addRecord(corbaRec);
    }


    /**
     * Causes phase 2 of the commit protocol to be replayed.
     */
    public void replayPhase2()
    {
	_recoveryStatus = RecoveryStatus.REPLAYING;

	Status theStatus = get_status();

	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_4", new Object[]{get_uid(), Utility.stringStatus(theStatus)});
	    }

	if (theStatus == Status.StatusPrepared)
	{
	    /*
	     * We need to get the status from the our parent transaction
	     * in the interposition hierarchy.
	     */
	    theStatus = getStatusFromParent();
	  
	    if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_5", new Object[]{get_uid(), Utility.stringStatus(theStatus)});
		}
	}

	if ( (theStatus == Status.StatusCommitting) ||
	     (theStatus == Status.StatusCommitted) )
	{
	  phase2Commit(_reportHeuristics);

	  _recoveryStatus = RecoveryStatus.REPLAYED;

	  _txStatus = Status.StatusCommitted;
	}
	else if ( (theStatus == Status.StatusRolledBack) ||
		  (theStatus == Status.StatusRollingBack) ||
		  (theStatus == Status.StatusMarkedRollback) ||
		  (theStatus == Status.StatusNoTransaction) )
	{
	  phase2Abort(_reportHeuristics);

	  _recoveryStatus = RecoveryStatus.REPLAYED;

	  _txStatus = Status.StatusRolledBack;
	}
	else if ( theStatus == Status.StatusUnknown )
	{
	    if (jtsLogger.loggerI18N.isInfoEnabled())
		{
		    jtsLogger.loggerI18N.info("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_6", new Object[]{get_uid()});
		}
	    _recoveryStatus = RecoveryStatus.REPLAY_FAILED;
	}
	else
	{
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_7", new Object[]{Utility.stringStatus(theStatus)});
	    _recoveryStatus = RecoveryStatus.REPLAY_FAILED;
	}


	 if (jtsLogger.loggerI18N.isDebugEnabled())
		{
		    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					       FacilityCode.FAC_CRASH_RECOVERY, 
					       "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_8", new Object[]{get_uid()});
		}
    }

    /**
     * Get the status of recovery for this transaction
     */
    public int getRecoveryStatus ()
    {
	return _recoveryStatus;
    }

/**
 * Check the status of this transaction state, i.e., that represented
 * by get_uid and not getSavingUid
 */

public Status getOriginalStatus()
{
    if (_recoveryStatus != RecoveryStatus.ACTIVATE_FAILED)
    {
	try
	{
	    /*
	     * Remember to get the status on the actual global transaction
	     * and not on the local branch, i.e., use get_uid and not
	     * getSavingUid
	     */

	    return StatusChecker.get_status(get_uid(), _originalProcessUid);
	}
	catch (Inactive ex)
	{
	    // shouldn't happen!!

	    return Status.StatusUnknown;
	}
    }
    else
    {
	// if it can't be activated, we cant get the process uid
	return Status.StatusUnknown;
    }

}


    private Status getStatusFromParent ()
    {
      org.omg.CosTransactions.Status theStatus = org.omg.CosTransactions.Status.StatusUnknown;

      int not_exist_count; //This variable is applied with Orbix

      if ((super._recoveryCoordinator != null) && (get_status() == org.omg.CosTransactions.Status.StatusPrepared))
      {
	ServerControl sc = new ServerControl((ServerTransaction) this);
	ServerRecoveryTopLevelAction tla = new ServerRecoveryTopLevelAction(sc);

	if (tla.valid())
	{
	  try
	  {
	    theStatus = super._recoveryCoordinator.replay_completion(tla.getReference());
	 
	    if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_9", new Object[]{Utility.stringStatus(theStatus)});
	    }
	  }

	  catch (TRANSIENT ex_trans)
	  {
          // orbix seems to count unreachable as transient, but we no longer support orbix
          jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_10", new Object[] {get_uid()});
          theStatus = org.omg.CosTransactions.Status.StatusRolledBack;
	  }
	  // What here what should be done for Orbix2000
	  catch (OBJECT_NOT_EXIST ex)
	  {
	      // i believe this state should be notran - ots explicitly objnotexist is
	      // rollback

	      theStatus = org.omg.CosTransactions.Status.StatusRolledBack;

	      //	    theStatus = org.omg.CosTransactions.Status.StatusNoTransaction;

	      if (jtsLogger.loggerI18N.isDebugEnabled())
		  {
		      jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
						 FacilityCode.FAC_CRASH_RECOVERY, 
						 "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_11", new Object[]{Utility.stringStatus(theStatus)});
		  }
	  }
	  catch (NotPrepared ex1)
	  {
	      jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_12");
	      theStatus = org.omg.CosTransactions.Status.StatusActive;
	  }
	  catch (Exception e)
	  {
	    // Unknown error, so better to do nothing at this stage.
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_13", e);
	  }
	}
	else {
	    jtsLogger.loggerI18N.warn("com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_14", new Object[]{get_uid()});
	};

	// Make sure we "delete" these objects when we are finished
	// with them or there will be a memory leak. If they are deleted
	// "early", and the root coordinator needs them then it will find
	// them unavailable, and will have to retry recovery later.

	sc = null;
	tla = null;
      }
      else
      {
	  if (jtsLogger.loggerI18N.isDebugEnabled())
	      {
		  jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
					     FacilityCode.FAC_CRASH_RECOVERY, 
					     "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_15");
	      }
      }
    
      return theStatus;
    }

    public boolean allCompleted ()
    {
	//	return (boolean) (_recoveryStatus == RecoveryStatus.REPLAYED);

	return false;
    }

    public String type()
    {
	if (_typeName == null) {
	    return super.type();
	} else {
	    return _typeName;
	}
    }

    public void removeOldStoreEntry()
    {
	try
	{
	    store().remove_committed(getSavingUid(), super.type());
	}
	catch (ObjectStoreException ex)
	{
	    jtsLogger.loggerI18N.warn("RecoveredServerTransaction.removeOldStoreEntry ", ex);
	}
    }
    
    public boolean assumeComplete()
    {
	_typeName = AssumedCompleteServerTransaction.typeName();
	return true;
    }
    /**
     * Override StateManager packHeader so it gets the original processUid, not
     * this process's
     *
     * @since JTS 2.1.
     */

protected void packHeader (OutputObjectState os, Uid txId,
			   Uid processUid) throws IOException
    {
	/*
	 * If there is a transaction present than pack the process Uid of
	 * this JVM and the tx id. Otherwise pack a null Uid.
	 */
	
	super.packHeader(os, get_uid(), _originalProcessUid);
    }

    /**
     * Override StateManager's unpackHeader to save the processUid of the
     * original process
     *
     * @since JTS 2.1.
     */

protected void unpackHeader (InputObjectState os, Uid txId,
			     Uid processUid) throws IOException
    {
	_originalProcessUid = new Uid(Uid.nullUid());
	super.unpackHeader(os, super.objectUid, _originalProcessUid);

	if (jtsLogger.loggerI18N.isDebugEnabled())
	{
	    jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "com.arjuna.ats.internal.jts.recovery.transactions.RecoveredServerTransaction_16", new Object[]{get_uid(), _originalProcessUid});
	}
    }

public boolean save_state (OutputObjectState objectState, int ot)
{
    // do the other stuff
    boolean result = super.save_state(objectState,ot);
    
   // iff assumed complete, include the time (this should happen only once)
    if (_typeName != null && result) {
	Date lastActiveTime = new Date();
	try {
	    objectState.packLong(lastActiveTime.getTime());
	} catch (java.io.IOException ex) {
	}
    }
    return result;
}

/** do not admit to being inactive */
public Date getLastActiveTime()
{
    return null;
}

    protected Uid _originalProcessUid;

    private String _typeName;
    private boolean  _reportHeuristics = false;
    private int	 _recoveryStatus = RecoveryStatus.NEW;

    private org.omg.CosTransactions.Status _txStatus;
}
