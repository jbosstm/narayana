/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.mwlabs.wscf.coordinator;

import com.arjuna.mw.wsas.exceptions.NoActivityException;
import com.arjuna.mw.wsas.exceptions.SystemException;
import com.arjuna.wsc.InvalidProtocolException;

/**
 * Local coordinators can implement this interface to enable direct
 * creation of a coordinator and subordinate coordinator. Since we
 * don't know the actual implementation details, users are required
 * to determine the type dynamically.
 *
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id $
 * @since 2.0.
 */

public interface LocalFactory
{

	/**
	 * Create a new subordinate coordinator instance with the default subordinate type.
	 * 
	 * @return a new coordinator instance.
	 */
	public Object createSubordinate () throws NoActivityException, InvalidProtocolException, SystemException;
    /**
     * Create a new subordinate coordinator instance with the supplied subordinate type.
     *
     * @return a new coordinator instance.
     */
    public Object createSubordinate (String subordinateType) throws NoActivityException, InvalidProtocolException, SystemException;

}