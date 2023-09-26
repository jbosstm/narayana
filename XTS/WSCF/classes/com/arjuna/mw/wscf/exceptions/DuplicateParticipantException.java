/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.exceptions;

import com.arjuna.mw.wsas.exceptions.WSASException;

/**
 * There was an attempt to register the same participant with the coordinator.
 * Some coordination protocols may allow this, whilst others will not.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DuplicateParticipantException.java,v 1.1 2002/12/19 10:44:02 nmcl Exp $
 * @since 1.0.
 */

public class DuplicateParticipantException extends WSASException
{

    public DuplicateParticipantException ()
    {
	super();
    }

    public DuplicateParticipantException (String s)
    {
	super(s);
    }

    public DuplicateParticipantException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}