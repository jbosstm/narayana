/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.model.sagas.exceptions;

import com.arjuna.mw.wsas.exceptions.SystemException;


/**
 * There is no coordinator associated with the target.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: InvalidSynchronizationException.java,v 1.1 2003/01/07 10:33:45 nmcl Exp $
 * @since 1.0.
 */

public class InvalidSynchronizationException extends SystemException
{

    public InvalidSynchronizationException()
    {
	super();
    }

    public InvalidSynchronizationException(String s)
    {
	super(s);
    }

    public InvalidSynchronizationException(String s, int errorcode)
    {
	super(s, errorcode);
    }

}