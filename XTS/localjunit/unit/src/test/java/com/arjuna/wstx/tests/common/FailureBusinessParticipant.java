/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */



package com.arjuna.wstx.tests.common;

import com.arjuna.wst.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: FailureBusinessParticipant.java,v 1.4 2004/09/09 08:48:39 kconner Exp $
 * @since 1.0.
 */

public class FailureBusinessParticipant implements BusinessAgreementWithParticipantCompletionParticipant
{

    public static final int FAIL_IN_CLOSE = 0;
    public static final int FAIL_IN_CANCEL = 1;
    public static final int FAIL_IN_COMPENSATE = 2;

    public FailureBusinessParticipant (int failurePoint, String id)
    {
	_failurePoint = failurePoint;
	_id = id;
    }

    public final boolean passed ()
    {
	return _passed;
    }

    /**
     * we use this to time out the failure behaviour for close because otherwise the transaction will keep
     * retrying -- that's as per the sepc but a pain in the proverbial
     */
    private static long closeFirstCalledTime = 0;

    /**
     * timeout for the close failure in milliseconds. this must be bigger than the BA coordinator
     * wait timeout and any timeout used by a test when it waits for the transaction to complete.
     * A few minutes should be adequate.
     */
    private final long CLOSE_FAIL_TIMEOUT = (3 * 60 * 1000);

    public void close () throws WrongStateException, SystemException
    {
	System.out.println("FailureBusinessParticipant.close for "+this);
    if (closeFirstCalledTime == 0) {
        closeFirstCalledTime = System.currentTimeMillis();
    }
	if (_failurePoint == FAIL_IN_CLOSE) {
        long timeNow = System.currentTimeMillis();
        if ((timeNow - closeFirstCalledTime) < CLOSE_FAIL_TIMEOUT) {
        throw new WrongStateException();
        }
    }
	
	_passed = true;
    }

    public void cancel () throws WrongStateException, SystemException, FaultedException
    {
	System.out.println("FailureBusinessParticipant.cancel for "+this);

	if (_failurePoint == FAIL_IN_CANCEL)
	    throw new FaultedException();
	
	_passed = true;
    }

    public void compensate () throws WrongStateException, SystemException, FaultedException
    {
	System.out.println("FailureBusinessParticipant.compensate for "+this);

	if (_failurePoint == FAIL_IN_COMPENSATE)
	    throw new FaultedException();
	
	_passed = true;
    }

    public void forget () throws WrongStateException, SystemException
    {
    }

    public void unknown () throws SystemException
    {
    }

    public void error () throws SystemException
    {
    }

    public String status () throws SystemException
    {
	return "Unknown";
    }

    public String toString ()
    {
	try
	{
	    return identifier();
	}
	catch (SystemException ex)
	{
	    return "Unknown";

	}
    }
    
    public String identifier () throws SystemException
    {
	return _id;
    }
    
    private boolean _passed = false;
    private String  _id = null;
    private int     _failurePoint;
    
}

