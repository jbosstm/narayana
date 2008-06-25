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
 * $Id: HLSWrapper.java,v 1.3 2004/03/15 13:25:01 nmcl Exp $
 */

package com.arjuna.mwlabs.wsas.util;

import com.arjuna.mw.wsas.activity.HLS;

import com.arjuna.ats.internal.arjuna.template.OrderedListElement;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: HLSWrapper.java,v 1.3 2004/03/15 13:25:01 nmcl Exp $
 * @since 1.0.
 */

public class HLSWrapper implements OrderedListElement
{

    public HLSWrapper (HLS hls)
    {
	_theHLS = hls;
    }
    
    /**
     * Are the two entries equal?
     */

    public boolean equals (OrderedListElement e)
    {
	if (e instanceof HLSWrapper)
	{
	    try
	    {
		return (boolean) (((HLSWrapper) e).hls().priority() == hls().priority());
	    }
	    catch (SystemException ex)
	    {
		return false;
	    }
	}
	else
	    return false;
    }

    /**
     * Is the current entry less than the one in the parameter?
     */

    public boolean lessThan (OrderedListElement e)
    {
	if (e instanceof HLSWrapper)
	{
	    try
	    {
		return (boolean) (((HLSWrapper) e).hls().priority() < hls().priority());
	    }
	    catch (SystemException ex)
	    {
		return false;
	    }
	}
	else
	    return false;
    }	

    /**
     * Is the current entry greater than the one in the parameter?
     */
 
    public boolean greaterThan (OrderedListElement e)
    {
	if (e instanceof HLSWrapper)
	{
	    try
	    {
		return (boolean) (((HLSWrapper) e).hls().priority() > hls().priority());
	    }
	    catch (SystemException ex)
	    {
		return false;
	    }
	}
	else
	    return false;
    }		

    public final HLS hls ()
    {
	return _theHLS;
    }
    
    private HLS _theHLS;
    
}

