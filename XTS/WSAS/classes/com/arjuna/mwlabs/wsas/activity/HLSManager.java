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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: HLSManager.java,v 1.5 2005/05/19 12:13:18 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.activity;

import com.arjuna.mw.wsas.logging.wsasLogger;

import com.arjuna.mw.wsas.activity.HLS;
import com.arjuna.mw.wsas.activity.OutcomeManager;

import com.arjuna.mwlabs.wsas.util.HLSWrapper;

import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.mw.wsas.exceptions.InvalidHLSException;

import java.util.*;

/**
 * The HLS manager is the way in which an HLS can register
 * itself with the activity service. This allows it to be informed
 * of the lifecycle of activities and to augment that lifecyle and
 * associated context.
 *
 * An HLS can be associated with all threads (globally) or with only
 * a specific thread (locally).
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: HLSManager.java,v 1.5 2005/05/19 12:13:18 nmcl Exp $
 * @since 1.0.
 */

public class HLSManager
{

    public static final void addHLS (HLS service) throws InvalidHLSException, SystemException
    {
	if (service == null) {
	    throw new InvalidHLSException();
    } else {
        synchronized(_hls) {
	    _hls.add(new HLSWrapper(service));
        }
    }
    }

    public static final void removeHLS (HLS service) throws InvalidHLSException, SystemException
    {
	if (service == null)
	    throw new InvalidHLSException();
	else
	{
	    synchronized (_hls)
	    {
            HLSWrapper elem = null;
            boolean found = false;
            Iterator<HLSWrapper> iter = _hls.iterator();
            while (!found && iter.hasNext()) {
                elem = iter.next();
                if (elem.hls() == service) {
                    found = true;
                }
            }
            if (!found) {
                throw new InvalidHLSException(wsasLogger.i18NLogger.get_activity_HLSManager_1());
            } else {
                _hls.remove(elem);
            }
        }
    }
    }

    public static final HLS[] allHighLevelServices () throws SystemException
    {
	synchronized (_hls)
	{
	    HLS[] toReturn = new HLS[(int) _hls.size()];
        Iterator<HLSWrapper> iter = _hls.iterator();
	    HLSWrapper elem;
	    int i = 0;
	    
	    while (iter.hasNext())
	    {
            elem = iter.next();
            toReturn[i++] = ((HLSWrapper) elem).hls();
	    }
	    
	    return toReturn;
	}
    }

    public static final void setOutcomeManager (OutcomeManager om) throws SystemException
    {
	synchronized (_outcomeManager)
	{
	    if (om == null)
		om = new OutcomeManagerImple();
	    
	    _outcomeManager = om;
	}
    }

    public static final OutcomeManager getOutcomeManager () throws SystemException
    {
	synchronized (_outcomeManager)
	{
	    return _outcomeManager;
	}
    }

    static final Iterator<HLSWrapper> HLServices ()
    {
	return _hls.iterator();
    }
    
    private static SortedSet<HLSWrapper> _hls = new TreeSet(); // order decreasing as higher is first
    private static OutcomeManager _outcomeManager = new OutcomeManagerImple();
    
}
