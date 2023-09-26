/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.resources;

import org.omg.CosTransactions.Synchronization;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.jts.logging.jtsLogger;

/**
 * CosTransactions::Synchronizations are maintained in a separate list
 * from the standard transaction intentions list.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: SynchronizationRecord.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class SynchronizationRecord implements Comparable
{

    public SynchronizationRecord (Synchronization ptr)
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("SynchronizationRecord::SynchronizationRecord ( " + ptr + " )");
    }

	_ptr = ptr;
	_uid = new Uid();
    _isJTAInterposed = false;
    }

    public SynchronizationRecord (Synchronization ptr, boolean isJTAInterposed) {
        this(ptr);
        _isJTAInterposed = isJTAInterposed;
	}

    public final Synchronization contents ()
    {
	if (jtsLogger.logger.isTraceEnabled()) {
        jtsLogger.logger.trace("SynchronizationRecord::contents - for " + _ptr);
    }

	return _ptr;
    }

    public final Uid get_uid ()
    {
	return _uid;
    }

    /*
     * SyncronizationsImples are ordered first according to the isJTAInterposed property and then by Uid.
     * Interposed synchronizations must come after non-interposed ones  (see
     * TransactionSynchronizationRegistry)  The ordering within the interposed/non-interposed categories
     * is arbitrary but must be defined for correct functioning of the sort. Just don't rely on it in
     * application level code.
     *
     * Note that interposition here is in the JTA TSR interface sense, not CORBA TS.
     *
     * @param object
     * @return
     */
    public int compareTo(Object object)
    {
        SynchronizationRecord other = (SynchronizationRecord)object;

        if(this._isJTAInterposed && (!other._isJTAInterposed))
        {
            return 1;
        }
        else if((!this._isJTAInterposed) && other._isJTAInterposed)
        {
            return -1;
        }
        else if(this._uid.equals(other._uid))
        {
            return 0;
        }
        else
        {
            return this._uid.lessThan(other._uid) ? -1 : 1;
        }
    }

    private Uid             _uid;
    private Synchronization _ptr;
    private boolean _isJTAInterposed;
}