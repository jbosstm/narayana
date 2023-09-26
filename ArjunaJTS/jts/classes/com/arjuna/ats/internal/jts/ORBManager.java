/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts;

import com.arjuna.ats.internal.jts.lifecycle.ShutdownOTS;
import com.arjuna.ats.jts.logging.jtsLogger;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.ORBShutdownListener;
import com.arjuna.orbportability.RootOA;

/**
 * Manage the default ORB and POA used by the transaction service for the
 * non-recoverable CORBA objects it creates.
 */

public class ORBManager
{
    public static final com.arjuna.orbportability.ORB getORB ()
    {
        if (isInitialised())
        {
            return getTheORB();
        }
        else
        {
            jtsLogger.i18NLogger.fatal_ORBManager();
            throw new com.arjuna.ats.arjuna.exceptions.FatalError();
        }
    }

    private static final com.arjuna.orbportability.ORB getTheORB ()
    {
        if (_theOrb == null)
            _theOrb = ORB.getInstance(ORB_NAME);

        return _theOrb;
    }

    public static final boolean setORB (com.arjuna.orbportability.ORB theOrb)
    {
        if (_theOrb == null)
        {
            _theOrb = theOrb;

            theOrb.setORBShutdownListener(new ORBShutdownListener() {

                @Override
                public void orbShutdown() {
                    ORBManager.reset();
                }
            });

            return true;
        }

        return false;
    }

    public static final com.arjuna.orbportability.OA getPOA ()
    {
        if (isInitialised())
        {
            return getThePOA();
        }
        else
        {
            jtsLogger.i18NLogger.fatal_ORBManager();
            throw new com.arjuna.ats.arjuna.exceptions.FatalError();
        }
    }

    private static final com.arjuna.orbportability.OA getThePOA ()
    {
        if (_thePoa == null) {
            _thePoa = RootOA.getRootOA(_theOrb);
            _thePoa.addPreShutdown(new ShutdownOTS());
        }

        return _thePoa;
    }

    public static final boolean setPOA (com.arjuna.orbportability.OA thePoa)
    {
        if (_thePoa == null)
        {
            _thePoa = thePoa;
            _thePoa.addPreShutdown(new ShutdownOTS());
            return true;
        }

        return false;
    }

    public static final com.arjuna.orbportability.Services getServices ()
    {
        return new com.arjuna.orbportability.Services(_theOrb);
    }

    public static final boolean isInitialised ()
    {
        return (_theOrb != null || _thePoa != null);
    }

    public static void reset() {
        _theOrb = null;
        _thePoa = null;
    }

    private static com.arjuna.orbportability.ORB _theOrb = null;

    private static com.arjuna.orbportability.OA _thePoa = null;

    private static final String ORB_NAME = "TransactionORB";
}