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
 * $Id: ProtocolAlreadyRegisteredException.java,v 1.1 2002/11/25 10:56:34 nmcl Exp $
 */

package com.arjuna.mw.wscf.exceptions;

import com.arjuna.mw.wsas.exceptions.WSASException;

/**
 * An attempt has been made to register the same coordination protocol
 * with the protocol factory.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ProtocolAlreadyRegisteredException.java,v 1.1 2002/11/25 10:56:34 nmcl Exp $
 * @since 1.0.
 */

public class ProtocolAlreadyRegisteredException extends WSASException
{

    public ProtocolAlreadyRegisteredException ()
    {
	super();
    }

    public ProtocolAlreadyRegisteredException (String s)
    {
	super(s);
    }

    public ProtocolAlreadyRegisteredException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}


