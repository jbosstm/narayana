/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mw.wsas.exceptions;

/**
 * This is the base class from which all WSAS related exceptions
 * inherit. It provides several kinds of additional information:
 *
 * (i) a string describing the error.
 * (ii) an integer code for the error.
 * 
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: WSASException.java,v 1.1 2002/11/25 10:51:44 nmcl Exp $
 * @since 1.0.
 */

public class WSASException extends Exception
{

    /**
     * Constructs a WSASException object; reason defaults to null and
     * errorcode defaults to 0.
     */

    public WSASException ()
    {
	super();

	_errorCode = 0;
	_data = null;
    }

    /**
     * Constructs a WSASException object with the specified reason.
     * errorcode defaults to 0.
     * 
     * @param reason the reason
     */

    public WSASException (String reason)
    {
	super(reason);

	_errorCode = 0;
	_data = null;
    }

    /**
     * Constructs a WSASException object with the specified reason and
     * errorcode.
     * 
     * @param reason the reason
     * @param errorcode the error code
     */

    public WSASException (String reason, int errorcode)
    {
	super(reason);
	
	_errorCode = errorcode;
	_data = null;
    }

    /**
     * Constructs a WSASException object with the specified reason and
     * object.
     */

    public WSASException (String reason, Object obj)
    {
	super(reason);
	
	_errorCode = 0;
	_data = obj;
    }

    /**
     * Constructs a WSASException object with the specified object.
     */

    public WSASException (Object obj)
    {
	super();
	
	_errorCode = 0;
	_data = obj;
    }

    /**
     * @return the errorcode associated with this exception.
     */

    public final int getErrorCode ()
    {
	return _errorCode;
    }

    /**
     * @return the data object associated with this exception.
     */
    
    public final Object getData ()
    {
	return _data;
    }
    
    private int    _errorCode;
    private Object _data;
    
}