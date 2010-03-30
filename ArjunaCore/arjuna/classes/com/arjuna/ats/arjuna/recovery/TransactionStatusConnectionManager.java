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
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: TransactionStatusConnectionManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.recovery ;

import java.util.* ;

import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.coordinator.ActionStatus ;
import com.arjuna.ats.arjuna.coordinator.TxControl ;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException ;
import com.arjuna.ats.arjuna.objectstore.ObjectStore ;
import com.arjuna.ats.arjuna.state.InputObjectState ;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector ;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem ;

import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_1 [com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_1] - Exception when accessing data store {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_2 [com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_2] - Object store exception {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_3 [com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_3] - found process uid {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_4 [com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_4] - added TransactionStatusConnector to table for process uid {0}
*/

public class TransactionStatusConnectionManager
{
    /**
     * Gets a reference to the Object Store.
     */
    public TransactionStatusConnectionManager()
    {
	if ( _objStore == null )
	{
	    _objStore = TxControl.getStore() ;
	}

	updateTSMI() ;
    }

    /**
     * Obtain the transaction status for the specified transaction.
     * At this point we don't know the type of the transaction, only it's
     * Uid. So, we're going to have to search through the object store.
     * This assumes that the transaction id is present in the local object
     * store. If it isn't, or there is a possibility it may not be, then
     * you should use the other variant of this method and determine the
     * type through another method.
     */

    public int getTransactionStatus( Uid tranUid )
    {
	String transactionType = "" ;

	int status = getTransactionStatus( transactionType, tranUid );

	return status ;
    }

    /**
     * Obtain the transaction status for the specified transaction type
     * and transaction.
     */
    public int getTransactionStatus( String transactionType, Uid tranUid )
    {
        int status = ActionStatus.INVALID ;

        // extract process id from uid
        String process_id = tranUid.getHexPid();

        // if the tx is in the same JVM we rely on ActionStatusService directly.
        // This skips the communication with TransactionStatusManager, which is just backed
        // by ActionStatusService anyhow. That allows TSM to be turned off for local only cases if desired.
        // Note: condition assumes ObjectStore is not shared between machines i.e. that processId is globally uniq.
        if(! process_id.equals( _localUid.getHexPid()) ) {
            status = getRemoteTransactionStatus(process_id, transactionType, tranUid);
        }

        /*
         * Try to read status from disc locally if invalid status,
         * as the tx may be local or, if it is remote, the
         * TransactionStatusManager may have died or comms may
         * have failed.
         * Use an ActionStatusService instance as that's what the remote
         * recovery manager would have used, and it contains all of the logic
         * to find and map the state type.
         */

        if ( status == ActionStatus.INVALID )
        {
            ActionStatusService ass = new ActionStatusService();

            try
            {
                status = ass.getTransactionStatus(transactionType, tranUid.stringForm());
            }
            catch ( Exception ex )
            {
                if (tsLogger.arjLoggerI18N.isWarnEnabled())
                {
                    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_1", ex);
                }
            }
        }

        return status ;
    }

    /**
     * Use the TransactionStatusConnector to remotly query a transaction manager to get the tx status.
     *
     * @param process_id the process identifier
     * @param transactionType the type of the transaction
     * @param tranUid the Uid of the transaction
     * @return the remote transaction status
     */
    private int getRemoteTransactionStatus(String process_id, String transactionType, Uid tranUid ) {

        int status = ActionStatus.INVALID ;

        // tx is not local, so use process id to index into
        // hash table to obtain transaction status connector
        // with which to retrieve the transaction status.

        // Note: assumes ObjectStore is not shared between machienes
        // otherwise we need to key on hostname,process_id tuple.

        if ( ! _tscTable.containsKey ( process_id ) )
        {
            updateTSMI();
        }

        if ( _tscTable.containsKey ( process_id ) )
        {
            TransactionStatusConnector tsc = (TransactionStatusConnector) _tscTable.get( process_id ) ;

            if ( tsc.isDead() )
            {
                _tscTable.remove( process_id ) ;
                tsc.delete() ;
                tsc = null ;
            }
            else
            {
                status = tsc.getTransactionStatus( transactionType, tranUid ) ;
            }
        }

        return status;
    }

    /**
     * Examine the Object Store for any new TrasactionStatusManagerItem
     * objects, and add to local hash table.
     */

    public void updateTSMI()
    {
	boolean tsmis = false ;

	InputObjectState uids = new InputObjectState() ;
	Vector tsmiVector = new Vector() ;

	try
	{
	    tsmis = _objStore.allObjUids( _typeName, uids ) ;
	}
	catch ( ObjectStoreException ex )
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_2", new Object[]{ex});
	    }
	}

	// cycle through each item, and update tsmTable with any
	// new TransactionStatusManagerItems

	if ( tsmis )
	{
	    Uid theUid = null;

	    boolean moreUids = true ;

	    while (moreUids)
	    {
		try
		{
		    theUid = UidHelper.unpackFrom(uids);

		    if ( theUid.equals( Uid.nullUid() ) )
		    {
			moreUids = false ;
		    }
		    else
		    {
			Uid newUid = new Uid (theUid) ;

			if (tsLogger.arjLoggerI18N.isDebugEnabled()) {
                tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_3", new Object[]{newUid});
            }
			tsmiVector.addElement(newUid) ;
		    }
		}
		catch (Exception ex )
		{
		    moreUids = false;
		}
	    }
	}

	// for each TransactionStatusManager found, if their is
	// not an entry in the local hash table for it then add it.
	Enumeration tsmiEnum = tsmiVector.elements() ;

	while ( tsmiEnum.hasMoreElements() )
	{
	    Uid currentUid = (Uid) tsmiEnum.nextElement() ;

	    String process_id = currentUid.getHexPid();

	    if ( ! _tscTable.containsKey( process_id ) )
	    {
		TransactionStatusConnector tsc = new TransactionStatusConnector ( process_id, currentUid ) ;

		if ( tsc.isDead() )
		{
		    tsc.delete() ;
		    tsc = null ;
		}
		else
		{
		    _tscTable.put ( process_id, tsc ) ;
		}

		if (tsLogger.arjLoggerI18N.isDebugEnabled()) {
            tsLogger.arjLoggerI18N.debug("com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager_4", new Object[]{process_id});
        }
	    }
	}
    }

    // Type within ObjectStore.
    private static String _typeName = TransactionStatusManagerItem.typeName() ;

    // Table of process ids and their transaction status managers items.
   private Hashtable _tscTable  = new Hashtable() ;

    // Reference to object store.
    private static ObjectStore _objStore = null ;

    private static Uid _localUid = new Uid();
}






