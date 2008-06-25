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
 * Copyright (C) 2000, 2001, 2002
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: OAPrePostShutdownTest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.orbportability.shutdown;

import org.jboss.dtf.testframework.unittest.Test;

import com.arjuna.orbportability.*;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;

/**
 * @author Richard Begg
 */
public class OAPrePostShutdownTest extends Test implements PrePostTestCallback
{
    public static final int NONE = 0, PRESHUTDOWN = 1, POSTSHUTDOWN = 2, INVALID = 3;
    private static final String[] STATE_STRING = {"NONE", "PRESHUTDOWN", "POSTSHUTDOWN", "INVALID" };

    public int	_currentState;

    /**
     * Generates a String version of the 'enumerated value'.
     *
     * @param value The 'enumerated value' to generate the string of.
     * @return The String version of the enumerated value.
     */
    public static String PrettyPrintState(int value)
    {
    	if ( (value >= NONE) && (value <= INVALID) )
    	    return(STATE_STRING[value]);

    	return("##ERROR##");
    }

    /**
     * Called by the pre-shutdown subclass to inform test that it has been prodded
     *
     * @param name The name associated with this pre-shutdown callback
     */
    public void preShutdownCalled(String name)
    {
    	logInformation( "Previous State : "+ PrettyPrintState( _currentState ) );

    	switch ( _currentState )
        {
    	    case NONE :
    	    case POSTSHUTDOWN :
    	    	_currentState = PRESHUTDOWN;
    	    	break;

    	    case PRESHUTDOWN :
    	    default :
    	    	_currentState = INVALID;
    	    	break;
    	}

    	logInformation( " Current State : "+ PrettyPrintState( _currentState ) );
    }

    /**
     * Called by the post-shutdown subclass to inform test that it has been prodded
     *
     * @param name The name associated with this post-shutdown callback
     */
    public void postShutdownCalled(String name)
    {
    	logInformation( "Previous State : "+ PrettyPrintState( _currentState ) );

    	switch ( _currentState )
        {
    	    case PRESHUTDOWN :
    	    	_currentState = POSTSHUTDOWN;
    	    	break;

    	    case NONE :
    	    case POSTSHUTDOWN :
    	    default :
    	    	_currentState = INVALID;
    	    	break;
    	}

    	logInformation( " Current State : "+ PrettyPrintState( _currentState ) );
    }

    public void run(String[] args)
    {
        ORB orb = ORB.getInstance("main_orb");
        RootOA oa = RootOA.getRootOA(orb);
	try
	{
/*
	     * Initialise the ORB and OA
	     */
	    logInformation("Initialising ORB and OA");

	    orb.initORB(args, null);
	    oa.initOA();

	    _currentState = NONE;
	}
	catch (Exception e)
	{
	    logInformation("Initialisation failed: "+e);
	    e.printStackTrace();
	    assertFailure();
	}

	/**
	 * Register pre and post shutdown handlers
	 */
	oa.addPreShutdown( new TestPreShutdown( "PreShutdown", this ) );
	oa.addPostShutdown( new TestPostShutdown( "PostShutdown", this ) );

        /*
	 * Shutdown ORB and OA
	 */
	logInformation("Shutting down ORB and OA");
	oa.destroy();
	orb.shutdown();

	/*
	 * Ensure final state is correct
	 */
	logInformation("Final state: " + PrettyPrintState(_currentState) );

	if ( _currentState == POSTSHUTDOWN )
	    assertSuccess();
	else
	    assertFailure();
    }

    /**
     *
     */
    public class TestPreShutdown extends com.arjuna.orbportability.oa.PreShutdown
    {
    	private PrePostTestCallback 	_callback;

    	public TestPreShutdown(String name, PrePostTestCallback callback)
    	{
    	    super(name);

    	    _callback = callback;
    	}

    	/**
    	 * Should be called before the OA is shutdown
    	 */
    	public void work ()
    	{
 	    _callback.preShutdownCalled(name());
	}
    }

    /**
     *
     */
    public class TestPostShutdown extends com.arjuna.orbportability.oa.PostShutdown
    {
    	private PrePostTestCallback 	_callback;

    	public TestPostShutdown(String name, PrePostTestCallback callback)
    	{
    	    super(name);

    	    _callback = callback;
    	}

    	/**
    	 * Should be called before the OA is shutdown
    	 */
    	public void work ()
    	{
 	    _callback.postShutdownCalled(name());
	}
    }


    public static void main(String[] args)
    {
	OAPrePostShutdownTest test = new OAPrePostShutdownTest();

	test.initialise(null, null, args, new org.jboss.dtf.testframework.unittest.LocalHarness());

	test.runTest();
    }
}
