/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if the state of the activity is such that it disallows the
 * attempted operation. For example, the activity is committing and
 * a participant that has prepared attempts to resign.
 *
 * Do we want to remove this and replace it with IllegalStateException as
 * is done in the JTA?
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: WrongStateException.java,v 1.2 2003/04/04 14:59:51 nmcl Exp $
 * @since 1.0.
 */

public class WrongStateException extends WSASException
{

    public WrongStateException ()
    {
	super();
    }

    public WrongStateException (String s)
    {
	super(s);
    }

    public WrongStateException (String s, int errorcode)
    {
	super(s, errorcode);
    }

}