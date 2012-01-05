/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: WSASException.java,v 1.1 2002/11/25 10:51:44 nmcl Exp $
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


