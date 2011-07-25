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
 * Copyright (C) 2005,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryXids.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.recovery.arjunacore;

import com.arjuna.ats.jta.xa.XidImple;

import java.util.*;
import javax.transaction.xa.*;

public class RecoveryXids
{

    public RecoveryXids (XAResource xares)
    {
	_xares = xares;
    }
    
    public boolean equals (Object obj)
    {
	if (obj instanceof RecoveryXids)
	{
	    try
	    {
		return ((RecoveryXids) obj)._xares.isSameRM(_xares);
	    }
	    catch (Exception ex)
	    {
	    }
	}

	return false;
    }

    /**
     * Update our tracking with results of a new recovery scan pass
     * @param trans the Xids seen during the new scan.
     */
    public final void nextScan (Xid[] trans)
    {
        long currentTime = System.currentTimeMillis();

        // record the new information:
        if(trans != null) {
            for(Xid xid : trans) {
                XidImple xidImple = new XidImple(xid);
                if(!_whenFirstSeen.containsKey(xidImple)) {
                    _whenFirstSeen.put(xidImple, currentTime);
                }
                _whenLastSeen.put(xidImple, currentTime);
            }
        }

        // garbage collect the stale information:
        Set<XidImple> candidates = new HashSet<XidImple>(_whenFirstSeen.keySet());
        for(XidImple candidate : candidates) {
            if(_whenLastSeen.get(candidate) != currentTime) {
                // seen it previously but it's gone now so we can forget it:
                _whenFirstSeen.remove(candidate);
                _whenLastSeen.remove(candidate);
            }
        }
        // gc note: transient errors in distributed RMs may cause values to disappear in one scan and then reappear later.
        // under the current model we'll recover Xids only if they stick around for enough consecutive scans to
        // span the safely interval. In the unlikely event that causes problems, we'll need to postpone gc for a given
        // interval and take care to include only Xids seen in the most recent scan when returning candidates for recovery.
    }


    /**
     * Return any Xids that should be considered for recovery.
     * @return Xids that are old enough to be eligible for recovery.
     */
    public final Xid[] toRecover ()
    {
        List<Xid> oldEnoughXids = new LinkedList<Xid>();
        long currentTime = System.currentTimeMillis();

        for(Map.Entry<XidImple,Long> entry : _whenFirstSeen.entrySet()) {
            if(entry.getValue() + safetyIntervalMillis <= currentTime) {
                oldEnoughXids.add(entry.getKey());
            }
        }

        return oldEnoughXids.toArray(new Xid[oldEnoughXids.size()]);
    }

    public final boolean isSameRM (XAResource xares)
    {
	try
	{
	    if (xares == null)
		return false;
	    else
		return xares.isSameRM(_xares);
	}
	catch (Exception ex)
	{
	    return false;
	}
    }
    
    public boolean contains (Xid xid)
    {
        XidImple xidImple = new XidImple(xid);

        return _whenFirstSeen.containsKey(xidImple);
    }
	
    /**
     * If supplied xids contains any values seen on prev scans, replace the existing
     * XAResource with the supplied one and return true. Otherwise, return false.
     *
     * @param xaResource
     * @param xids
     * @return
     */
    public boolean updateIfEquivalentRM(XAResource xaResource, Xid[] xids)
    {
        if(xids != null && xids.length > 0) {
            for(int i = 0; i < xids.length; i++) {
                if(contains(xids[i])) {
                    _xares = xaResource;
                    return true;
                }
            }
        }

        // either (or both) passes have an empty Xid set,
        // so fallback to isSameRM as we can't use Xid matching
        if(isSameRM(xaResource)) {
            _xares = xaResource;
            return true;
        }

        return false;
    }

    // record when we first saw and most recently saw a given Xid. time in system clock (milliseconds).
    // since we don't trust 3rd party hashcode/equals we convert to our own wrapper class.
    private final Map<XidImple,Long> _whenFirstSeen = new HashMap<XidImple, Long>();
    private final Map<XidImple,Long> _whenLastSeen = new HashMap<XidImple, Long>();

    private XAResource _xares;

    private static final int safetyIntervalMillis = 10000; // may eventually want to make this configurable?
}