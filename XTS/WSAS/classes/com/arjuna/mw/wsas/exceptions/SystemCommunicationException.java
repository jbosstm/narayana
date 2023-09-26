/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wsas.exceptions;

/**
 * Marker interface for exceptions e.g. timeouts, in the client/server communication.
 */
public class SystemCommunicationException extends SystemException {

	public SystemCommunicationException ()
	{
		super();
	}

	public SystemCommunicationException (String s)
	{
		super(s);
	}

	public SystemCommunicationException (String s, int errorcode)
	{
		super(s, errorcode);
	}

	public SystemCommunicationException (String reason, Object obj)
	{
		super(reason, obj);
	}

	public SystemCommunicationException (Object ex)
	{
		super(ex);
	}

}