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
 * $Id: ActiveChildException.java,v 1.1 2002/11/25 10:51:42 nmcl Exp $
 */

package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if an attempt is made to complete an activity that has active
 * child activities and at least one of the registered HLSs determines
 * the it is an invalid condition.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ActiveChildException.java,v 1.1 2002/11/25 10:51:42 nmcl Exp $
 * @since 1.0.
 */

public class ActiveChildException extends WSASException
{

    public ActiveChildException ()
    {
	super();
    }

    public ActiveChildException (String s)
    {
	super(s);
    }

    public ActiveChildException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}


