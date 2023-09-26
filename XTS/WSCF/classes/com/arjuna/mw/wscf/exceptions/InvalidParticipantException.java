/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.exceptions;

import com.arjuna.mw.wsas.exceptions.WSASException;

/**
 * The specified participant is invalid in the context used.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: InvalidParticipantException.java,v 1.1 2002/12/19 10:44:02 nmcl Exp $
 * @since 1.0.
 */

public class InvalidParticipantException extends WSASException
{

    public InvalidParticipantException ()
    {
	super();
    }

    public InvalidParticipantException (String s)
    {
	super(s);
    }

    public InvalidParticipantException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}