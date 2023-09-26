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
 * @version $Id: CompensateFailedException.java,v 1.1 2004/04/21 15:57:43 nmcl Exp $
 * @since 1.0.
 */

public class CompensateFailedException extends SystemException
{

    public CompensateFailedException ()
    {
	super();
    }

    public CompensateFailedException (String s)
    {
	super(s);
    }

    public CompensateFailedException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}