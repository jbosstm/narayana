/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators;

import com.arjuna.ats.jts.logging.jtsLogger;

public class ORBRunner extends Thread
{

    public ORBRunner ()
    {
        setDaemon(true);

        start();
    }

    public void run()
    {
        try
        {
            JavaIdlRCServiceInit._orb.orb().run();
        }
        catch (Throwable e)
        {
            jtsLogger.i18NLogger.warn_caughtexception(e);
        }
    }
}