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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.xa.XidImple;

public class RecoveryXids
{

    public RecoveryXids (XAResource xares)
    {
    	_xares = xares;
        _lastValidated = System.currentTimeMillis();
        if (tsLogger.logger.isTraceEnabled())
            tsLogger.logger.trace("RecoveryXids new recoveryXids " + xares + " " + _lastValidated);
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
                    if (tsLogger.logger.isTraceEnabled())
                        tsLogger.logger.trace("RecoveryXids _whenFirstSeen put nextScan " + _xares + " " + currentTime + " === " + xidImple);
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
                if (tsLogger.logger.isTraceEnabled())
                    tsLogger.logger.trace("RecoveryXids _whenFirstSeen remove nextScan" + _xares + " " + currentTime + " === " + candidate);
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
                if (tsLogger.logger.isTraceEnabled())
                    tsLogger.logger.trace("RecoveryXids _whenFirstSeen toRecover yes " + _xares + " " + entry.getValue() + " === " + currentTime);
            }
            if (tsLogger.logger.isTraceEnabled())
                tsLogger.logger.trace("RecoveryXids _whenFirstSeen toRecover no " + _xares + " " + entry.getValue() + " === " + currentTime);
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

    // JBTM-924
    public boolean isStale() {
        long now = System.currentTimeMillis();
        // JBTM-1255 - use a different safety declaration for staleness, if you set a safety interval of 0 (using reflection) then 
        // you will detect everything as stale. The only time we actually set safetyIntervalMillis is in JBTM-895 unit test SimpleIsolatedServers
        // so in the normal case this will behave as before
        long threshold = _lastValidated+(2*safetyIntervalMillis < staleSafetyIntervalMillis ? staleSafetyIntervalMillis : 2*safetyIntervalMillis);
        long diff = now - threshold;
        boolean result = diff > 0;
        if (tsLogger.logger.isTraceEnabled())
            tsLogger.logger.trace("RecoveryXids isStale Check " + _xares + " " + _lastValidated + " " + now + " " + result);
        return result;
    }
    
    public boolean remove (Xid xid)
    {
        XidImple xidImple = new XidImple(xid);
        
        if (_whenFirstSeen.containsKey(xidImple)) {
        	_whenFirstSeen.remove(xidImple);
        	if (tsLogger.logger.isTraceEnabled())
                tsLogger.logger.trace("RecoveryXids _whenFirstSeen remove remove " + _xares + " " + _lastValidated + " " + xidImple);
        	_whenLastSeen.remove(xidImple);
        	return true;
        } else {
        	return false;
        }
    }

	public boolean isEmpty() {
		return _whenFirstSeen.isEmpty();
	}

    /**
     * If supplied xids contains any values seen on prev scans, replace the existing
     * XAResource with the supplied one and return true. Otherwise, return false.
     *
     * @param xaResource
     * @param xids
     * @return true if equivalent
     */
    public boolean updateIfEquivalentRM(XAResource xaResource, Xid[] xids)
    {
        if(xids != null && xids.length > 0) {
            for(int i = 0; i < xids.length; i++) {
                if(contains(xids[i])) {
                    _xares = xaResource;
                    _lastValidated = System.currentTimeMillis();
                    if (tsLogger.logger.isTraceEnabled())
                        tsLogger.logger.trace("RecoveryXids updateIfEquivalentRM1 " + _xares + " " + _lastValidated);
                    return true;
                }
            }
        }

        // either (or both) passes have an empty Xid set,
        // so fallback to isSameRM as we can't use Xid matching
        if(isSameRM(xaResource)) {
            _xares = xaResource;
            _lastValidated = System.currentTimeMillis();
            if (tsLogger.logger.isTraceEnabled())
                tsLogger.logger.trace("RecoveryXids updateIfEquivalentRM2 " + _xares + " " + _lastValidated);
            return true;
        }

        return false;
    }
    
    public int size() {
        return _whenFirstSeen.size();
    }

    // record when we first saw and most recently saw a given Xid. time in system clock (milliseconds).
    // since we don't trust 3rd party hashcode/equals we convert to our own wrapper class.
    private final Map<XidImple,Long> _whenFirstSeen = new HashMap<XidImple, Long>();
    private final Map<XidImple,Long> _whenLastSeen = new HashMap<XidImple, Long>();

    private XAResource _xares;
    private long _lastValidated;

    /**
     * JBTM-1255 this is required to reinstate JBTM-924, see message in @see RecoveryXids#isStale() 
     */
    private static final int staleSafetyIntervalMillis; // The advice is that this (if made configurable is twice the safety interval)
    
    // JBTM-916 removed final so 10000 is not inlined into source code until we make this configurable
	// https://issues.jboss.org/browse/JBTM-842
    private static int safetyIntervalMillis; // may eventually want to make this configurable?
    
    static {
        safetyIntervalMillis = jtaPropertyManager.getJTAEnvironmentBean().getOrphanSafetyInterval();
        if (safetyIntervalMillis > 0) {
            staleSafetyIntervalMillis = safetyIntervalMillis * 2;
        } else {
            staleSafetyIntervalMillis = 20000;
        }
    }
}
