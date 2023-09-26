/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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