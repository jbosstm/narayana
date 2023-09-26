/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jta.xa;

import javax.transaction.xa.Xid;

public class TxInfo
{

    /*
     * IMPORTANT: Do not re-order.
     */

    public static final int ASSOCIATED = 0;
    public static final int NOT_ASSOCIATED = 1;
    public static final int ASSOCIATION_SUSPENDED = 2;
    public static final int FAILED = 3;
    public static final int OPTIMIZED_ROLLBACK = 4;
    public static final int UNKNOWN = 5;
    
    public TxInfo (Xid xid)
    {
        this(xid, TxInfo.ASSOCIATED);
    }

    public TxInfo (Xid xid, int state)
    {
	_xid = xid;
	_thread = Thread.currentThread();

	setState(state);
    }

    public final Thread thread ()
    {
	return _thread;
    }
    
    public final Xid xid ()
    {
	return _xid;
    }

    public final int getState ()
    {
	return _state;
    }

    public final void setState (int s)
    {
	if ((s >= TxInfo.ASSOCIATED) && (s <= TxInfo.UNKNOWN))
	    _state = s;
	else
	    _state = TxInfo.UNKNOWN;
    }

    private Xid    _xid;
    private int    _state;
    private Thread _thread;
    
}