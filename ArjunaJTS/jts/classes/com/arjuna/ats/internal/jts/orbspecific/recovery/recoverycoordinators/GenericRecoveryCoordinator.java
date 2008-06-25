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
 * $Id: GenericRecoveryCoordinator.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.orbspecific.recovery.recoverycoordinators;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.objectstore.*;

import com.arjuna.ats.jts.common.jtsPropertyManager;
import com.arjuna.ats.arjuna.common.*;
import com.arjuna.orbportability.*;
import com.arjuna.common.util.propertyservice.PropertyManager;

import com.arjuna.ats.internal.jts.recovery.*;
import com.arjuna.ats.internal.jts.recovery.contact.StatusChecker;
import com.arjuna.ats.internal.jts.recovery.recoverycoordinators.*;
import com.arjuna.ats.internal.jts.recovery.transactions.*;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

import org.omg.CosTransactions.*;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.UNKNOWN;

import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.common.util.logging.*;


/**
 * This provides the ORB-independent recovery coordinator
 * functionality. The ORB-specific implementations delegate their work
 * to this.
 * <P>
 * @author Dave Ingham(dave.ingham@arjuna.com), Peter Furniss, Mark Little (mark.little@arjuna.com) Malik SAHEB (malik.saheb@arjuna.com
 * @version $Id: GenericRecoveryCoordinator.java 2342 2006-03-30 13:06:17Z  $ 
 *
 * @message com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_1 [com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_1] - GenericRecoveryCoordinator {0} constructed
 * @message com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_2 [com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_2] - GenericRecoveryCoordinator() constructing
 * @message com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_3 [com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_3] - GenericRecoveryCoordinator {0} destroyed
 * @message com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_4 [com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_4] - GenericRecoveryCoordinator - swapping Resource for RC {0}
 */

public class GenericRecoveryCoordinator extends org.omg.CosTransactions.RecoveryCoordinatorPOA
{

    /**
     * Normal constructor. Used both for creating a RecoveryCoordinator in
     * the same process as the Coordinator (where this is necessary) and 
     * when reactivating a RecoveryCoordinator as an implementation instance 
     * (i.e. <i>not</i> as POA default servant) from
     * stringified data in RecoveryManager from data received in a
     * RecoveryCoordinator object key. 
     * <p>Combines the parameters into a {@link RecoveryCoordinatorId}.
     */
    public GenericRecoveryCoordinator (Uid RCUid, Uid actionUid, 
				       Uid processUid, boolean isServerTransaction)
    {
	_id = new RecoveryCoordinatorId(RCUid, actionUid, processUid, isServerTransaction);

	if (jtsLogger.loggerI18N.isDebugEnabled())
	{
	    jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_1", new Object[]{_id});
	}
    }
    
    /** 
     * protected constructor used by default servant derived class (with POA orbs).
     * When used a default servant, there is only one GenericRecoveryCoordinator
     * instance, whose _id field is null.
     *
     */
    protected GenericRecoveryCoordinator()
    {
	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
			  FacilityCode.FAC_CRASH_RECOVERY, 
			  "com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_2");
	    }
	_id = null;
    }
    
    public void finalize () throws Throwable
    {
	super.finalize();
	if (jtsLogger.loggerI18N.isDebugEnabled())
	    {
		jtsLogger.loggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC, 
					   FacilityCode.FAC_CRASH_RECOVERY, 
					   "com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_3", new Object[]{_id});
	    }
    }

    /**
     * Implementation of IDL method:
     *  <p>
     *  Operation: <b>::CosTransactions::RecoveryCoordinator::replay_completion</b>.
     *  <pre>
     *  #pragma prefix "omg.org/CosTransactions/RecoveryCoordinator"
     *  ::CosTransactions::Status replay_completion(
     *  in ::CosTransactions::Resource r
     *  )
     *  raises(
     *  ::CosTransactions::NotPrepared
     *  );
     *  </pre>
     *  </p>
     * This method is used when the instance is used as a particular implementation
     * object (i.e. <i>not</i> as default servant). Delegates to the static 
     * {@link #replay_completion(RecoveryCoordinatorId, Resource) replay_completion}
     * using the {@link RecoveryCoordinatorId} made in the constructor.
    */

    public Status replay_completion ( Resource res ) throws SystemException, NotPrepared 
    {
	return GenericRecoveryCoordinator.replay_completion(_id, res);
    }

    /**
     * Respond to a replay_completion request for the RecoveryCoordinator
     * identified by parameter id.
     */

    protected static Status replay_completion ( RecoveryCoordinatorId id, Resource res ) throws SystemException, NotPrepared 
    {

	if (jtsLogger.logger.isDebugEnabled())
	{
	    jtsLogger.logger.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC, 
				   FacilityCode.FAC_CRASH_RECOVERY, 
				   "GenericRecoveryCoordinator(" + id._RCUid + ").replay_completion(" 
				   + ( res != null ? "resource supplied)" : "null resource)"));
	}

	Status currentStatus = Status.StatusUnknown;

	/* 
	 * First check to see if the transaction is active by asking the 
	 * per-process contact.
	 * If alive, return the status reported by the
	 * transaction.  If not alive then try and recover the
	 * transaction from the intentions list.  
	 */
    
	boolean transactionActive = true;

	try
	{
	    currentStatus = get_status(id._actionUid, id._originalProcessUid);
	}
	catch (Inactive e)
	{
	    // original process is dead.
	    transactionActive = false;
	}

	if (currentStatus == Status.StatusNoTransaction)
	{
	    /*
	     * There is no intentions list, so the transaction either
	     * committed or rolled back. However, this routine is only
	     * ever called by replay_completion, which means that there
	     * is a resource (hopefully one which was participating in
	     * the transaction) that is in doubt as to the
	     * transaction's outcome. If the transaction had committed,
	     * then this resource would know of the outcome. Therefore,
	     * it must have rolled back!
	     */

	    /*
	     * Unfortunately the last statement is wrong. There is a timing
	     * issue here: the resource recovery may be doing an upcall while
	     * the downcall (from coordinator recovery) is going on and
	     * removing the log. What can then happen is that a resource may
	     * see a commit folled by a rollback.
	     */

	    currentStatus = Status.StatusRolledBack;
	}
        else if ( currentStatus == Status.StatusCommitted )
        {
            /*
             * If the status returned is StatusCommitted, the only reason a
             * replay_completion request can come in is if the resource on 
             * the other end has not received the second phase and hence the 
             * transaction is in the process of committing and has not
	     * committed.
             */

            currentStatus = Status.StatusCommitting;
        }

	if (!transactionActive)
	{
	    // original process is dead, so reasonable for us to try to 
	    // recover

	    /*
	     * The RecoveredTransactionReplayer is a threaded object
	     * so we can get the status and return it while the
	     * replayer does the phase 2 commit in a new thread.  
	     */

	    String tranType = ( (id._isServerTransaction) ? ServerTransaction.typeName() : ArjunaTransactionImple.typeName() );

	    com.arjuna.ats.internal.jts.recovery.transactions.RecoveredTransactionReplayer replayer = new com.arjuna.ats.internal.jts.recovery.transactions.RecoveredTransactionReplayer(id._actionUid, tranType);

	    // this will cause the activatation attempt
	    currentStatus = replayer.getStatus();

	    /*
	     * If the transaction has been successfully activated and
	     * we've been given a new resource then add it to the
	     * intentions list. This will cause the old resource that
	     * corresponded to this RecoveryCoordinator to be replaced
	     * by the given one. This is achieved by the
	     * AbstractRecord list processing.
	     */
	 
	    if ( (replayer.getRecoveryStatus() != com.arjuna.ats.internal.jts.recovery.transactions.RecoveryStatus.ACTIVATE_FAILED) &&
		 (res != null) )
	    {
		if (jtsLogger.loggerI18N.isDebugEnabled())
		    {
			jtsLogger.loggerI18N.debug(DebugLevel.FUNCTIONS, 
						   VisibilityLevel.VIS_PUBLIC, 
						   FacilityCode.FAC_CRASH_RECOVERY, 
						   "com.arjuna.ats.internal.jts.recovery.recoverycoordinators.GenericRecoveryCoordinator_4", new Object[]{id._RCUid});
		    }

		replayer.swapResource(id._RCUid, res);
	    }

	    /*
	     * If we've activated then now replay phase 2. The
	     * replayer creates a new thread to do this.  
	     */

	    if (replayer.getRecoveryStatus() != com.arjuna.ats.internal.jts.recovery.transactions.RecoveryStatus.ACTIVATE_FAILED)
	    {
		replayer.replayPhase2();
	    }
	    else
	    {
		replayer.tidyup();
		
		/*
		 * The transaction didn't activate so we have a
		 * rollback situation but we can't rollback the
		 * resource that we have been given through the
		 * intentions list but we can issue rollback
		 * directly. This is configurable through the System
		 * properties.
		 */

		currentStatus = Status.StatusRolledBack;
	    }
	}

	/*
	 * Try to call rollback on the resource directly. This is not
	 * strictly necessary, since the resource could do this work itself
	 * when it gets the StatusRolledBack return value. However, some
	 * resources may not work this way. If this resource wasn't involved
	 * in the transaction in the first place then it doesn't matter if
	 * we invoke it here, since it has no affect on the transaction
	 * outcome.
	 */

	if (currentStatus == Status.StatusRolledBack)
	{
	    if (_issueRecoveryRollback)
	    {
		ResourceCompletor resourceCompletor = new ResourceCompletor(res, ResourceCompletor.ROLLBACK);
		resourceCompletor.start();
	    }
	}
	
	/* 
	 * If the transaction is Active then throw the NotPrepared
	 * exception.  
	 */

	if (currentStatus == Status.StatusActive)
	    throw new NotPrepared();

	return currentStatus;
    }

    /**
     * Construct a string, to be used somehow in the objectkey (probably)
     * of a RecoveryCoordinator reference. This will be deconstructed in 
     * the reconstruct() which is passed such a string, to remake the
     * necessary RecoveryCoordinator when a replay_completion is received for it.
     *
     * Put here to make it in the same class as the deconstruction
     */
    public static String makeId( Uid rcUid, Uid tranUid, 
				 Uid processUid, boolean isServerTransaction )
    {
	RecoveryCoordinatorId id = new RecoveryCoordinatorId(rcUid, tranUid, processUid, 
							     isServerTransaction);

	return id.makeId();
    }

    /**
     *  (re)construct a RecoveryCoordinator instance using the encoded information
     *  in the parameter. The encoded information was (we hope) created using
     *  {@link #makeId makeId} and has been passed around as part of the object key
     *  in the RecoveryCoordinator IOR.
     */
    public static GenericRecoveryCoordinator reconstruct(String encodedRCData)
    {
	if (jtsLogger.logger.isDebugEnabled())
	    {
		jtsLogger.logger.debug(DebugLevel.CONSTRUCTORS, 
				       VisibilityLevel.VIS_PUBLIC, 
				       FacilityCode.FAC_CRASH_RECOVERY, 
				       "GenericRecoveryCoordinator.reconstruct(" + encodedRCData + ")");
	    }
	
	RecoveryCoordinatorId id = RecoveryCoordinatorId.reconstruct(encodedRCData);

	if (id != null) {
	    return new GenericRecoveryCoordinator(id);
	} else {
	    // already traced
	    return null;
	}	
    }

private static Status get_status (Uid actionUid, Uid processUid) throws Inactive
    {
	Status status = Status.StatusUnknown;
	boolean transactionActive = true;

	try
	{
	    status = StatusChecker.get_current_status(actionUid, processUid);
	}
	catch (Inactive e)
	{
	    // original process is dead.
	    
	    transactionActive = false;
	}

	boolean hasBeenRecovering = false;
	
	for (;;)
	{
	    /*
	     * An active transaction may actually be in the process of
	     * recovering. In which case, we cannot add a new resource to the
	     * intentions list. Hold here if that is the case.
	     */

	    if (transactionActive)
	    {
		Object o = com.arjuna.ats.internal.jts.recovery.transactions.RecoveredTransactionReplayer.isPresent(actionUid);

		if (o != null)
		{
		    hasBeenRecovering = true;
		    
		    synchronized (o)
		    {
			try
			{
			    o.wait();
			}
			catch (Exception e)
			{
			}
		    }
		}
		else
		{
		    if (hasBeenRecovering)
			throw new Inactive();
		    else
			break;
		}
	    }
	    else
		throw new Inactive();
	}

	return status;
    }
 
    private GenericRecoveryCoordinator (RecoveryCoordinatorId id)
    {
	_id = id;
    }

    private RecoveryCoordinatorId _id;

    private static boolean  _issueRecoveryRollback;

    static
    {
	_issueRecoveryRollback = true;
	
	String issueRecoveryRollback = jtsPropertyManager.propertyManager.getProperty(com.arjuna.ats.jts.recovery.RecoveryEnvironment.OTS_ISSUE_RECOVERY_ROLLBACK);
	
	if (issueRecoveryRollback != null)
	{
	    if (issueRecoveryRollback.compareTo("NO") == 0)
		_issueRecoveryRollback = false;
	}
    }

}


