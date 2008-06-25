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
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: OTIDMap.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jts.interposition.resources.osi;

import com.arjuna.ats.arjuna.common.Uid;

import org.omg.CosTransactions.*;

import java.util.Hashtable;
import java.util.Enumeration;

/*
 * Class which maintains a mapping of otid to Uid.
 * It automatically updates itself when it sees a
 * new Uid, and is pruned when transactions terminate.
 */

class OTIDWrapper
{
    
    public OTIDWrapper (otid_t otid)
    {
	_otid = otid;
	_uid = new Uid();
    }

    public Uid get_uid ()
    {
	return _uid;
    }

    public otid_t get_otid ()
    {
	return _otid;
    }

    private otid_t _otid;
    private Uid _uid;
 
};
    
public class OTIDMap
{

    public static synchronized Uid find (otid_t otid)
    {
	OTIDWrapper element = null;
	
	if (_otids.size() > 0)
	{
	    Enumeration e = _otids.elements();

	    while (e.hasMoreElements())
	    {
		element = (OTIDWrapper) e.nextElement();

		if (OTIDMap.same(element.get_otid(), otid))
		    return element.get_uid();
	    }
	}
	
	/*
	 * Got here, so must be new otid.
	 */

	element = new OTIDWrapper(otid);
    
	_otids.put(element.get_uid(), element);

	return element.get_uid();
    }

    public static synchronized boolean remove (Uid uid)
    {
	OTIDWrapper wrapper = (OTIDWrapper) _otids.remove(uid);

	if (wrapper != null)
	{
	    wrapper = null;

	    return true;
	}
	else
	    return false;
    }

    /*
     * Only called from synchronized methods.
     */
    
    private static boolean same (otid_t otid1, otid_t otid2)
    {
	if ((otid1.formatID == otid2.formatID) &&
	    (otid1.bqual_length == otid2.bqual_length))
	{
	    for (int i = 0; i < otid1.bqual_length; i++)
	    {
		if (otid1.tid[i] != otid2.tid[i])
		    return false;
	    }

	    /*
	     * Got here, so must be equal!
	     */
	    
	    return true;
	}
	else
	    return false;
    }

    private static Hashtable _otids = new Hashtable();
 
}

