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
 * @version $Id: DemoBusinessParticipant.java,v 1.5 2004/09/09 08:48:39 kconner Exp $
 * @since 1.0.
 */

public class DemoBusinessParticipant implements BusinessAgreementWithParticipantCompletionParticipant
{

    public static final int COMPENSATE = 0;
    public static final int CANCEL = 1;
    public static final int CLOSE = 2;
    public static final int EXIT = 3;

    /*
     * TODO does EXIT imply a memory leak? How does the participant
     * get unregistered from the dispatcher if it isn't called during
     * termination?
     */

    public DemoBusinessParticipant (int outcome, String id)
    {
	_outcome = outcome;
	_id = id;
    }

    public final boolean passed ()
    {
	/*
	 * If we get a passed result and our status was EXIT then this
	 * means that one of our methods was called, which is wrong if
	 * we exited!
	 */

	switch (_outcome)
	{
	case EXIT:
	    return !_passed;
	default:
	    return _passed;
	}
    }

    public void close () throws WrongStateException, SystemException
    {
	System.out.println(this.getClass().getName()+".close for "+this);

	if (_outcome == CLOSE)
	    _passed = true;
    }

    public void cancel () throws WrongStateException, SystemException
    {
	System.out.println(this.getClass().getName()+".cancel for "+this);

	if (_outcome == CANCEL)
	    _passed = true;
    }

    public void compensate () throws FaultedException, WrongStateException, SystemException
    {
	System.out.println(this.getClass().getName()+".compensate for "+this);

	if (_outcome == COMPENSATE)
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

    /**
     * @return the status value.
     */

    public String status () throws SystemException
    {
	return "Unknown";
    }
    
    protected boolean _passed = false;
    protected String  _id = null;
    protected int     _outcome;
    
}

