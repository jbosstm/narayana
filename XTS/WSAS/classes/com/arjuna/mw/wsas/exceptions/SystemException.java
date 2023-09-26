/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * Thrown if an error occurs which is not met by another specific exception.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SystemException.java,v 1.2 2005/05/19 12:13:16 nmcl Exp $
 * @since 1.0.
 */

public class SystemException extends WSASException
{

	public SystemException ()
	{
		super();
	}

	public SystemException (String s)
	{
		super(s);
	}

	public SystemException (String s, int errorcode)
	{
		super(s, errorcode);
	}

	public SystemException (String reason, Object obj)
	{
		super(reason, obj);
	}

	public SystemException (Object ex)
	{
		super(ex);
	}

}