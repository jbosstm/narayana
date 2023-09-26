/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wscf.exceptions;

import com.arjuna.mw.wsas.exceptions.SystemException;

/**
 * There is no coordinator associated with the target.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: NoCoordinatorException.java,v 1.1 2002/12/17 11:36:38 nmcl Exp $
 * @since 1.0.
 */

public class NoCoordinatorException extends SystemException
{

    public NoCoordinatorException ()
    {
	super();
    }

    public NoCoordinatorException (String s)
    {
	super(s);
    }

    public NoCoordinatorException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}