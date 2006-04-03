/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
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
 * Copyright (C) 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: XARecoveryResource.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jta.recovery;

import com.arjuna.ats.arjuna.common.Uid;

import java.util.*;
import javax.transaction.xa.*;

public interface XARecoveryResource
{

    /**
     * Results of performing recovery.
     */

    public static final int RECOVERED_OK = 1;
    public static final int FAILED_TO_RECOVER = 2;
    public static final int WAITING_FOR_RECOVERY = 3;
    public static final int TRANSACTION_NOT_PREPARED = 4;

    /**
     * Responses to whether or not the instance is recoverable.
     */

    public static final int INCOMPLETE_STATE = 10;
    public static final int INFLIGHT_TRANSACTION = 11;
    public static final int RECOVERY_REQUIRED = 12;
    
    /**
     * If we don't have an XAResource then we cannot recover at
     * this stage. The XAResource will have to be provided for
     * us and then we can retry.
     *
     * Because recovery happens periodically, it is possible that it takes
     * a snapshot of a transaction that is still running and will vanish
     * from the log anyway. If that happens, then we don't need to (and
     * can't) run recovery on it.
     */

    public int recoverable ();

    /**
     * Attempt the recovery. Return one of the status values above.
     */

    public int recover ();

    /**
     * @return the Xid that was used to manipulate this state.
     */

    public Xid getXid ();

    /**
     * @return the Uid for this instance.
     */

    public Uid get_uid ();

    /**
     * @return the type for this instance.
     */

    public String type ();

}
