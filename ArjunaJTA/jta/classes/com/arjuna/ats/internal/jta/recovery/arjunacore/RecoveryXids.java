/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
import com.arjuna.ats.internal.jta.utils.XAUtils;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.xa.XATxConverter;
import com.arjuna.ats.jta.xa.XidImple;

public class RecoveryXids
{

    public RecoveryXids (NameScopedXAResource xares)
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
		return ((RecoveryXids) obj).isSameRM(_xares);
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

    public final boolean isSameRM (NameScopedXAResource xares)
    {
	try
	{
	    if (xares == null)
		return false;
	    else
		return XAUtils.isSameRM(xares.getXaResource(), _xares.getXaResource());
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
     * Heuristically determine is the supplied xaResource is from the same origin as ours and if so,
     * replace the existing resource with the supplied one. This deals with cases where a recovery
     * plugin supplies a new resource on each call.
     *
     * @param xaResource an xaResource which may or may not be from the same orgin as ours.
     * @param xids The list of xids returned by a recovery scan of the supplied resource.
     * @return true if the supplied xaResource is from the same origin as the one we previously held.
     */
    public boolean updateIfEquivalentRM(NameScopedXAResource xaResource, Xid[] xids)
    {
        // Equivalence is hard! get it wrong and XARecoveryModule._xidScans can leak memory...

        // If the resources have the same (non-null) jndiName, then they are equivalent
        //   (unless someone messed up and renamed a datasource between recovery passes...)
        // If the resource's own implementation (i.e. vendor driver) says they are equivalent, then they are
        //  (if the vendor got it right, which is not as likely as you'd hope).
        if(xaResource.isSameName(_xares) || isSameRM(xaResource)) {
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("RecoveryXids updateIfEquivalent updated with strong match. prev="+_xares+", replacement="+xaResource+", _lastValidated="+_lastValidated);
            }
            _xares = xaResource;
            _lastValidated = System.currentTimeMillis();
            return true;
        }

        // For cases where we have nothing else to rely on, we fall back to determining equivalence based
        // on if the resources know about the same xids.
        // Which is fine until non-uniq xids values from inflowed transactions appear in more than one RM...
        if(_xares.isAnonymous() && xaResource.isAnonymous()) {
            if(xids != null && xids.length > 0) {
                for(int i = 0; i < xids.length; i++) {
                    if(contains(xids[i]) && xids[i].getFormatId() == XATxConverter.FORMAT_ID) {
                        if (tsLogger.logger.isTraceEnabled()) {
                            tsLogger.logger.trace("RecoveryXids updateIfEquivalent updated with weak match. prev="+_xares+", replacement="+xaResource+", _lastValidated="+_lastValidated);
                        }
                        _xares = xaResource;
                        _lastValidated = System.currentTimeMillis();
                        return true;
                    }
                }
            }
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

    private NameScopedXAResource _xares;
    private long _lastValidated;

    /**
     * JBTM-1255 this is required to reinstate JBTM-924, see message in @see RecoveryXids#isStale() 
     */
    private static volatile int staleSafetyIntervalMillis; // The advice is that this (if made configurable is twice the safety interval)
    
    // JBTM-916 removed final so 10000 is not inlined into source code until we make this configurable
	// https://issues.jboss.org/browse/JBTM-842
    private static volatile int safetyIntervalMillis; // may eventually want to make this configurable?
    
    static {
        setSafetyIntervalMillis(jtaPropertyManager.getJTAEnvironmentBean().getOrphanSafetyInterval());
    }

    /**
     * Setting up the safety interval millis. The value defines what time has to elapse
     * for the participant record being considered as stale.
     * When value is set lower than 0 then default value of {@code 20_000} milliseconds is used.
     *
     * @param safetyIntervalMillis number of milliseconds after the record is declared to be stale
     */
    public static synchronized void setSafetyIntervalMillis(int safetyIntervalMillis) {
        RecoveryXids.safetyIntervalMillis = safetyIntervalMillis;
        if (safetyIntervalMillis > 0) {
            staleSafetyIntervalMillis = safetyIntervalMillis * 2;
        } else {
            staleSafetyIntervalMillis = 20_000;
        }
    }
}