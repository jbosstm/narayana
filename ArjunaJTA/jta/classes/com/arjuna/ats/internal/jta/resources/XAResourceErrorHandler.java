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
 * Copyright (C) 2004,
 * 
 * Arjuna Technologies Ltd, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: XAResourceErrorHandler.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.resources;

import java.util.HashMap;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.ats.jta.resources.XAResourceMap;

public class XAResourceErrorHandler
{

	public static boolean notAProblem (XAResource res, XAException ex, boolean commit)
	{
		boolean isNotAProblem = false;
		XAResourceMap theMap = (XAResourceMap) _maps.get(res.getClass().getName());

		if (theMap != null)
			isNotAProblem = theMap.notAProblem(ex, commit);

		return isNotAProblem;
	}

	public static void addXAResourceMap (String type, XAResourceMap map)
	{
		_maps.put(type, map);
	}

	private static HashMap _maps = new HashMap();

    /**
     * Static block puts all XAResourceMap instances defined in JTAEnvironmentBean to the XAResourceErrorHandler's hash map.
     * They are later used to check if the XAException is a non-error when received in reply to commit or rollback.
     */
    static
    {
        for(XAResourceMap xaResourceMap : jtaPropertyManager.getJTAEnvironmentBean().getXaResourceMaps())
        {
            XAResourceErrorHandler.addXAResourceMap(xaResourceMap.getXAResourceName(), xaResourceMap);
        }
    }
}
