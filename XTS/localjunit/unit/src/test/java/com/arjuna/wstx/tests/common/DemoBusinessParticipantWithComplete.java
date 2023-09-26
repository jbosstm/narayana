/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */




package com.arjuna.wstx.tests.common;

import com.arjuna.wst.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DemoBusinessParticipantWithComplete.java,v 1.5 2004/09/09 08:48:40 kconner Exp $
 * @since 1.0.
 *
 * Requires complete to be sent before the transaction terminates.
 */

public class DemoBusinessParticipantWithComplete extends DemoBusinessParticipant implements BusinessAgreementWithCoordinatorCompletionParticipant
{

    public static final int COMPLETE = 4;

    public DemoBusinessParticipantWithComplete (int outcome, String id)
    {
	super(outcome, id);
    }

    public void close () throws WrongStateException, SystemException
    {
	System.out.println(this.getClass().getName()+".close for "+this);

	if (!_completed)
	    throw new SystemException();

	if (_outcome == CLOSE)
	    _passed = true;
    }

    public void cancel () throws WrongStateException, SystemException
    {
	System.out.println(this.getClass().getName()+".cancel for "+this);

	if (!_completed)
	    throw new SystemException();

	if (_outcome == CANCEL)
	    _passed = true;
    }

    public void compensate () throws FaultedException, WrongStateException, SystemException
    {
	System.out.println(this.getClass().getName()+".compensate for "+this);

	if (!_completed)
	    throw new SystemException();

	if (_outcome == COMPENSATE)
	    _passed = true;
    }

    public void complete () throws WrongStateException, SystemException
    {
	System.out.println(this.getClass().getName()+".complete for "+this);

	if (_outcome == COMPLETE)
	    _passed = true;

	_completed = true;
    }

    private boolean _completed = false;

}