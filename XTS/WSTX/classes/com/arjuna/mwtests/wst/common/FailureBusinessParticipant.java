/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: FailureBusinessParticipant.java,v 1.4 2004/09/09 08:48:39 kconner Exp $
 */

package com.arjuna.mwtests.wst.common;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.wst.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: FailureBusinessParticipant.java,v 1.4 2004/09/09 08:48:39 kconner Exp $
 * @since 1.0.
 */

public class FailureBusinessParticipant implements com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant
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

    public void close () throws WrongStateException, SystemException
    {
	System.out.println("FailureBusinessParticipant.close for "+this);

	if (_failurePoint == FAIL_IN_CLOSE)
	    throw new WrongStateException();
	
	_passed = true;
    }

    public void cancel () throws WrongStateException, SystemException
    {
	System.out.println("FailureBusinessParticipant.cancel for "+this);

	if (_failurePoint == FAIL_IN_CANCEL)
	    throw new WrongStateException();
	
	_passed = true;
    }

    public void compensate () throws WrongStateException, SystemException
    {
	System.out.println("FailureBusinessParticipant.compensate for "+this);

	if (_failurePoint == FAIL_IN_COMPENSATE)
	    throw new WrongStateException();
	
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

