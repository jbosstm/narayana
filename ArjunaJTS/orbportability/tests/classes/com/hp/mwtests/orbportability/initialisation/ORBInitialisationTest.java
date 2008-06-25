/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * Copyright (C) 2000, 2001, 2002
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ORBInitialisationTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.orbportability.initialisation;

import org.jboss.dtf.testframework.unittest.Test;

import com.arjuna.orbportability.orb.*;
import com.arjuna.orbportability.*;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

/**
 * @author Richard Begg
 */
public class ORBInitialisationTest extends Test implements TestAttributeCallback
{
    public final static int NONE = 0, PREINIT = 1, POSTINIT = 2, INVALID = 3;
    private final static String[] STATE_TEXT = { "NONE","PREINIT","POSTINIT","INVALID" };

    private int 	_currentState;

    public static String getStateText(int value)
    {
    	return(STATE_TEXT[value]);
    }

    public void preInitAttributeCalled()
    {
    	logInformation("Previous State: " + getStateText(_currentState));

    	switch (_currentState)
    	{
    	    case NONE:
    	    case POSTINIT:
    	    	_currentState = PREINIT;
    	    	break;
    	    default:
    	    case PREINIT :
    	    	_currentState = INVALID;
    	    	break;
    	}

    	logInformation(" Current State: " + getStateText(_currentState));
    }

    public void postInitAttributeCalled()
    {
    	logInformation("Previous State: " + getStateText(_currentState));

    	switch (_currentState)
    	{
    	    case NONE:
    	    case PREINIT:
    	    	_currentState = POSTINIT;
    	    	break;
    	    default:
    	    case POSTINIT :
    	    	_currentState = INVALID;
    	    	break;
    	}

    	logInformation(" Current State: " + getStateText(_currentState));
    }

    public void run(String[] args)
    {
        ORB orb = ORB.getInstance("main_orb");
        RootOA oa = RootOA.getRootOA(orb);

	try
	{
	    _currentState = NONE;

	    /*
	     * Registering attributes with ORB
	     */
	    orb.addAttribute( new PreTestAttribute( this ) );
	    orb.addAttribute( new PostTestAttribute( this ) );

            /*
	     * Initialise the ORB and OA
	     */
	    logInformation("Initialising ORB and OA");

	    orb.initORB(args, null);
	    oa.initOA();

	    if (_currentState == POSTINIT)
	    	assertSuccess();
	    else
	    	assertFailure();
	}
	catch (Exception e)
	{
	    logInformation("Initialisation failed: "+e);
	    e.printStackTrace();
	    assertFailure();
	}

	oa.destroy();
	orb.shutdown();
    }

    public class PreTestAttribute extends Attribute
    {
    	protected TestAttributeCallback _callback = null;

    	public PreTestAttribute( TestAttributeCallback callback )
    	{
    	    _callback = callback;
    	}

    	public void initialise (String[] params)
    	{
    	     _callback.preInitAttributeCalled();
    	}

	public boolean postORBInit ()
    	{
	    return(false);
	}
    }

    public class PostTestAttribute extends Attribute
    {
    	protected TestAttributeCallback _callback = null;

    	public PostTestAttribute( TestAttributeCallback callback )
    	{
    	    _callback = callback;
    	}

    	public void initialise (String[] params)
    	{
    	     _callback.postInitAttributeCalled();
    	}

	public boolean postORBInit ()
    	{
	    return(true);
	}
    }

    public static void main(String[] args)
    {
	ORBInitialisationTest test = new ORBInitialisationTest();

	test.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());

	test.runTest();
    }

}
