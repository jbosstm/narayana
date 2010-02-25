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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: TxInfo.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.xa;

import com.arjuna.ats.arjuna.common.*;

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
