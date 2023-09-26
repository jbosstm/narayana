/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if an attempt is made to complete an activity that has active
 * child activities and at least one of the registered HLSs determines
 * the it is an invalid condition.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: ActiveChildException.java,v 1.1 2002/11/25 10:51:42 nmcl Exp $
 * @since 1.0.
 */

public class ActiveChildException extends WSASException
{

    public ActiveChildException ()
    {
	super();
    }

    public ActiveChildException (String s)
    {
	super(s);
    }

    public ActiveChildException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}