/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.exceptions;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * A general error has occurred.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: UnexpectedException.java,v 1.1 2002/11/25 10:56:34 nmcl Exp $
 * @since 1.0.
 */

public class UnexpectedException extends SystemException
{

    public UnexpectedException ()
    {
	super();
    }

    public UnexpectedException (String s)
    {
	super(s);
    }

    public UnexpectedException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}