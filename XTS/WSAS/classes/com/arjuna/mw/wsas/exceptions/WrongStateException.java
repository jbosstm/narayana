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
 * $Id: WrongStateException.java,v 1.2 2003/04/04 14:59:51 nmcl Exp $
 */

package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if the state of the activity is such that it disallows the
 * attempted operation. For example, the activity is committing and
 * a participant that has prepared attempts to resign.
 *
 * Do we want to remove this and replace it with IllegalStateException as
 * is done in the JTA?
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: WrongStateException.java,v 1.2 2003/04/04 14:59:51 nmcl Exp $
 * @since 1.0.
 */

public class WrongStateException extends WSASException
{

    public WrongStateException ()
    {
	super();
    }

    public WrongStateException (String s)
    {
	super(s);
    }

    public WrongStateException (String s, int errorcode)
    {
	super(s, errorcode);
    }

}


