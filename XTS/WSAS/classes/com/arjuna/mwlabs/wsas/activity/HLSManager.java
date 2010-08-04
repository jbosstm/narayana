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
        String key = service.identity();
        synchronized(_hlsMap) {
            _hlsMap.put(key, service);
        }
    }
    }

    public static final void removeHLS (HLS service) throws InvalidHLSException, SystemException
    {
	if (service == null)
	    throw new InvalidHLSException();
	else
	{
        String key = service.identity();
        HLS oldValue;
	    synchronized (_hlsMap)
	    {
            oldValue = _hlsMap.remove(key);
        }
        if (oldValue == null) {
            throw new InvalidHLSException(wsasLogger.i18NLogger.get_activity_HLSManager_1());
        }
    }
    }
    /*
     * redundant?
     */
    public static final HLS[] allHighLevelServices () throws SystemException
    {
	synchronized (_hlsMap)
	{
	    HLS[] toReturn = new HLS[(int) _hlsMap.size()];
        Collection<HLS> services = _hlsMap.values();
        return services.toArray(toReturn);
	}
    }

    public static final HLS getHighLevelService (String serviceType) throws SystemException
    {
        synchronized (_hlsMap)
        {
            return _hlsMap.get(serviceType);
        }
    }

    private static HashMap<String, HLS> _hlsMap = new HashMap<String, HLS>();

}
