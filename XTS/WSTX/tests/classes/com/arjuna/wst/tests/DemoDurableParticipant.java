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
 * $Id: DemoDurableParticipant.java,v 1.1.2.1 2004/08/09 12:34:30 nmcl Exp $
 */

package com.arjuna.wst.tests;

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

