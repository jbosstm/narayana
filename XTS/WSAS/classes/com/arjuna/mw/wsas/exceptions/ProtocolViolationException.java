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
 * $Id: ProtocolViolationException.java,v 1.1 2002/11/25 10:51:43 nmcl Exp $
 */

package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if the underlying protocol is violated in some way during
 * termination. For example, a two-phase commit protocol is necessarily
 * blocking to ensure consensus in the precence of failures. However,
 * this could mean that participants who have been prepared have to wait
 * forever if they don't get told the results of the transaction by the
 * (failed) coordinator. As such, heuristics were introduced to allow
 * a participant to make a unilateral decision about what to do. If this
 * decision goes against the coordinator's choice then the two-phase
 * protocol is violated.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ProtocolViolationException.java,v 1.1 2002/11/25 10:51:43 nmcl Exp $
 * @since 1.0.
 */

public class ProtocolViolationException extends WSASException
{

    public ProtocolViolationException ()
    {
	super();
    }

    public ProtocolViolationException (String s)
    {
	super(s);
    }

    public ProtocolViolationException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}


