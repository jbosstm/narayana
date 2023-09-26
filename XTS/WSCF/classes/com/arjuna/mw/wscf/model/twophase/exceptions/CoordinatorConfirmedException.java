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
 * @version $Id: CoordinatorConfirmedException.java,v 1.1 2003/01/07 10:33:44 nmcl Exp $
 * @since 1.0.
 */

public class CoordinatorConfirmedException extends SystemException
{

    public CoordinatorConfirmedException ()
    {
	super();
    }

    public CoordinatorConfirmedException (String s)
    {
	super(s);
    }

    public CoordinatorConfirmedException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}