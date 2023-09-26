/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.twophase.exceptions;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * There is no coordinator associated with the target.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ParticipantCancelledException.java,v 1.1 2005/06/09 09:41:43 nmcl Exp $
 * @since 1.0.
 */

public class ParticipantCancelledException extends SystemException
{

    public ParticipantCancelledException ()
    {
	super();
    }

    public ParticipantCancelledException (String s)
    {
	super(s);
    }

    public ParticipantCancelledException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}