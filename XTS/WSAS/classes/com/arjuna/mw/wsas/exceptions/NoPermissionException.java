/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * The invoking thread does not have permission to attempt to use the
 * operation. For example, some activity implementations only allow
 * the creating thread to terminate an activity.
 *
 * Do we want to remove this and replace it with SecurityException, as
 * the JTA has done?
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: NoPermissionException.java,v 1.1 2002/11/25 10:51:43 nmcl Exp $
 * @since 1.0.
 */

public class NoPermissionException extends WSASException
{

    public NoPermissionException ()
    {
	super();
    }

    public NoPermissionException (String s)
    {
	super(s);
    }

    public NoPermissionException (String s, int errorcode)
    {
	super(s, errorcode);
    }
    
}