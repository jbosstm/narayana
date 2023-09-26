/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wstx.tests.common;

import com.arjuna.ats.arjuna.common.Uid;

import com.arjuna.wst.*;

//import com.arjuna.mw.wst.vote.*;

//import com.arjuna.mw.wst.exceptions.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: DemoDurableParticipant.java,v 1.1.2.1 2004/08/09 12:34:30 nmcl Exp $
 * @since 1.0.
 */

public class DemoDurableParticipant implements Durable2PCParticipant
{

    public DemoDurableParticipant ()
    {
	_passed = false;
    _prepared = false;
    _resolved = false;
    }

    public final boolean resolved ()
    {
	return _resolved;
    }

    public final boolean prepared ()
    {
	return _prepared;
    }

    public final boolean passed ()
    {
	return _passed;
    }
    
    public Vote prepare () throws WrongStateException, SystemException
    {
	System.out.println("DemoDurableParticipant.prepare for "+this);
	
    _prepared = true;
	return new Prepared();
    }

    public void commit () throws WrongStateException, SystemException
    {
	System.out.println("DemoDurableParticipant.commit for "+this);

    _resolved = true;
	_passed = true;
    }

    public void rollback () throws WrongStateException, SystemException
    {
	System.out.println("DemoDurableParticipant.rollback for "+this);

    _resolved = true;
	_passed = false;
    }

    public void unknown () throws SystemException
    {
    }

    public void error () throws SystemException
    {
    }

    public String toString ()
    {
	return identifier();
    }
    
    public String identifier ()
    {
	return _id.stringForm();
    }
    
    private boolean _passed;
    private boolean _prepared;
    private boolean _resolved;
    private Uid     _id = new Uid();
    
}