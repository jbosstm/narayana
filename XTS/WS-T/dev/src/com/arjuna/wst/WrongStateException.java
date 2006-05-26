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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: WrongStateException.java,v 1.2 2003/04/11 14:47:29 nmcl Exp $
 */

package com.arjuna.wst;

/**
 * Thrown if the state of the transaction is incompatible with the
 * operation attempted. For example, asking the transaction to rollback
 * if it is already committing.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: WrongStateException.java,v 1.2 2003/04/11 14:47:29 nmcl Exp $
 * @since 1.0.
 */

public class WrongStateException extends Exception
{
    
    public WrongStateException ()
    {
	super();
    }

    public WrongStateException (String s)
    {
	super(s);
    }
 
}

