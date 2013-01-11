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

