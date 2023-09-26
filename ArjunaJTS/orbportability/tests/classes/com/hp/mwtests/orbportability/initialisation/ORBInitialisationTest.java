/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.orbportability.initialisation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.orb.Attribute;

/**
 * @author Richard Begg
 */
public class ORBInitialisationTest implements TestAttributeCallback
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
        System.out.println("Previous State: " + getStateText(_currentState));

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

        System.out.println(" Current State: " + getStateText(_currentState));
    }

    public void postInitAttributeCalled()
    {
        System.out.println("Previous State: " + getStateText(_currentState));

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

        System.out.println(" Current State: " + getStateText(_currentState));
    }

    @Test
    public void test()
    {
        ORB orb = ORB.getInstance("main_orb");
        RootOA oa = RootOA.getRootOA(orb);

        try {
            _currentState = NONE;

            /*
            * Registering attributes with ORB
            */
            orb.addAttribute( new PreTestAttribute( this ) );
            orb.addAttribute( new PostTestAttribute( this ) );

            /*
	         * Initialise the ORB and OA
	         */
            System.out.println("Initialising ORB and OA");

            orb.initORB(new String[] {}, null);
            oa.initOA();

            assertEquals(POSTINIT, _currentState);
        }
        catch (Exception e)
        {
            fail("Initialisation failed: "+e);
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

}