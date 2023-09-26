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
 * @version $Id: CoordinatorCancelledException.java,v 1.2 2004/03/15 13:25:04 nmcl Exp $
 * @since 1.0.
 */

public class CoordinatorCancelledException extends SystemException
{

    public CoordinatorCancelledException ()
    {
	super();
    }

    public CoordinatorCancelledException (String s)
    {
	super(s);
    }

    public CoordinatorCancelledException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}