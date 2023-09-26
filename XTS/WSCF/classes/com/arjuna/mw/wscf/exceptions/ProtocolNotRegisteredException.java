/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.exceptions;

import com.arjuna.mw.wsas.exceptions.WSASException;

/**
 * An attempt was made to act on a protocol that the protocol factory had
 * no knowledge of.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ProtocolNotRegisteredException.java,v 1.1 2002/11/25 10:56:34 nmcl Exp $
 * @since 1.0.
 */

public class ProtocolNotRegisteredException extends WSASException
{

    public ProtocolNotRegisteredException ()
    {
	super();
    }

    public ProtocolNotRegisteredException (String s)
    {
	super(s);
    }

    public ProtocolNotRegisteredException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}