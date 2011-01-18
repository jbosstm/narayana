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

import com.arjuna.ats.jta.utils.XAHelper;

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

    public final void nextScan (Xid[] trans)
    {
	/*
	 * Rather than recover now, wait until the next scan to
	 * give any transactions a chance to finish.
	 */

	_scanHoldNumber++;

	/*
	 * We keep a list of all Xids that are returned from all
	 * XAResource instances. Because we never know when exactly we 
	 * can get rid of them (because of the multi-pass approach to
	 * determining those that need to be recovered), we only keep the
	 * cache for a maximum number of scans. After that, we zero it and
	 * start from scratch again. This may mean that we take an extra pass
	 * to recover some transactions.
	 */

	if (_scanHoldNumber > MAX_SCAN_HOLD)
	    _scanM = null;

	if (_scanM == null)
	    _scanM = _scanN;
	else
	{
	    if ((_scanM != null) && (_scanN != null))
	    {
		int newArraySize = _scanM.length + _scanN.length;
		Xid[] copy = new Xid[newArraySize];
		
		System.arraycopy(_scanM, 0, copy, 0, _scanM.length);
		System.arraycopy(_scanN, 0, copy, _scanM.length, _scanN.length);

		_scanM = copy;
	    }
	}
	    
	_scanN = trans;
    }

    /*
     * Go through the current list of inflight transactions and look for
     * the same entries in the previous scan. If we find them, then we will
     * try to recover them. Then move the current list into the old list
     * for next time. We could prune out those transactions we manage to
     * recover, but this will happen automatically at the next scan since
     * they won't appear in the next new list of inflight transactions.
     */

    public final Xid[] toRecover ()
    {
        final int numScanN = (_scanN == null ? 0 : _scanN.length) ;
        final int numScanM = (_scanM == null ? 0 : _scanM.length) ;
        final int numScan = Math.min(numScanN, numScanM) ;

        if (numScan == 0)
        {
            return null ;
        }

        final List<Xid> workingList = new ArrayList<Xid>(numScanN) ;

        for(int i = 0; i < numScanN; i++)
        {
            // JBTM-823 / JBPAPP-5195 : don't assume list order/content match.
            // the list is (hopefully) small and we don't entirely trust 3rd party
            // Xid hashcode behaviour, so we just do this the brute force way...
            for(int j = 0; j < numScanM; j++)
            {
                if (XAHelper.sameXID(_scanN[i], _scanM[j]))
                {
                    workingList.add(_scanN[i]);
                    // any given id should be in _scanN only once, but _scanM may have dupls as
                    // it's actually a combination of prev runs, per the array copy in nextScan.
                    // we drop out here as we want each Xid only once in the return set:
                    break;
                }
            }
        }

        return workingList.toArray(new Xid[workingList.size()]);
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
	if (_scanN != null)
	{
	    for (int i = 0; i < _scanN.length; i++)
	    {
		if (XAHelper.sameXID(xid, _scanN[i]))
		    return true;
	    }
	}
	
	if (_scanM != null)
	{
	    for (int i = 0; i < _scanM.length; i++)
	    {
		if (XAHelper.sameXID(xid, _scanM[i]))
		    return true;
	    }
	}

	return false;
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
        if(xids == null || xids.length == 0) {
            return false;
        }

        for(int i = 0; i < xids.length; i++) {
            if(contains(xids[i])) {
                _xares = xaResource;
                return true;
            }
        }

        return false;
    }

    private Xid[]      _scanN;
    private Xid[]      _scanM;
    private XAResource _xares;
    private int        _scanHoldNumber;

    private static final int MAX_SCAN_HOLD = 10;  // number of scans we hold the cache for before flushing;

}
