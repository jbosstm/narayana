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
 * $Id: Vote.java,v 1.1 2002/11/25 11:00:51 nmcl Exp $
 */

package com.arjuna.mw.wstx.common;

import com.arjuna.mw.wscf.common.Qualifier;

/**
 * All votes from prepare are instances of this interface.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Vote.java,v 1.1 2002/11/25 11:00:51 nmcl Exp $
 * @since 1.0.
 */

public abstract class Vote
{

    /**
     * @return any qualifiers that may be associated with the vote. May be
     * null.
     */

    public Qualifier[] getQualifiers ()
    {
	return _qualifiers;
    }

    public abstract String toString ();

    /**
     * Currently only check actual vote type and not the qualifiers.
     */

    public abstract boolean equals (Object o);
    
    protected Vote ()
    {
	this(null);
    }
    
    protected Vote (Qualifier[] quals)
    {
	_qualifiers = quals;
    }

    /**
     * @return a printable version of the vote.
     */

    protected Qualifier[] _qualifiers;

}
