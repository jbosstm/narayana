/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: XARecover.java,v 1.2 2005/05/19 12:13:22 nmcl Exp $
 */

package com.arjuna.mw.wscf.model.as.coordinator.xa.messages;

import com.arjuna.mw.wscf.common.Qualifier;

import com.arjuna.mw.wscf.model.as.coordinator.Message;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * The transaction is recovering and a list of in doubt resources is
 * being requested.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: XARecover.java,v 1.2 2005/05/19 12:13:22 nmcl Exp $
 */

public class XARecover implements Message
{

    public XARecover (int flag)
    {
	this(flag, null);
    }
    
    public XARecover (int flag, Qualifier[] quals)
    {
	_flag = flag;
    }

    public boolean equals (Object o)
    {
	if (o == null)
	    return false;

	if (o instanceof XARecover)
	    return true;
	else
	    return false;
    }

    /**
     * @return a printable version of the vote.
     */

    public String toString ()
    {
	return "org.w3c.wscf.xa.messages.XARecover";
    }

    public String messageName () throws SystemException
    {
	return toString();
    }

    public Object coordinationSpecificData () throws SystemException
    {
	return new Integer(_flag);
    }

    public Qualifier[] qualifiers () throws SystemException
    {
	return null;
    }

    private int _flag;
    
}
